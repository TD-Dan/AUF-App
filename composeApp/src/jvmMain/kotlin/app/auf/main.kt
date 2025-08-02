package app.auf

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.auf.App
import app.auf.StateManager

fun main() = application {
    // 1. Create a single, persistent instance of our StateManager.
    val stateManager = StateManager()

    // 2. Call the initial action to load our data.
    stateManager.loadCatalogue()

    Window(onCloseRequest = ::exitApplication, title = "AUF") {
        // 3. Pass the stateManager instance into our root UI Composable.
        App(stateManager)
    }
}