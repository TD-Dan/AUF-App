package app.auf

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class StateManager(apiKey: String) {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val jsonParser = Json { isLenient = true; ignoreUnknownKeys = true }
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    // The gateway is now instantiated with the provided API key.
    private val gateway = Gateway(apiKey)

    fun loadCatalogue() {
        try {
            val catalogueFile = File("framework/holon_catalogue.json")
            val catalogueJson = catalogueFile.readText()
            val parsedCatalogue = jsonParser.decodeFromString<HolonCatalogueFile>(catalogueJson)

            _state.update { currentState ->
                currentState.copy(holonCatalogue = parsedCatalogue.holon_catalogue)
            }
        } catch (e: Exception) {
            println("Error loading catalogue: ${e.message}")
            _state.update { it.copy(gatewayStatus = GatewayStatus.ERROR) }
        }
    }

    fun startSession(holonId: String) {
        val holonHeader = _state.value.holonCatalogue.find { it.id == holonId }
        if (holonHeader == null) {
            println("Error: Holon with ID $holonId not found in catalogue.")
            return
        }

        try {
            val holonFile = File("framework/${holonHeader.filePath}")
            val holonContent = holonFile.readText()
            val holon = Holon(header = holonHeader, content = holonContent)

            _state.update {
                it.copy(
                    activeHolonId = holonId,
                    activeHolons = it.activeHolons + (holonId to holon),
                    sessionTranscript = emptyList()
                )
            }
        } catch (e: Exception) {
            println("Error loading holon content for ${holonHeader.name}: ${e.message}")
        }
    }

    fun sendMessage(message: String) {
        val userMessage = ChatMessage(Author.USER, message)
        _state.update {
            it.copy(
                sessionTranscript = it.sessionTranscript + userMessage,
                isProcessing = true
            )
        }

        coroutineScope.launch {
            val aiResponse = gateway.ask(message)
            _state.update {
                it.copy(
                    sessionTranscript = it.sessionTranscript + aiResponse,
                    isProcessing = false
                )
            }
        }
    }

    fun setCatalogueFilter(type: String?) {
        _state.update { it.copy(catalogueFilter = type) }
    }
}