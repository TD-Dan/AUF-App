package app.auf

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

class StateManager(apiKey: String, private val initialSettings: UserSettings) {

    // ... (StateFlow, jsonParser, gateway, etc. remain the same) ...
    private val _state = MutableStateFlow(AppState(
        selectedModel = initialSettings.selectedModel,
        activeHolonIds = initialSettings.activeHolonIds
    ))
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val jsonParser = Json { isLenient = true; ignoreUnknownKeys = true }
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val gateway: Gateway()
    private val appVersion = "1.0.0"

    init {
        loadCatalogue()
        loadAvailableModels()
    }

    private fun readFileContent(filePath: String): String {
        return try {
            val file = File(filePath)
            if (file.exists()) file.readText() else {
                println("Error: File not found at $filePath")
                ""
            }
        } catch (e: Exception) {
            println("Error reading file at $filePath: ${e.message}")
            ""
        }
    }

    private fun buildContextualPrompt(newMessage: String): String {
        val promptBuilder = StringBuilder()
        val catalogue = _state.value.holonCatalogue

        // Base protocol
        promptBuilder.appendLine(readFileContent("framework/framework_protocol.md"))

        // System state
        val utcTimestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val systemState = """
            {
              "utc_timestamp": "$utcTimestamp",
              "host_application": "AUF App",
              "host_version": "$appVersion",
              "host_os": "${System.getProperty("os.name")}",
              "host_llm": "${_state.value.selectedModel}"
            }
        """.trimIndent()
        promptBuilder.appendLine("\n--- START OF FILE system_state.json ---")
        promptBuilder.appendLine(systemState)

        // Knowledge Graph Index
        promptBuilder.appendLine("\n--- START OF FILE holon_catalogue.json ---")
        promptBuilder.appendLine(readFileContent("framework/holon_catalogue.json"))

        // Active Holons
        val aiPersonaHeader = catalogue.find { it.type == "AI_Persona" }
        val humanPersonaHeader = catalogue.find { it.type == "Human_Persona" }

        if (aiPersonaHeader != null) {
            promptBuilder.appendLine("\n--- START OF FILE ${File(aiPersonaHeader.filePath).name} ---")
            promptBuilder.appendLine(readFileContent("framework/${aiPersonaHeader.filePath}"))
        }
        if (humanPersonaHeader != null) {
            promptBuilder.appendLine("\n--- START OF FILE ${File(humanPersonaHeader.filePath).name} ---")
            promptBuilder.appendLine(readFileContent("framework/${humanPersonaHeader.filePath}"))
        }

        _state.value.activeHolonIds.forEach { holonId ->
            val holonHeader = catalogue.find { it.id == holonId }
            if (holonHeader != null && holonHeader.type != "AI_Persona" && holonHeader.type != "Human_Persona") {
                promptBuilder.appendLine("\n--- START OF FILE ${File(holonHeader.filePath).name} ---")
                promptBuilder.appendLine(readFileContent("framework/${holonHeader.filePath}"))
            }
        }

        // Session History
        promptBuilder.appendLine("\n--- START OF SESSION HISTORY ---")
        _state.value.sessionTranscript.forEach { message ->
            promptBuilder.appendLine("${message.author}: ${message.content}")
        }
        val lastMessage = "${Author.USER}: $newMessage"
        promptBuilder.appendLine(lastMessage)

        return promptBuilder.toString()
    }

    // --- FINAL VERSION: With just-in-time hibernation instructions ---
    fun sendMessage(message: String) {
        if (_state.value.gatewayStatus == GatewayStatus.ERROR) { return }

        val userMessage = ChatMessage(Author.USER, message)
        _state.update { it.copy(sessionTranscript = it.sessionTranscript + userMessage, isProcessing = true) }

        coroutineScope.launch {
            val fullPrompt: String
            val isHibernation = message.trim() == "[SYSTEM_COMMAND: GENERATE_HIBERNATION_PROPOSAL]"

            if (isHibernation) {
                // For hibernation, build the standard prompt AND append the hibernation instructions
                val basePrompt = buildContextualPrompt(message)
                val hibernationInstructions = readFileContent("framework/hibernation_protocol.md")
                fullPrompt = "$basePrompt\n$hibernationInstructions"
            } else {
                // For normal conversation, just build the standard prompt
                fullPrompt = buildContextualPrompt(message)
            }

            val currentModel = _state.value.selectedModel
            val aiResponse = gateway.ask(fullPrompt, currentModel)

            if (isHibernation) {
                // Intercept the hibernation command's output for manual review
                println("\n\n--- HIBERNATION PACKET RECEIVED ---")
                println("--- Please manually copy, review, and commit these files. ---")
                println("----------------------------------------------------------\n")
                println(aiResponse.content)
                println("\n----------------------------------------------------------")
                println("--- END OF HIBERNATION PACKET ---")

                val confirmationMessage = ChatMessage(Author.AI, "Hibernation proposal generated. Please check the application console for the full output.")
                _state.update { it.copy(sessionTranscript = it.sessionTranscript + confirmationMessage, isProcessing = false) }

            } else {
                // Original logic for normal conversation
                _state.update { it.copy(sessionTranscript = it.sessionTranscript + aiResponse, isProcessing = false) }
            }
        }
    }

    // ... (loadCatalogue, loadAvailableModels, and other functions remain the same) ...
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
    private fun loadAvailableModels() {
        coroutineScope.launch {
            if (gateway is GoogleAIGateway) {
                val models = gateway.listModels()
                if (models.isNotEmpty()) {
                    _state.update { it.copy(availableModels = models) }
                } else {
                    _state.update { it.copy(availableModels = listOf("gemini-1.5-pro-latest", "gemini-1.5-flash-latest")) }
                }
            }
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