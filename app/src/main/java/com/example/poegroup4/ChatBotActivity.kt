package com.example.poegroup4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.adapters.ChatAdapter
import com.example.poegroup4.models.ChatMessage

class ChatBotActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var inputMessage: EditText
    private lateinit var sendButton: Button
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_chat_bot, findViewById(R.id.content_frame))

        supportActionBar?.title = "Customer Service Bot"

        recyclerView = findViewById(R.id.recyclerChat)
        inputMessage = findViewById(R.id.etMessage)
        sendButton = findViewById(R.id.btnSend)

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        sendButton.setOnClickListener {
            val userMessage = inputMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage(userMessage, isUser = true)
                inputMessage.text.clear()
                botReply(userMessage)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun botReply(userText: String) {
        val reply = when {
            "hello" in userText.lowercase() || "hi" in userText.lowercase() -> "Hi there! How can I help you today?"

            "hi" in userText.lowercase() || "hi" in userText.lowercase() -> "Hi there! How can I help you today?"

            "help" in userText.lowercase() -> "Sure! Please tell me what you need help with."

            "balance" in userText.lowercase() -> "You can check your balance in the Overview screen."

            "emergency" in userText.lowercase() && "fund" in userText.lowercase() -> "You can view your Emergency Fund details in the Emergency Fund History screen."

            "transaction" in userText.lowercase() -> "You can view or add transactions from the Transactions screen."

            "overview" in userText.lowercase() -> "The Overview screen shows your budgets and spending by category."

            "budget" in userText.lowercase() -> "You can set and view budget goals under the Budget section."

            "how are you" in userText.lowercase() -> "I'm here and ready to help you with your finances!"

            "thanks" in userText.lowercase() || "thank you" in userText.lowercase() -> "You're welcome! Let me know if you need anything else."

            "contact" in userText.lowercase() -> "You can reach out via the support section in the app settings."

            "reset" in userText.lowercase() -> "If you'd like to reset your data, go to Settings > Reset Data. Be careful—this cannot be undone."

            "goodbye" in userText.lowercase() || "bye" in userText.lowercase() -> "Goodbye! Feel free to chat again anytime."

            "ai" in userText.lowercase() -> "Yes, I am your AI assistant! I'm here to guide you."

            "what can you do" in userText.lowercase() -> "I can help with navigating the app, understanding budgets, tracking transactions, and more!"

            "report" in userText.lowercase() -> "You can view your reports in the Reports section under Overview."

            "save" in userText.lowercase() || "saving" in userText.lowercase() -> "Saving regularly builds a strong Emergency Fund. Keep it up!"

            "goal" in userText.lowercase() -> "Setting financial goals helps you stay on track. Visit the Budget Goals section to update yours."
            else -> "I'm here to assist! Can you rephrase or give more details?"
        }
        addMessage(reply, isUser = false)
    }
}
