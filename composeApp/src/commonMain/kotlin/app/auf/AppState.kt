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
    // This new property will hold the currently selected filter type.
    val catalogueFilter: String? = null,
    val activeHolons: Map<String, Holon> = emptyMap(),
    val activeHolonId: String? = null,
    val sessionTranscript: List<ChatMessage> = emptyList(),
    // Default state is now IDLE.
    val gatewayStatus: GatewayStatus = GatewayStatus.IDLE,
    val isProcessing: Boolean = false
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