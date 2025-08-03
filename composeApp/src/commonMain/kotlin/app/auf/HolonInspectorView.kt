package app.auf

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    // --- CORRECTED LOGIC: Now we just look up the holon by the inspected ID ---
    val inspectedHolon = appState.activeHolons[appState.inspectedHolonId]

    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        if (inspectedHolon != null) {
            // Added a scrollable column for long holon content
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = inspectedHolon.header.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "ID: ${inspectedHolon.header.id}",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = inspectedHolon.header.summary,
                    fontSize = 14.sp
                )
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = inspectedHolon.content,
                    fontSize = 13.sp,
                    color = Color.DarkGray // Improved contrast
                )
            }
        } else {
            Text("Select a holon from the catalogue to see its details.", fontStyle = FontStyle.Italic)
        }
    }
}