package com.pawbyte.pawassistant

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class ChatMessage(
    val id: Long,
    val text: String,
    val isFromUser: Boolean
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(ChatUiState(messages = loadMessages()))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var activeRequestJob: Job? = null

    fun sendMessage(rawText: String) {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty() || _uiState.value.isLoading) return

        val userMessage = ChatMessage(
            id = System.currentTimeMillis(),
            text = trimmed,
            isFromUser = true
        )

        val updatedMessages = _uiState.value.messages + userMessage
        saveMessages(updatedMessages)

        _uiState.update {
            it.copy(
                messages = updatedMessages,
                isLoading = true,
                errorMessage = null
            )
        }

        val contextPayload = updatedMessages.map { message ->
            AiContextMessage(
                role = if (message.isFromUser) "user" else "model",
                text = message.text
            )
        }

        activeRequestJob = viewModelScope.launch {
            val result = AiService.sendConversation(contextPayload)
            result.fold(
                onSuccess = { replyText ->
                    val assistantMessage = ChatMessage(
                        id = System.currentTimeMillis(),
                        text = replyText,
                        isFromUser = false
                    )
                    val withReply = _uiState.value.messages + assistantMessage
                    saveMessages(withReply)
                    _uiState.update {
                        it.copy(
                            messages = withReply,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                                ?: "Yanıt alınırken bir hata oluştu."
                        )
                    }
                }
            )
        }
    }

    fun startNewChat() {
        activeRequestJob?.cancel()
        activeRequestJob = null
        prefs.edit().remove(KEY_CHAT_HISTORY).apply()
        _uiState.value = ChatUiState()
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun saveMessages(messages: List<ChatMessage>) {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(
                JSONObject().apply {
                    put("id", message.id)
                    put("text", message.text)
                    put("isFromUser", message.isFromUser)
                }
            )
        }
        prefs.edit().putString(KEY_CHAT_HISTORY, array.toString()).apply()
    }

    private fun loadMessages(): List<ChatMessage> {
        val raw = prefs.getString(KEY_CHAT_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val text = obj.optString("text", "")
                    if (text.isNotBlank()) {
                        add(
                            ChatMessage(
                                id = obj.optLong("id", i.toLong()),
                                text = text,
                                isFromUser = obj.optBoolean("isFromUser", true)
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val PREFS_NAME = "paw_assistant_prefs"
        const val KEY_CHAT_HISTORY = "chat_history"
    }
}
