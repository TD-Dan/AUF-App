package app.auf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This new class exactly matches the structure of holon_catalogue.json
@Serializable
data class HolonCatalogueFile(
    val holon_catalogue: List<HolonHeader>
)

data class AppState(
    val holonCatalogue: List<HolonHeader> = emptyList(),
    val catalogueFilter: String? = null,
    val activeHolons: Map<String, Holon> = emptyMap(),
    val activeHolonIds: Set<String> = emptySet(),
    // --- ADDED to track the single holon being inspected ---
    val inspectedHolonId: String? = null,
    val sessionTranscript: List<ChatMessage> = emptyList(),
    val gatewayStatus: GatewayStatus = GatewayStatus.IDLE,
    val isProcessing: Boolean = false,
    // --- CHANGED for dynamic model loading ---
    val availableModels: List<String> = emptyList(), // Starts empty
    val selectedModel: String = "gemini-1.5-flash-latest" // Still defaults to a safe, cheap model
)

data class ChatMessage(
    val author: Author,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Author {
    USER, AI
}

// Corrected Enum
enum class GatewayStatus {
    OK, IDLE, ERROR
}

// The Holon class will now hold its content as well as its header.
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
    // This annotation tells the parser to look for "file_path" in the JSON
    // and map it to our "filePath" property.
    @SerialName("file_path")
    val filePath: String
)