package com.example.appgame

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class Tube(var colors: List<Color>)

@SuppressLint("NewApi")
@Composable
fun ColorSortingGame(navController: NavController) {
    val colors = listOf(
        Color(0xFFFFC107), // Yellow
        Color(0xFFFF5722), // Orange
        Color(0xFF2196F3)  // Blue
    )

    var tubes by remember {
        mutableStateOf(
            List(4) { Tube(generateRandomColors(colors)) } +
                    List(2) { Tube(mutableListOf()) } // Add two empty tubes for transfer
        )
    }
    val history = remember { mutableStateListOf<List<Tube>>() }
    var selectedTubeIndex by remember { mutableStateOf<Int?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Color Flood") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    tubes.forEachIndexed { index, tube ->
                        TubeView(
                            tube = tube,
                            onClick = {
                                history.add(tubes.map { it.copy(colors = it.colors.toMutableList()) })
                                handleTubeClick(
                                    index,
                                    tubes,
                                    selectedTubeIndex
                                ) { newTubes, newSelectedIndex ->
                                    tubes = newTubes
                                    selectedTubeIndex = newSelectedIndex
                                }
                                if (checkCompletion(tubes)) {
                                    showDialog = true
                                }
                            },
                            isSelected = selectedTubeIndex == index
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (history.isNotEmpty()) {
                            tubes = history[history.size - 1] // Quay lại trạng thái trước đó
                            history.removeAt(history.size - 1) // Xóa phần tử cuối cùng khỏi history
                        }
                    },
                    enabled = history.isNotEmpty()
                ) {
                    Text("Undo")
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("OK")
                        }
                    },
                    title = { Text("Congratulations!") },
                    text = { Text("You've successfully sorted all the colors!") }
                )
            }
        }
    )
}

@Composable
fun TubeView(tube: Tube, onClick: () -> Unit, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(60.dp, 200.dp)
            .background(
                color = if (isSelected) Color.Gray else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            tube.colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                        .background(color = color, shape = RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

fun handleTubeClick(
    clickedIndex: Int,
    tubes: List<Tube>,
    selectedTubeIndex: Int?,
    onUpdate: (List<Tube>, Int?) -> Unit
) {
    if (selectedTubeIndex == null) {
        // First tube selection
        onUpdate(tubes, clickedIndex)
    } else if (selectedTubeIndex == clickedIndex) {
        // Deselect if the same tube is clicked again
        onUpdate(tubes, null)
    } else {
        // Try to transfer liquid
        val sourceTube = tubes[selectedTubeIndex]
        val targetTube = tubes[clickedIndex]

        if (sourceTube.colors.isNotEmpty()) {
            val sourceColor = sourceTube.colors.last()
            if (targetTube.colors.isEmpty() || targetTube.colors.first() == sourceColor) {
                if (targetTube.colors.size < 4) { // Check if target has space
                    val updatedTubes = tubes.toMutableList()

                    // Chuyển nước từ chai nguồn sang chai đích (thêm vào dưới cùng)
                    updatedTubes[selectedTubeIndex] =
                        Tube(sourceTube.colors.dropLast(1).toMutableList())
                    updatedTubes[clickedIndex] =
                        Tube(mutableListOf(sourceColor) + targetTube.colors)

                    onUpdate(updatedTubes, null)
                }
            } else {
                onUpdate(tubes, null) // Invalid move, reset selection
            }
        }
    }
}

fun generateRandomColors(colors: List<Color>): MutableList<Color> {
    return colors.shuffled().take(3).toMutableList() // Random colors with max 3 layers
}

fun checkCompletion(tubes: List<Tube>): Boolean {
    return tubes.all { tube ->
        tube.colors.isEmpty() || (tube.colors.distinct().size == 1 && tube.colors.size == 4)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewColorSortingGame() {
    ColorSortingGame(navController = rememberNavController())
}