package app.auf

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HolonInspectorView(stateManager: StateManager, modifier: Modifier = Modifier) {
    val appState by stateManager.state.collectAsState()
    // Find the full Holon object from the activeHolons map.
    val activeHolon = appState.activeHolons[appState.activeHolonId]

    // The outer Box now uses the modifier passed from App.kt and adds padding.
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.TopStart // Align content to the top-start.
    ) {
        if (activeHolon != null) {
            // If a holon is selected, display its details.
            Column {
                Text(
                    text = activeHolon.header.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "ID: ${activeHolon.header.id}",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = activeHolon.header.summary,
                    fontSize = 14.sp
                )

                // Add a divider and display the main content of the holon.
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = activeHolon.content,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        } else {
            // If no holon is selected, show a placeholder message.
            Text("Select a holon from the catalogue to see its details.", fontStyle = FontStyle.Italic)
        }
    }
}