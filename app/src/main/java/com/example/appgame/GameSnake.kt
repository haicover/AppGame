package com.example.appgame

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.appgame.ui.theme.DarkGreen
import com.example.appgame.ui.theme.Shapes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Random

@Composable
fun GameSnake(navController: NavController) {
    val scope = rememberCoroutineScope()
    val game = Game(scope)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Snake") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = Color(0xFF6200EA),
                contentColor = Color.White
            )
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                Snake(game)
            }
        }
    )
}

data class State(val food: Pair<Int, Int>, val snake: List<Pair<Int, Int>>, val score: Int)

class Game(private val scope: CoroutineScope) {

    private val mutex = Mutex()
    private val mutableState =
        MutableStateFlow(State(food = Pair(5, 5), snake = listOf(Pair(7, 7)), score = 0))
    val state: Flow<State> = mutableState

    var move = Pair(1, 0)
        set(value) {
            scope.launch {
                mutex.withLock {
                    field = value
                }
            }
        }

    // Trạng thái tạm dừng và hiển thị Dialog
    var isPaused = MutableStateFlow(false) // Thêm trạng thái tạm dừng
    val showDialog = MutableStateFlow(false)
    val gameResult = MutableStateFlow("")

    init {
        scope.launch {
            var snakeLength = 4
            var score = 0
            while (true) {
                delay(150)

                // Nếu đang tạm dừng thì bỏ qua vòng lặp
                if (isPaused.value) continue

                mutableState.update {
                    val newPosition = it.snake.first().let { poz ->
                        mutex.withLock {
                            Pair(
                                (poz.first + move.first + BOARD_SIZE) % BOARD_SIZE,
                                (poz.second + move.second + BOARD_SIZE) % BOARD_SIZE
                            )
                        }
                    }

                    // Kiểm tra điều kiện thắng hoặc thất bại
                    if (newPosition in it.snake) {
                        showDialog.value = true
                        gameResult.value = "Bạn đã thất bại!"
                        snakeLength = 4
                        score = 0
                    }

                    if (newPosition == it.food) {
                        snakeLength++
                        score++
                    }

                    if (score >= 20) {
                        showDialog.value = true
                        gameResult.value = "Bạn đã chiến thắng!"
                        snakeLength = 4
                        score = 0
                    }

                    it.copy(
                        food = if (newPosition == it.food) Pair(
                            Random().nextInt(BOARD_SIZE),
                            Random().nextInt(BOARD_SIZE)
                        ) else it.food,
                        snake = listOf(newPosition) + it.snake.take(snakeLength - 1)
                    )
                }
            }
        }
    }

    companion object {
        const val BOARD_SIZE = 16
    }
}

@Composable
fun Snake(game: Game) {
    val state = game.state.collectAsState(initial = null)
    val isPaused by game.isPaused.collectAsState()
    val showDialog by game.showDialog.collectAsState()
    val gameResult by game.gameResult.collectAsState()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        state.value?.let {
            Board(it)
        }
        Buttons(
            onDirectionChange = { game.move = it },
            isPaused = isPaused,
            onPauseToggle = { game.isPaused.value = !isPaused }
        )

        // Hiển thị Dialog khi đạt điều kiện
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { game.showDialog.value = false },
                title = { Text("Kết quả") },
                text = { Text(gameResult) },
                confirmButton = {
                    Button(onClick = { game.showDialog.value = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun Buttons(
    onDirectionChange: (Pair<Int, Int>) -> Unit,
    isPaused: Boolean,
    onPauseToggle: () -> Unit
) {
    val buttonSize = Modifier.size(64.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
        Button(onClick = { onDirectionChange(Pair(0, -1)) }, modifier = buttonSize) {
            Icon(Icons.Default.KeyboardArrowUp, null)
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Trái
            Button(onClick = { onDirectionChange(Pair(-1, 0)) }, modifier = buttonSize) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
            }

            // Hình ảnh Play/Pause ở giữa
            Image(
                painter = if (isPaused) {
                    painterResource(id = R.drawable.play) // Hình ảnh play
                } else {
                    painterResource(id = R.drawable.pauses) // Hình ảnh pause
                },
                contentDescription = if (isPaused) "Play" else "Pause",
                modifier = Modifier
                    .padding(5.dp)
                    .size(50.dp)
                    .clickable { onPauseToggle() } // Thêm sự kiện click cho nút Play/Pause
            )

            // Nút Phải
            Button(onClick = { onDirectionChange(Pair(1, 0)) }, modifier = buttonSize) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
        Button(onClick = { onDirectionChange(Pair(0, 1)) }, modifier = buttonSize) {
            Icon(Icons.Default.KeyboardArrowDown, null)
        }
    }
}

@Composable
fun Board(state: State) {
    BoxWithConstraints(Modifier.padding(16.dp)) {
        val tileSize = maxWidth / Game.BOARD_SIZE

        Box(
            Modifier
                .size(maxWidth)
                .border(2.dp, Color.Green)
        )

        // Vẽ thức ăn
        Box(
            Modifier
                .offset(x = tileSize * state.food.first, y = tileSize * state.food.second)
                .size(tileSize)
                .background(
                    Color.Green, CircleShape
                )
        )

        // Vẽ con rắn
        state.snake.forEach {
            Box(
                modifier = Modifier
                    .offset(x = tileSize * it.first, y = tileSize * it.second)
                    .size(tileSize)
                    .background(
                        Color.Green, Shapes.small
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGameSnake() {
    GameSnake(navController = rememberNavController())
}