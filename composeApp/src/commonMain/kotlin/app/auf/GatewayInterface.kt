package app.auf

/**
 * Defines the contract for any AI gateway, ensuring the application
 * remains decoupled from any specific LLM provider.
 */
interface GatewayInterface {
    /**
     * Sends a prompt to the configured AI model and returns the response.
     * @param prompt The user's input message.
     * @return A ChatMessage object containing the AI's response.
     */
    suspend fun ask(prompt: String): ChatMessage
}