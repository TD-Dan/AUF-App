package app.auf

import kotlinx.serialization.Serializable

/**
 * A simple data class to hold non-sensitive user preferences.
 * This object will be serialized to a plain text JSON file.
 * MODIFIED to align with the "Me vs. World" architecture.
 */
@Serializable
data class UserSettings(
    val windowWidth: Int = 1400,
    val windowHeight: Int = 900,
    val selectedModel: String = "gemini-1.5-flash-latest",
    // ADDED: A dedicated field for the selected "Me" persona.
    val selectedAiPersonaId: String? = null,
    // RENAMED: For clarity, this now holds "The World" holons.
    val activeContextualHolonIds: Set<String> = emptySet()
)