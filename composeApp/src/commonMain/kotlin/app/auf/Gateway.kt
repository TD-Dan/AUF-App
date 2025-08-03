package app.auf

// This is the contract that all platforms must fulfill.
expect class Gateway() {
    suspend fun ask(prompt: String, modelName: String): ChatMessage
    suspend fun listModels(): List<String>
}