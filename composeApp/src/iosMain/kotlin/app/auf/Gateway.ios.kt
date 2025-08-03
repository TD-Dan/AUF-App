package app.auf

// Dummy implementation for iOS to allow the app to compile.
actual class Gateway {
    actual suspend fun ask(prompt: String, modelName: String): ChatMessage {
        return ChatMessage(Author.AI, "AI functionality is not available on this platform yet.")
    }
    actual suspend fun listModels(): List<String> {
        return listOf("iOS Dummy Model")
    }
}