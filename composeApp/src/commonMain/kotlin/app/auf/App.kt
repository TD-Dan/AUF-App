package app.auf

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App(stateManager: StateManager) {
    MaterialTheme {
        Row(Modifier.fillMaxSize()) {
            // 1. Session View (Left Pane) - Given a fixed width
            SessionView(stateManager, modifier = Modifier.width(320.dp))

            // A vertical line to separate the panes
            Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

            // 2. Chat UI (Center Pane) - Fills remaining space
            ChatView(stateManager, modifier = Modifier.weight(1f))

            // A vertical line to separate the panes
            Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

            // 3. Holon Inspector (Right Pane) - Given a fixed width
            HolonInspectorView(stateManager, modifier = Modifier.width(320.dp))
        }
    }
}