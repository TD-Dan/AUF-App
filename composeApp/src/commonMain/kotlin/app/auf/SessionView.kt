package app.auf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionView(stateManager: StateManager, modifier: Modifier = Modifier) {
    val appState by stateManager.state.collectAsState()
    val holonCatalogue = appState.holonCatalogue
    val activeContextualHolonIds = appState.contextualHolonIds
    val activeAiPersonaId = appState.aiPersonaId
    val activeFilter = appState.catalogueFilter
    val holonTypes = holonCatalogue.map { it.type }.distinct().sorted()
    val filteredCatalogue = if (activeFilter == null) {
        holonCatalogue
    } else {
        holonCatalogue.filter { it.type == activeFilter }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Holon Catalogue",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val buttonPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            val buttonFontSize = 12.sp
            Button(
                onClick = { stateManager.setCatalogueFilter(null) },
                contentPadding = buttonPadding,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (activeFilter == null) Color.DarkGray else Color.LightGray,
                    contentColor = if (activeFilter == null) Color.White else Color.Black
                )
            ) { Text("All", fontSize = buttonFontSize) }
            holonTypes.forEach { type ->
                Button(
                    onClick = { stateManager.setCatalogueFilter(type) },
                    contentPadding = buttonPadding,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (activeFilter == type) Color.DarkGray else Color.LightGray,
                        contentColor = if (activeFilter == type) Color.White else Color.Black
                    )
                ) { Text(type, fontSize = buttonFontSize) }
            }
        }

        LazyColumn {
            items(filteredCatalogue) { holon ->
                val isTheActiveAgent = activeAiPersonaId == holon.id
                val isInContext = activeContextualHolonIds.contains(holon.id)
                val isSelected = isTheActiveAgent || isInContext
                val backgroundColor = when {
                    isTheActiveAgent && isInContext -> Color(0xFFA9A9A9) // Darker grey if both active and in context
                    isTheActiveAgent -> Color(0xFFD3D3D3) // Light grey if just active agent
                    isInContext -> Color(0xFFE0E0E0) // Slightly lighter grey if just in context
                    else -> Color.Transparent
                }
                val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                val fontStyle = if (isTheActiveAgent) FontStyle.Italic else FontStyle.Normal
                val displayText = if (isTheActiveAgent) "${holon.name} (Active Agent)" else holon.name

                Text(
                    text = displayText,
                    fontWeight = fontWeight,
                    fontStyle = fontStyle,
                    color = if(isTheActiveAgent) Color.DarkGray else Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        // MODIFIED: Click is now always enabled.
                        .clickable {
                            stateManager.toggleHolonActive(holon.id)
                            stateManager.inspectHolon(holon.id)
                        }
                        .background(backgroundColor)
                        .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
                )
            }
        }
    }
}