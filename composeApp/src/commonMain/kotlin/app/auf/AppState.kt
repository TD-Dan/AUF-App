package app.auf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HolonCatalogueFile(
    val holon_catalogue: List<HolonHeader>
)

// MODIFIED: Final, lean state model. "Me" (aiPersonaId) vs "The World" (contextualHolonIds).
data class AppState(
    val holonCatalogue: List<HolonHeader> = emptyList(),
    val catalogueFilter: String? = null,
    val activeHolons: Map<String, Holon> = emptyMap(),

    // --- The "Me" ---
    val aiPersonaId: String? = null,
    // --- The "World" ---
    val contextualHolonIds: Set<String> = emptySet(),

    val inspectedHolonId: String? = null,
    val chatHistory: List<ChatMessage> = emptyList(),
    val isSystemVisible: Boolean = false,
    val gatewayStatus: GatewayStatus = GatewayStatus.IDLE,
    val isProcessing: Boolean = false,
    val availableModels: List<String> = emptyList(),
    val selectedModel: String = "gemini-1.5-flash-latest"
)

data class ChatMessage(
    val author: Author,
    val content: String,
    val title: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Author {
    USER, AI, SYSTEM
}

enum class GatewayStatus {
    OK, IDLE, ERROR
}

@Serializable
data class Holon(
    val header: HolonHeader,
    val content: String
)

@Serializable
data class HolonHeader(
    val id: String,
    val type: String,
    val name: String,
    val summary: String,
    @SerialName("file_path")
    val filePath: String
)