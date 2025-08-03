package app.auf

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class StateManager(apiKey: String) {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val jsonParser = Json { isLenient = true; ignoreUnknownKeys = true }
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val gateway: GatewayInterface

    init {
        gateway = GoogleAIGateway(apiKey)
        // ADDED: The StateManager now loads its own catalogue upon creation.
        loadCatalogue()
    }

    private fun loadCatalogue() { // This function correctly remains private
        try {
            val catalogueFile = File("framework/holon_catalogue.json")
            if (!catalogueFile.exists()) {
                throw IllegalStateException("Critical Failure: holon_catalogue.json not found at ${catalogueFile.absolutePath}")
            }
            val catalogueJson = catalogueFile.readText()
            val parsedCatalogue = jsonParser.decodeFromString<HolonCatalogueFile>(catalogueJson)

            _state.update { currentState ->
                currentState.copy(
                    holonCatalogue = parsedCatalogue.holon_catalogue,
                    gatewayStatus = GatewayStatus.OK
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _state.update { it.copy(gatewayStatus = GatewayStatus.ERROR) }
        }
    }

    // ... (rest of the file is unchanged) ...
    fun startSession(holonId: String) {
        val holonHeader = _state.value.holonCatalogue.find { it.id == holonId }
        if (holonHeader == null) {
            println("Error: Holon with ID $holonId not found in catalogue.")
            return
        }

        try {
            val holonFile = File("framework/${holonHeader.filePath}")
            if (!holonFile.exists()) {
                throw IllegalStateException("Holon file not found: ${holonFile.absolutePath}")
            }
            val holonContent = holonFile.readText()
            val holon = Holon(header = holonHeader, content = holonContent)

            _state.update {
                it.copy(
                    activeHolonId = holonId,
                    activeHolons = it.activeHolons + (holonId to holon),
                    sessionTranscript = emptyList() // Clear transcript for new session
                )
            }
        } catch (e: Exception) {
            println("Error loading holon content for ${holonHeader.name}: ${e.message}")
        }
    }

    fun sendMessage(message: String) {
        // Prevent sending messages if the gateway failed to initialize
        if (_state.value.gatewayStatus == GatewayStatus.ERROR) {
            println("Cannot send message, gateway is in an error state.")
            return
        }

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