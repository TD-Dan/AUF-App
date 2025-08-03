package app.auf

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class StateManager(apiKey: String, private val initialSettings: UserSettings) {

    // Now this line can correctly access the 'initialSettings' property.
    private val _state = MutableStateFlow(AppState(
        selectedModel = initialSettings.selectedModel,
        activeHolonIds = initialSettings.activeHolonIds
    ))
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val jsonParser = Json { isLenient = true; ignoreUnknownKeys = true }
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val gateway: GatewayInterface

    init {
        gateway = GoogleAIGateway(apiKey)
        loadCatalogue()
        loadAvailableModels()
    }

    private fun loadCatalogue() {
        try {
            val catalogueFile = File("framework/holon_catalogue.json")
            if (!catalogueFile.exists()) { throw IllegalStateException("Critical Failure: holon_catalogue.json not found.") }
            val catalogueJson = catalogueFile.readText()
            val parsedCatalogue = jsonParser.decodeFromString<HolonCatalogueFile>(catalogueJson)
            _state.update { it.copy(holonCatalogue = parsedCatalogue.holon_catalogue, gatewayStatus = GatewayStatus.OK) }
        } catch (e: Exception) {
            e.printStackTrace()
            _state.update { it.copy(gatewayStatus = GatewayStatus.ERROR) }
        }
    }

    // --- CORRECTED to be more resilient, as you suggested ---
    private fun loadAvailableModels() {
        coroutineScope.launch {
            // We can only list models if we have a GoogleAIGateway
            if (gateway is GoogleAIGateway) {
                val models = gateway.listModels()
                if (models.isNotEmpty()) {
                    _state.update { it.copy(availableModels = models) }
                } else {
                    // If the API call fails or returns no models, use the hardcoded default
                    _state.update { it.copy(availableModels = listOf("gemini-1.5-pro-latest", "gemini-1.5-flash-latest")) }
                }
            }
        }
    }

    // --- CORRECTED to pass the model to the gateway ---
    fun sendMessage(message: String) {
        if (_state.value.gatewayStatus == GatewayStatus.ERROR) { return }

        val userMessage = ChatMessage(Author.USER, message)
        val currentModel = _state.value.selectedModel // Get the currently selected model
        _state.update { it.copy(sessionTranscript = it.sessionTranscript + userMessage, isProcessing = true) }

        coroutineScope.launch {
            // Pass the model to the gateway's ask function
            val aiResponse = gateway.ask(message, currentModel)
            _state.update { it.copy(sessionTranscript = it.sessionTranscript + aiResponse, isProcessing = false) }
        }
    }

    fun toggleHolonActive(holonId: String) {
        val currentActiveIds = _state.value.activeHolonIds
        val newActiveIds = if (currentActiveIds.contains(holonId)) currentActiveIds - holonId else currentActiveIds + holonId
        _state.update { it.copy(activeHolonIds = newActiveIds) }
    }

    fun inspectHolon(holonId: String?) {
        if (holonId == null) {
            _state.update { it.copy(inspectedHolonId = null) }
            return
        }
        if (_state.value.activeHolons.containsKey(holonId)) {
            _state.update { it.copy(inspectedHolonId = holonId) }
            return
        }
        val holonHeader = _state.value.holonCatalogue.find { it.id == holonId } ?: return
        try {
            val holonFile = File("framework/${holonHeader.filePath}")
            val holon = Holon(header = holonHeader, content = holonFile.readText())
            _state.update { it.copy(inspectedHolonId = holonId, activeHolons = it.activeHolons + (holonId to holon)) }
        } catch (e: Exception) {
            println("Error loading holon content for inspection: ${e.message}")
        }
    }

    fun selectModel(modelName: String) {
        if (modelName in _state.value.availableModels) {
            _state.update { it.copy(selectedModel = modelName) }
        }
    }

    fun setCatalogueFilter(type: String?) {
        _state.update { it.copy(catalogueFilter = type) }
    }
}