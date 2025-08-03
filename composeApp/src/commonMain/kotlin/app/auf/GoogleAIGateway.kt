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

    private val model = "gemini-1.5-pro-latest"
    private val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"

    // --- Data classes for parsing the API responses ---

    // For a SUCCESSFUL response
    @Serializable
    private data class GeminiSuccessResponse(val candidates: List<Candidate>)
    @Serializable
    private data class Candidate(val content: Content)
    @Serializable
    private data class Content(val parts: List<Part>)
    @Serializable
    private data class Part(val text: String)

    // For an ERROR response (matches the JSON you provided)
    @Serializable
    private data class GeminiErrorResponse(val error: ApiError)
    @Serializable
    private data class ApiError(val message: String, val code: Int, val status: String)

    // For the request body
    @Serializable
    private data class GeminiRequest(val contents: List<Content>)

    override suspend fun ask(prompt: String): ChatMessage {
        try {
            val requestBody = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )

            // Get the response from the API
            val responseElement: JsonElement = httpClient.post(endpoint) {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            // 2. Check if the response is a success or an error
            if (responseElement.jsonObject.containsKey("candidates")) {
                // It's a success, parse it as such
                val successResponse = jsonParser.decodeFromJsonElement<GeminiSuccessResponse>(responseElement)
                val responseContent = successResponse.candidates.first().content.parts.first().text
                return ChatMessage(Author.AI, responseContent)

            } else if (responseElement.jsonObject.containsKey("error")) {
                // It's an error, parse it and format a user-friendly message
                val errorResponse = jsonParser.decodeFromJsonElement<GeminiErrorResponse>(responseElement)
                val errorMessage = "AI Error (${errorResponse.error.code}): ${errorResponse.error.message}"
                println(errorMessage) // Also print to console for debugging
                return ChatMessage(Author.AI, errorMessage)

            } else {
                // The response is in an unknown format
                return ChatMessage(Author.AI, "Error: Received an unknown response format from the AI gateway.")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return ChatMessage(Author.AI, "Error: Could not connect to or process response from Google AI. ${e.message}")
        }
    }
}