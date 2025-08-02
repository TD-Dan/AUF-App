package app.auf

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.io.File

class StateManager {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // Configure the JSON parser.
    private val jsonParser = Json { isLenient = true; ignoreUnknownKeys = true }

    fun loadCatalogue() {
        try {
            // For now, we assume the framework files are in a 'framework' folder
            // relative to where the app is run.
            val catalogueFile = File("framework/holon_catalogue.json")
            val catalogueJson = catalogueFile.readText()
            val parsedCatalogue = jsonParser.decodeFromString<HolonCatalogueFile>(catalogueJson)

            _state.update { currentState ->
                currentState.copy(holonCatalogue = parsedCatalogue.holon_catalogue)
            }
        } catch (e: Exception) {
            // Handle error (e.g., file not found)
            // For now, we'll just print the error. A real implementation would show a UI message.
            println("Error loading catalogue: ${e.message}")
            _state.update { it.copy(gatewayStatus = GatewayStatus.ERROR) } // Use status for feedback
        }
    }

    fun startSession(holonId: String) {
        // --- TODO ---
        println("TODO: Implement startSession for holonId: $holonId")
    }

    fun sendMessage(message: String) {
        // --- TODO ---
        println("TODO: Implement sendMessage with message: $message")
    }
}