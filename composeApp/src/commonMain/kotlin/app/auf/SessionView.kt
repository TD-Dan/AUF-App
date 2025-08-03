package app.auf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionView(stateManager: StateManager, modifier: Modifier = Modifier) {
    val appState by stateManager.state.collectAsState()
    val holonCatalogue = appState.holonCatalogue
    // --- CHANGED to use the new Set of active IDs ---
    val activeHolonIds = appState.activeHolonIds
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

        // Filter buttons remain the same
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

        // The LazyColumn now uses the new selection logic
        LazyColumn {
            items(filteredCatalogue) { holon ->
                val isSelected = activeHolonIds.contains(holon.id)
                val backgroundColor = if (isSelected) Color(0xFFE0E0E0) else Color.Transparent
                val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                Text(
                    text = holon.name,
                    fontWeight = fontWeight,
                    modifier = Modifier
                        .fillMaxWidth()
                        // --- CHANGED to call both functions ---
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