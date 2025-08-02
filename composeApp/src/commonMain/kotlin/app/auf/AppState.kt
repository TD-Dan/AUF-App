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
    val activeHolons: Map<String, Holon> = emptyMap(),
    val sessionTranscript: List<ChatMessage> = emptyList(),
    val gatewayStatus: GatewayStatus = GatewayStatus.DISCONNECTED,
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

enum class GatewayStatus {
    CONNECTED, DISCONNECTED, ERROR
}

@Serializable
data class Holon(
    val header: HolonHeader
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