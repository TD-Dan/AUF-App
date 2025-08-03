package app.auf

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import java.util.Properties

fun main() = application {
    // --- 1. SETUP: Determine settings directory and initialize managers ---
    val settingsDir = File(System.getProperty("user.home"), ".auf")
    val settingsManager = SettingsManager(settingsDir)

    // Load saved settings, or use defaults if none exist.
    val savedSettings = settingsManager.loadSettings() ?: UserSettings()

    // --- 2. API KEY: Load developer secret from local.properties ---
    val properties = Properties()
    val localPropertiesFile = File("composeApp/local.properties")
    val apiKey = if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
        properties.getProperty("google.api.key", "")
    } else { "" }

    if (apiKey.isBlank()) {
        println("WARNING: google.api.key not found in composeApp/local.properties. AI will not function.")
    }

    // --- 3. STATE INITIALIZATION: Pass key and settings to the StateManager ---
    val stateManager = StateManager(apiKey, savedSettings)

    // Use loaded settings to configure the initial window state.
    val windowState = rememberWindowState(
        width = savedSettings.windowWidth.dp,
        height = savedSettings.windowHeight.dp
    )

    // --- 4. WINDOW LIFECYCLE: Load the app and define the save-on-exit behavior ---
    Window(
        onCloseRequest = {
            // This block runs when the user closes the window.

            // Get the current state from the UI and StateManager.
            val currentSettingsToSave = UserSettings(
                windowWidth = windowState.size.width.value.toInt(),
                windowHeight = windowState.size.height.value.toInt(),
                selectedModel = stateManager.state.value.selectedModel,
                activeHolonIds = stateManager.state.value.activeHolonIds
            )

            // Save the current state to the file.
            settingsManager.saveSettings(currentSettingsToSave)

            // Exit the application.
            exitApplication()
        },
        title = "AUF",
        state = windowState
    ) {
        App(stateManager)
    }
}