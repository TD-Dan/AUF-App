package app.auf

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class Gateway {

    // 1. Create a single, reusable Ktor HttpClient instance.
    //    This is more efficient than creating a new client for every request.
    private val client = HttpClient {
        // 2. Install the ContentNegotiation plugin.
        //    This plugin automatically serializes our request data classes to JSON
        //    and deserializes the JSON responses back into our response data classes.
        install(ContentNegotiation) {
            json(Json {
                // Configure the JSON parser to be lenient and ignore unknown keys,
                // which makes our client more robust against API changes.
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // A placeholder for our future generateContent function.
    // We will build this out next. It's a suspend function because Ktor's
    // network calls are asynchronous and non-blocking.
    suspend fun generateContent(prompt: String): String {
        // TODO: Implement the actual REST API call to Google AI
        return "This is a placeholder response for the prompt: $prompt"
    }

    // We can add other functions here for different API endpoints as needed.
}