package app.auf

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import kotlin.time.Duration.Companion.seconds

// This is the actual implementation for the JVM target.
actual class Gateway {
    private val requestOptions = RequestOptions(timeout = 100.seconds)

    // Note: You need to provide your API key here.
    // In a real app, this would be passed in securely.
    private val apiKey = "YOUR_API_KEY_HERE" // Replace with your key logic if needed

    private val proModel = GenerativeModel(
        modelName = "gemini-1.5-pro-latest",
        apiKey = apiKey,
        requestOptions = requestOptions
    )
    private val flashModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = apiKey,
        requestOptions = requestOptions
    )

    actual suspend fun ask(prompt: String, modelName: String): ChatMessage {
        val modelToUse = when (modelName) {
            "gemini-1.5-pro-latest" -> proModel
            "gemini-1.5-flash-latest" -> flashModel
            else -> proModel
        }
        return try {
            val response = modelToUse.generateContent(content { text(prompt) })
            ChatMessage(Author.AI, response.text ?: "Error: Empty response from AI.")
        } catch (e: Exception) {
            println("Error: Could not connect to or process response from Google AI. ${e.message}")
            ChatMessage(Author.AI, "Error: Could not connect to or process response from Google AI. ${e.message}")
        }
    }

    actual suspend fun listModels(): List<String> {
        return listOf("gemini-1.5-pro-latest", "gemini-1.5-flash-latest")
    }
}