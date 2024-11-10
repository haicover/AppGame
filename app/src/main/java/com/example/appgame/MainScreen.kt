package com.example.appgame

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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(playerOne: String, playerTwo: String, onGameEnded: () -> Unit) {
    val boxPositions = remember { mutableStateListOf(0, 0, 0, 0, 0, 0, 0, 0, 0) }
    var playerTurn by remember { mutableStateOf(1) }
    var totalSelectedBoxes by remember { mutableStateOf(1) }
    var showDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }

    val winningCombinations = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )

    fun checkResults(): Boolean {
        return winningCombinations.any { combo ->
            boxPositions[combo[0]] == playerTurn &&
                    boxPositions[combo[1]] == playerTurn &&
                    boxPositions[combo[2]] == playerTurn
        }
    }

    fun restartMatch() {
        boxPositions.replaceAll { 0 }
        playerTurn = 1
        totalSelectedBoxes = 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$playerOne vs $playerTwo", style = MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(16.dp))

        for (row in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(if (boxPositions[index] == 0) Color.LightGray else if (boxPositions[index] == 1) Color.Blue else Color.Red)
                            .clickable(enabled = boxPositions[index] == 0) {
                                boxPositions[index] = playerTurn
                                if (checkResults()) {
                                    resultMessage = if (playerTurn == 1) "$playerOne is the Winner!" else "$playerTwo is the Winner!"
                                    showDialog = true
                                } else if (totalSelectedBoxes == 9) {
                                    resultMessage = "Match Draw"
                                    showDialog = true
                                } else {
                                    playerTurn = if (playerTurn == 1) 2 else 1
                                    totalSelectedBoxes++
                                }
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showDialog) {
            ResultDialog(
                message = resultMessage,
                onDismiss = {
                    restartMatch()
                    showDialog = false
                    onGameEnded() // Quay lại màn hình nhập tên sau khi hiển thị kết quả
                }
            )
        }
    }
}
@Composable
fun ResultDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Result") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Start Again")
            }
        }
    )
}