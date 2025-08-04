package app.auf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatView(stateManager: StateManager, modifier: Modifier = Modifier) {
    val appState by stateManager.state.collectAsState()
    val chatHistory = appState.chatHistory
    val isProcessing = appState.isProcessing
    var userMessage by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    // --- Data for Control Panel ---
    val availableModels = appState.availableModels
    val selectedModel = appState.selectedModel
    // ADDED: Get data for the new "Active Agent" dropdown
    val aiPersonas = appState.holonCatalogue.filter { it.type == "AI_Persona" }
    val selectedAiPersonaId = appState.aiPersonaId
    val selectedAiPersonaName = aiPersonas.find { it.id == selectedAiPersonaId }?.name ?: "None"

    var isModelSelectorExpanded by remember { mutableStateOf(false) }
    // ADDED: State for the new dropdown
    var isAgentSelectorExpanded by remember { mutableStateOf(false) }

    val sendMessageAction = {
        if (userMessage.isNotBlank() && !isProcessing) {
            stateManager.sendMessage(userMessage)
            userMessage = ""
        }
    }
    val displayedHistory = if (appState.isSystemVisible) chatHistory else chatHistory.filter { it.author != Author.SYSTEM }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Message History (unchanged from previous version)
        LazyColumn(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
            items(displayedHistory) { message ->
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val authorText = when (message.author) {
                            Author.SYSTEM -> "SYSTEM: ${message.title}"
                            else -> "${message.author}:"
                        }
                        val authorFontWeight = when (message.author) {
                            Author.SYSTEM, Author.USER -> FontWeight.Bold
                            else -> FontWeight.Normal
                        }
                        Text(
                            text = authorText,
                            fontWeight = authorFontWeight,
                            fontStyle = if (message.author == Author.SYSTEM) FontStyle.Italic else FontStyle.Normal,
                            fontSize = if (message.author == Author.SYSTEM) 12.sp else 14.sp,
                            color = if (message.author == Author.SYSTEM) Color.Gray else Color.Unspecified
                        )
                        IconButton(
                            onClick = { clipboardManager.setText(AnnotatedString(message.content)) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, "Copy", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                    val messageModifier = if (message.author == Author.SYSTEM) {
                        Modifier.fillMaxWidth().background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    } else { Modifier.padding(start = 8.dp) }
                    Text(
                        text = message.content,
                        modifier = messageModifier,
                        fontStyle = if (message.author == Author.SYSTEM) FontStyle.Italic else FontStyle.Normal,
                        fontSize = if (message.author == Author.SYSTEM) 12.sp else 14.sp,
                        color = if (message.author == Author.SYSTEM) Color.DarkGray else Color.Unspecified
                    )
                }
            }
        }

        // --- MODIFIED: Control Panel Toolbar ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { stateManager.toggleSystemMessageVisibility() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray.copy(alpha = 0.4f))
            ) { Text(if (appState.isSystemVisible) "Hide System" else "Show System") }

            Column(horizontalAlignment = Alignment.End) {
                // Active Agent Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Active Agent:", fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Box {
                        Button(onClick = { isAgentSelectorExpanded = true }) { Text(selectedAiPersonaName) }
                        DropdownMenu(expanded = isAgentSelectorExpanded, onDismissRequest = { isAgentSelectorExpanded = false }) {
                            DropdownMenuItem(onClick = {
                                stateManager.selectAiPersona(null)
                                isAgentSelectorExpanded = false
                            }) { Text("None") }
                            aiPersonas.forEach { persona ->
                                DropdownMenuItem(onClick = {
                                    stateManager.selectAiPersona(persona.id)
                                    isAgentSelectorExpanded = false
                                }) { Text(persona.name) }
                            }
                        }
                    }
                }
                // Model Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Model:", fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Box {
                        Button(onClick = { isModelSelectorExpanded = true }) { Text(selectedModel) }
                        DropdownMenu(expanded = isModelSelectorExpanded, onDismissRequest = { isModelSelectorExpanded = false }) {
                            availableModels.forEach { modelName ->
                                DropdownMenuItem(onClick = {
                                    stateManager.selectModel(modelName)
                                    isModelSelectorExpanded = false
                                }) { Text(modelName) }
                            }
                        }
                    }
                }
            }
        }

        // Input field and send button (unchanged)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                modifier = Modifier.weight(1f).onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.isCtrlPressed && event.key == Key.Enter) {
                        sendMessageAction(); true
                    } else false
                },
                placeholder = { Text("Type your message...") },
                enabled = !isProcessing
            )
            Button(onClick = sendMessageAction, modifier = Modifier.padding(start = 8.dp), enabled = !isProcessing) {
                if (isProcessing) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                } else { Text("Send") }
            }
        }
    }
}