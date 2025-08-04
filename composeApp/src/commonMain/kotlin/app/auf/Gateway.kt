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

    suspend fun generateContent(apiKey: String, model: String, prompt: String): String {
        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
        val requestBody = GenerateContentRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
        try {
            val response: GenerateContentResponse = client.post(apiUrl) {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()
            return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: response.promptFeedback?.blockReason?.let { "Blocked: $it" }
                ?: "No content received."
        } catch (e: Exception) {
            println("API Call Failed: ${e.message}")
            e.printStackTrace()
            return "Error: Could not connect to the API."
        }
    }

    // ADDED: New function to fetch the list of models
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