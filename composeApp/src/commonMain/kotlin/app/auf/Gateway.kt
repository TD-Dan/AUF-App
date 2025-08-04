package app.auf

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class Gateway {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // MODIFIED: The function now accepts a List<Content> instead of a raw String.
    // This aligns the gateway with the StateManager's new prompt building logic.
    suspend fun generateContent(apiKey: String, model: String, contents: List<Content>): String {
        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
        // MODIFIED: The request body is now created directly from the passed-in contents.
        val requestBody = GenerateContentRequest(contents = contents)
        try {
            val response: GenerateContentResponse = client.post(apiUrl) {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            // The rest of the response parsing logic remains correct.
            return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: response.promptFeedback?.blockReason?.let { "Blocked: $it" }
                ?: "No content received."
        } catch (e: Exception) {
            println("API Call Failed: ${e.message}")
            e.printStackTrace()
            return "Error: Could not connect to the API."
        }
    }

    suspend fun listModels(apiKey: String): List<ModelInfo> {
        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models"
        return try {
            client.get(apiUrl) {
                parameter("key", apiKey)
            }.body<ListModelsResponse>().models
        } catch (e: Exception) {
            println("Failed to fetch models: ${e.message}")
            emptyList()
        }
    }
}