package app.auf

import kotlinx.serialization.Serializable

/**
 * A simple data class to hold non-sensitive user preferences.
 * This object will be serialized to a plain text JSON file.
 */
@Serializable
data class UserSettings(
    val windowWidth: Int = 1400,
    val windowHeight: Int = 900,
    val selectedModel: String = "gemini-1.5-flash-latest",
    val activeHolonIds: Set<String> = emptySet()
)