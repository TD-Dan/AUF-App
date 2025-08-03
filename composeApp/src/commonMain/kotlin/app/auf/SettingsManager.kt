package app.auf

import kotlinx.serialization.json.Json
import java.io.File

/**
 * Manages the loading and saving of user preferences to a plain text JSON file.
 * This class handles no sensitive data.
 */
class SettingsManager(settingsDir: File) {

    private val jsonParser = Json { prettyPrint = true }
    private val settingsFile = File(settingsDir, "user_settings.json")

    init {
        // Ensure the settings directory exists on startup.
        settingsDir.mkdirs()
    }

    /**
     * Saves the provided UserSettings object to user_settings.json.
     */
    fun saveSettings(settings: UserSettings) {
        try {
            val jsonString = jsonParser.encodeToString(UserSettings.serializer(), settings)
            settingsFile.writeText(jsonString)
        } catch (e: Exception) {
            println("Error saving settings: ${e.message}")
        }
    }

    /**
     * Loads UserSettings from user_settings.json.
     * Returns null if the file doesn't exist or is corrupt, allowing the app to use defaults.
     */
    fun loadSettings(): UserSettings? {
        if (!settingsFile.exists()) return null

        return try {
            val jsonString = settingsFile.readText()
            jsonParser.decodeFromString(UserSettings.serializer(), jsonString)
        } catch (e: Exception) {
            println("Error loading settings file. It might be corrupt. Using defaults. Error: ${e.message}")
            null
        }
    }
}