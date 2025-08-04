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
import java.time.Instant
import java.time.format.DateTimeFormatter

class StateManager(private val apiKey: String, private val initialSettings: UserSettings) {

    private val _state = MutableStateFlow(AppState(
        selectedModel = initialSettings.selectedModel,
        aiPersonaId = initialSettings.selectedAiPersonaId,
        contextualHolonIds = initialSettings.activeContextualHolonIds
    ))
    val state: StateFlow<AppState> = _state.asStateFlow()
    // ... (other properties are correct)
    private val jsonParser = Json { isLenient = true; ignoreUnknownKeys = true }
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val gateway: Gateway = Gateway()
    private val appVersion = "1.0.0"

    init {
        loadCatalogue()
        loadAvailableModels()
    }

    // MODIFIED: This function is now simplified. Its only job is to manage the context set.
    // It no longer needs to check the holon type.
    fun toggleHolonActive(holonId: String) {
        val state = _state.value
        val newContextIds = if (state.contextualHolonIds.contains(holonId)) {
            state.contextualHolonIds - holonId
        } else {
            state.contextualHolonIds + holonId
        }
        _state.update { it.copy(contextualHolonIds = newContextIds) }
    }

    // --- All other functions from the previous correct version remain the same ---
    fun selectAiPersona(holonId: String?) { _state.update { it.copy(aiPersonaId = holonId) } }
    private fun readFileContent(filePath: String): String { return try { File(filePath).readText() } catch (e: Exception) { "Error reading file: $filePath" } }
    private fun buildPromptMessages(newMessage: String): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        val catalogue = _state.value.holonCatalogue
        val state = _state.value
        messages.add(ChatMessage(Author.SYSTEM, readFileContent("framework/framework_protocol.md"), "framework_protocol.md"))
        val utcTimestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val systemStateContent = """
            {
              "utc_timestamp": "$utcTimestamp",
              "host_application": "AUF App",
              "host_version": "$appVersion",
              "host_os": "${System.getProperty("os.name")}",
              "host_llm": "${state.selectedModel}"
            }
        """.trimIndent()
        messages.add(ChatMessage(Author.SYSTEM, systemStateContent, "system_state.json"))
        messages.add(ChatMessage(Author.SYSTEM, readFileContent("framework/holon_catalogue.json"), "holon_catalogue.json"))
        val allActiveIds = (state.contextualHolonIds + listOfNotNull(state.aiPersonaId)).toSet()
        allActiveIds.forEach { holonId ->
            catalogue.find { it.id == holonId }?.let { header ->
                messages.add(ChatMessage(Author.SYSTEM, readFileContent("framework/${header.filePath}"), File(header.filePath).name))
            }
        }
        state.chatHistory.filter { it.author == Author.USER || it.author == Author.AI }.forEach { messages.add(it) }
        messages.add(ChatMessage(Author.USER, newMessage, "User"))
        return messages
    }
    private fun convertChatToApiContents(messages: List<ChatMessage>): List<Content> {
        val apiContents = mutableListOf<Content>()
        val userPromptParts = mutableListOf<String>()
        var historyProcessed = false
        messages.forEach { msg ->
            when (msg.author) {
                Author.SYSTEM -> userPromptParts.add("--- START OF FILE ${msg.title} ---\n${msg.content}")
                Author.USER, Author.AI -> {
                    if (!historyProcessed) {
                        if (userPromptParts.isNotEmpty()) {
                            apiContents.add(Content("user", listOf(Part(userPromptParts.joinToString("\n\n")))))
                            userPromptParts.clear()
                        }
                        historyProcessed = true
                    }
                    val role = if (msg.author == Author.AI) "model" else "user"
                    apiContents.add(Content(role, listOf(Part(msg.content))))
                }
            }
        }
        if (userPromptParts.isNotEmpty()) { apiContents.add(Content("user", listOf(Part(userPromptParts.joinToString("\n\n"))))) }
        val mergedContents = mutableListOf<Content>()
        if (apiContents.isNotEmpty()) {
            var currentRole = apiContents.first().role
            val currentParts = mutableListOf<String>()
            apiContents.forEach { content ->
                if (content.role == currentRole) {
                    currentParts.add(content.parts.first().text)
                } else {
                    mergedContents.add(Content(currentRole, listOf(Part(currentParts.joinToString("\n\n")))))
                    currentRole = content.role
                    currentParts.clear()
                    currentParts.add(content.parts.first().text)
                }
            }
            mergedContents.add(Content(currentRole, listOf(Part(currentParts.joinToString("\n\n")))))
        }
        return mergedContents
    }
    fun sendMessage(message: String) {
        if (_state.value.isProcessing) return
        val fullPromptMessages = buildPromptMessages(message)
        val apiRequestContents = convertChatToApiContents(fullPromptMessages)
        _state.update { it.copy(chatHistory = fullPromptMessages, isProcessing = true) }
        coroutineScope.launch {
            val responseContent = gateway.generateContent(apiKey, _state.value.selectedModel, apiRequestContents)
            val aiResponse = ChatMessage(Author.AI, responseContent, "AI")
            _state.update { it.copy(chatHistory = it.chatHistory + aiResponse, isProcessing = false) }
        }
    }
    private fun loadCatalogue() {
        try {
            val catalogueFile = File("framework/holon_catalogue.json")
            val catalogueJson = catalogueFile.readText()
            val parsedCatalogue = jsonParser.decodeFromString<HolonCatalogueFile>(catalogueJson)
            _state.update { it.copy(holonCatalogue = parsedCatalogue.holon_catalogue, gatewayStatus = GatewayStatus.OK) }
        } catch (e: Exception) {
            e.printStackTrace()
            _state.update { it.copy(gatewayStatus = GatewayStatus.ERROR) }
        }
    }
    fun toggleSystemMessageVisibility() { _state.update { it.copy(isSystemVisible = !it.isSystemVisible) } }
    private fun loadAvailableModels() { coroutineScope.launch {
        val models = gateway.listModels(apiKey)
        if (models.isNotEmpty()) { _state.update { it.copy(availableModels = models.map { m -> m.name.removePrefix("models/") }) }
        } else { _state.update { it.copy(availableModels = listOf("gemini-1.5-pro-latest", "gemini-1.5-flash-latest")) } }
    } }
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