package com.example.appgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CombinedScreen(navController: NavController) {
    var isGameStarted by remember { mutableStateOf(false) }
    var playerOne by remember { mutableStateOf(TextFieldValue("")) }
    var playerTwo by remember { mutableStateOf(TextFieldValue("")) }

    if (isGameStarted) {
        // Hiển thị MainScreen sau khi bắt đầu trò chơi
        MainScreen(
            playerOne = playerOne.text,
            playerTwo = playerTwo.text
        ) {
            // Khi kết thúc trò chơi, quay lại màn hình nhập tên người chơi
            isGameStarted = false
            playerOne = TextFieldValue("")
            playerTwo = TextFieldValue("")
        }
    } else {
        // Hiển thị AddPlayersScreen để nhập tên
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Game Tic-tac-toe") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    backgroundColor = Color(0xFF6200EA),  // A vibrant purple color
                    contentColor = Color.White
                )
            },
            content = { paddingValues ->
                // Ensure the content starts below the TopAppBar
                Column(modifier = Modifier.padding(paddingValues)) {
                    AddPlayersScreen(
                        onStartGame = { name1, name2 ->
                            playerOne = TextFieldValue(name1)
                            playerTwo = TextFieldValue(name2)
                            isGameStarted = true // Chuyển sang màn hình trò chơi
                        }
                    )
                }
            }
        )

    }
}

@Composable
fun AddPlayersScreen(onStartGame: (String, String) -> Unit) {
    var playerOne by remember { mutableStateOf(TextFieldValue("")) }
    var playerTwo by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF))
            .padding(30.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(30.dp),
            elevation = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ENTER \n PLAYERS NAMES",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = playerOne,
                    onValueChange = { playerOne = it },
                    label = { Text("Enter player one name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = playerTwo,
                    onValueChange = { playerTwo = it },
                    label = { Text("Enter player two name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { onStartGame(playerOne.text, playerTwo.text) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6A1B9A)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Start Game",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

