package app.auf

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ChatView(stateManager: StateManager, modifier: Modifier = Modifier) {
    val appState by stateManager.state.collectAsState()
    val transcript = appState.sessionTranscript
    val isProcessing = appState.isProcessing
    var userMessage by remember { mutableStateOf("") }

    // --- State for the new model selector ---
    val availableModels = appState.availableModels
    val selectedModel = appState.selectedModel
    var isModelSelectorExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // 1. Message History
        LazyColumn(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
            items(transcript) { message ->
                // A slightly more structured look for messages
                val fontWeight = if (message.author == Author.USER) FontWeight.Bold else FontWeight.Normal
                Text(
                    text = "${message.author}:",
                    fontWeight = fontWeight,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = message.content,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
            }
        }

        // --- ADDED: Model Selector UI ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text("Model:")
            Spacer(Modifier.width(8.dp))
            Box {
                Button(onClick = { isModelSelectorExpanded = true }) {
                    Text(selectedModel)
                }
                DropdownMenu(
                    expanded = isModelSelectorExpanded,
                    onDismissRequest = { isModelSelectorExpanded = false }
                ) {
                    availableModels.forEach { modelName ->
                        DropdownMenuItem(onClick = {
                            stateManager.selectModel(modelName)
                            isModelSelectorExpanded = false
                        }) {
                            Text(modelName)
                        }
                    }
                }
            }
        }


        // 2. Input field and send button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message...") },
                enabled = !isProcessing
            )
            Button(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        stateManager.sendMessage(userMessage)
                        userMessage = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(24.dp)
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                } else {
                    Text("Send")
                }
            }
        }
    }
}