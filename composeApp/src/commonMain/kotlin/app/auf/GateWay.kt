package app.auf

/*import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage as OpenAIChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
*/
class Gateway(apiKey: String) {
/*
    // The OpenAI client is now initialized with the key passed to the constructor.
    private val openAI = OpenAI(apiKey)

    suspend fun ask(prompt: String): ChatMessage {
        try {
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = listOf(
                    OpenAIChatMessage(
                        role = ChatRole.User,
                        content = prompt
                    )
                )
            )

            val completion = openAI.chatCompletion(chatCompletionRequest)
            val responseContent = completion.choices.first().message.content ?: "No response content."

            return ChatMessage(Author.AI, responseContent)
        } catch (e: Exception) {
            e.printStackTrace()
            return ChatMessage(Author.AI, "Error: Could not connect to the AI. ${e.message}")
        }
    }*/
}