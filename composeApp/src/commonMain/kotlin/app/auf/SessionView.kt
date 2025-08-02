package app.auf

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SessionView(stateManager: StateManager) {
    val appState by stateManager.state.collectAsState()

    Column(Modifier.padding(8.dp)) {
        Text("Sessions", style = MaterialTheme.typography.h6, modifier = Modifier.padding(bottom = 8.dp))
        LazyColumn {
            items(appState.holonCatalogue) { holon ->
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        // This is the key change: we make the item clickable.
                        .clickable {
                            // When clicked, we call the corresponding function on our StateManager.
                            // This is an "event" flowing up from the UI.
                            stateManager.startSession(holon.id)
                        }
                ) {
                    Text(holon.name, style = MaterialTheme.typography.subtitle1)
                    Text(holon.summary, style = MaterialTheme.typography.caption)
                }
                Divider()
            }
        }
    }
}