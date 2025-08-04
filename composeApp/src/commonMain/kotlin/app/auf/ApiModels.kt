package app.auf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Request Models ---

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

// --- Response Models (for GenerateContent) ---

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>?,
    // MODIFIED: This field is not present on successful responses.
    // Making it nullable with a default value of null fixes the crash.
    @SerialName("promptFeedback")
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String?,
    val safetyRatings: List<SafetyRating> = emptyList()
)

@Serializable
data class SafetyRating(
    val category: String,
    val probability: String
)

@Serializable
data class PromptFeedback(
    val blockReason: String?,
    val safetyRatings: List<SafetyRating>
)


// --- Response Models (for ListModels) ---

@Serializable
data class ListModelsResponse(
    val models: List<ModelInfo>
)

@Serializable
data class ModelInfo(
    val name: String,
    val displayName: String?,
    // MODIFIED: This field is not present on all models.
    // Making it nullable with a default value of null fixes the crash.
    val description: String? = null,
    @SerialName("version")
    val version: String
)