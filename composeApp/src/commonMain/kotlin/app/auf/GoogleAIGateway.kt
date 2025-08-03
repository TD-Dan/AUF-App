package app.auf

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class GoogleAIGateway(private val apiKey: String) : GatewayInterface {

    private val jsonParser = Json { ignoreUnknownKeys = true; prettyPrint = true; isLenient = true }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    private val baseEndpoint = "https://generativelanguage.googleapis.com/v1beta"

    // --- Data classes for parsing API responses ---
    @Serializable private data class GeminiRequest(val contents: List<Content>)
    @Serializable private data class Content(val parts: List<Part>)
    @Serializable private data class Part(val text: String)

    @Serializable private data class GeminiSuccessResponse(val candidates: List<Candidate>)
    @Serializable private data class Candidate(val content: Content)

    @Serializable private data class GeminiErrorResponse(val error: ApiError)
    @Serializable private data class ApiError(val message: String, val code: Int, val status: String)

    // --- Data classes for listing models ---
    @Serializable private data class ListModelsResponse(val models: List<ModelInfo>)
    @Serializable private data class ModelInfo(val name: String)


    // --- Gateway Interface Implementation ---

    override suspend fun ask(prompt: String, model: String): ChatMessage {
        try {
            val endpoint = "$baseEndpoint/models/$model:generateContent"
            val requestBody = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))

            val responseElement: JsonElement = httpClient.post(endpoint) {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            // ... (The success/error handling logic remains the same)
            if (responseElement.jsonObject.containsKey("candidates")) {
                val successResponse = jsonParser.decodeFromJsonElement<GeminiSuccessResponse>(responseElement)
                val responseContent = successResponse.candidates.first().content.parts.first().text
                return ChatMessage(Author.AI, responseContent)
            } else if (responseElement.jsonObject.containsKey("error")) {
                val errorResponse = jsonParser.decodeFromJsonElement<GeminiErrorResponse>(responseElement)
                val errorMessage = "AI Error (${errorResponse.error.code}): ${errorResponse.error.message}"
                println(errorMessage)
                return ChatMessage(Author.AI, errorMessage)
            } else {
                return ChatMessage(Author.AI, "Error: Received an unknown response format from the AI gateway.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ChatMessage(Author.AI, "Error: Could not connect to or process response from Google AI. ${e.message}")
        }
    }

    suspend fun listModels(): List<String> {
        try {
            val endpoint = "$baseEndpoint/models"

            // --- DEBUGGING CHANGE ---
            // Get the raw response as a string first
            val rawJsonResponse: String = httpClient.get(endpoint) {
                parameter("key", apiKey)
            }.body()

            // Print it for us to see
            println("--- RAW JSON RESPONSE FROM listModels ---")
            println(rawJsonResponse)
            println("----------------------------------------")

            // Now, try to parse it (this will still fail, but we will see why)
            val response = jsonParser.decodeFromString<ListModelsResponse>(rawJsonResponse)
            // --- END DEBUGGING CHANGE ---

            // The API returns names like "models/gemini-1.5-pro-latest", so we clean them up.
            return response.models.map { it.name.removePrefix("models/") }
        } catch (e: Exception) {
            e.printStackTrace()
            // If the API call fails, return an empty list. The StateManager will handle the fallback.
            return emptyList()
        }
    }
}