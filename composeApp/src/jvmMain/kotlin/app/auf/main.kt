package app.auf

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import java.util.Properties

fun main() = application {
    // Read the API key from local.properties
    val properties = Properties()
    val localPropertiesFile = File("local.properties")
    val apiKey = if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
        // Corrected property name to align with our non-OpenAI approach
        properties.getProperty("google.api.key", "")
    } else {
        ""
    }

    if (apiKey.isBlank()) {
        println("WARNING: google.api.key not found in local.properties. The application will not be able to connect to the AI.")
    }

    // Pass the API key to the StateManager. It will now self-initialize.
    val stateManager = StateManager(apiKey)
    // stateManager.loadCatalogue() // <-- REMOVED THIS LINE

    Window(
        onCloseRequest = ::exitApplication,
        title = "AUF",
        state = rememberWindowState(width = 1400.dp, height = 900.dp)
    ) {
        App(stateManager)
    }
}