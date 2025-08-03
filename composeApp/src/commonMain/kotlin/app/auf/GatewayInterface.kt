package app.auf

/**
 * Defines the contract for any AI gateway, ensuring the application
 * remains decoupled from any specific LLM provider.
 */
interface GatewayInterface {
    /**
     * Sends a prompt to the configured AI model and returns the response.
     * @param prompt The user's input message.
     * @param model The specific model to use for this request.
     * @return A ChatMessage object containing the AI's response.
     */
    // --- CHANGED to include the model parameter, fixing the error ---
    suspend fun ask(prompt: String, model: String): ChatMessage
}