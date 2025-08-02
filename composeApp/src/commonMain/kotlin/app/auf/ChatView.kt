package app.auf

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp

@Composable
fun ChatView(stateManager: StateManager, modifier: Modifier = Modifier) {
    val appState by stateManager.state.collectAsState()
    val transcript = appState.sessionTranscript
    val isProcessing = appState.isProcessing
    var userMessage by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // 1. Message History
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(transcript) { message ->
                Text("${message.author}: ${message.content}")
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
                // Disable the text field while the AI is processing.
                enabled = !isProcessing
            )
            Button(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        stateManager.sendMessage(userMessage)
                        userMessage = "" // Clear the input field
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
                // Disable the button and show a progress indicator.
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