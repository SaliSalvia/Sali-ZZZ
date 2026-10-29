package com.salizzai.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salizzai.app.SaliZZaiApp
import com.salizzai.app.data.api.ZaiApiClient
import com.salizzai.app.data.db.AppDatabase
import com.salizzai.app.data.db.entity.Attachment
import com.salizzai.app.data.db.entity.Conversation
import com.salizzai.app.data.db.entity.Message
import com.salizzai.app.data.model.ChatCompletionRequest
import com.salizzai.app.data.model.ChatMessage
import com.salizzai.app.data.model.FileTreeNode
import com.salizzai.app.data.model.ThinkingConfig
import com.salizzai.app.data.prefs.UserPreferences
import com.salizzai.app.util.ZipExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID

data class ChatUiState(
    val conversationId: String? = null,
    val conversationTitle: String = "گفتگوی جدید",
    val messages: List<Message> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val isStreaming: Boolean = false,
    val error: String? = null,
    val draftInput: String = "",
    val model: String = "glm-4.6",
    val thinkingEnabled: Boolean = false,
    val temperature: Float = 0.7f,
    val systemPrompt: String = ""
)

class ChatViewModel(
    private val database: AppDatabase,
    private val zaiClient: ZaiApiClient,
    private val userPrefs: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Observe user preferences and update state
    init {
        viewModelScope.launch {
            userPrefs.model.collectLatest { m ->
                _uiState.update { it.copy(model = m) }
            }
        }
        viewModelScope.launch {
            userPrefs.temperature.collectLatest { t ->
                _uiState.update { it.copy(temperature = t) }
            }
        }
        viewModelScope.launch {
            userPrefs.thinkingEnabled.collectLatest { e ->
                _uiState.update { it.copy(thinkingEnabled = e) }
            }
        }
        viewModelScope.launch {
            userPrefs.systemPrompt.collectLatest { p ->
                _uiState.update { it.copy(systemPrompt = p) }
            }
        }
    }

    fun setConversationId(id: String?) {
        _uiState.update { it.copy(conversationId = id) }
        if (id != null) {
            viewModelScope.launch {
                val conv = database.conversationDao().getById(id)
                if (conv != null) {
                    _uiState.update { it.copy(conversationTitle = conv.title, model = conv.model) }
                }
                val msgs = database.messageDao().getForConversation(id)
                _uiState.update { it.copy(messages = msgs) }
                val atts = database.attachmentDao().getForConversation(id)
                _uiState.update { it.copy(attachments = atts) }
            }
        } else {
            _uiState.update { it.copy(messages = emptyList(), attachments = emptyList(), conversationTitle = "گفتگوی جدید") }
        }
    }

    fun updateDraftInput(text: String) {
        _uiState.update { it.copy(draftInput = text) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun newChat() {
        setConversationId(null)
    }

    suspend fun processZipFile(filename: String, mimeType: String, bytes: ByteArray): Attachment {
        return withContext(Dispatchers.IO) {
            val result = ZipExtractor.extract(bytes)
            val fileTreeJson = Json.encodeToString(
                ListSerializer(FileTreeNode.serializer()),
                result.fileTree
            )
            val attachment = Attachment(
                id = UUID.randomUUID().toString(),
                messageId = null,
                conversationId = _uiState.value.conversationId,
                filename = "zip_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.zip",
                originalName = filename,
                mimeType = mimeType,
                size = bytes.size.toLong(),
                fileType = "zip",
                extractedText = result.extractedText,
                fileTree = fileTreeJson,
                fileCount = result.fileCount,
                totalSize = result.totalSize
            )
            database.attachmentDao().insert(attachment)
            _uiState.update { state ->
                state.copy(attachments = state.attachments + attachment)
            }
            attachment
        }
    }

    suspend fun processRegularFile(filename: String, mimeType: String, bytes: ByteArray): Attachment {
        return withContext(Dispatchers.IO) {
            val isText = filename.substringAfterLast(".").lowercase() in setOf(
                "txt", "md", "json", "js", "ts", "tsx", "jsx", "py", "java", "kt"
            )
            val extractedText = if (isText && bytes.size < 1024 * 1024) {
                val content = bytes.toString(Charsets.UTF_8)
                val truncated = if (content.length > 8000) content.take(8000) + "\n... [متوقف شد]" else content
                "### `$filename`\n\n```\n$truncated\n```\n"
            } else null

            val attachment = Attachment(
                id = UUID.randomUUID().toString(),
                messageId = null,
                conversationId = _uiState.value.conversationId,
                filename = "${System.currentTimeMillis()}_$filename",
                originalName = filename,
                mimeType = mimeType,
                size = bytes.size.toLong(),
                fileType = "file",
                extractedText = extractedText,
                fileCount = 1,
                totalSize = bytes.size.toLong()
            )
            database.attachmentDao().insert(attachment)
            _uiState.update { state ->
                state.copy(attachments = state.attachments + attachment)
            }
            attachment
        }
    }

    fun removeAttachment(id: String) {
        viewModelScope.launch {
            database.attachmentDao().deleteById(id)
            _uiState.update { state ->
                state.copy(attachments = state.attachments.filterNot { it.id == id })
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() && _uiState.value.attachments.isEmpty()) return
        if (_uiState.value.isStreaming) return

        // Capture the data we need at the moment of send (these are immutable values)
        val initialAttachments = _uiState.value.attachments
        val userMsgContent = text
        val attachmentIds = initialAttachments.map { it.id }
        val attachmentContext = initialAttachments
            .filter { !it.extractedText.isNullOrEmpty() }
            .joinToString("\n\n") { att ->
                "--- فایل پیوست: ${att.originalName} (${if (att.fileType == "zip") "شامل ${att.fileCount} فایل" else "تک فایل"}) ---\n${att.extractedText}\n--- پایان ---"
            }

        viewModelScope.launch {
            // Read latest state INSIDE the coroutine to get updated prefs
            val currentState = _uiState.value

            // Create conversation if not exists
            var convId = currentState.conversationId
            if (convId == null) {
                convId = UUID.randomUUID().toString()
                val conv = Conversation(
                    id = convId,
                    title = userMsgContent.take(50).ifBlank { "گفتگوی جدید" },
                    model = currentState.model
                )
                database.conversationDao().insert(conv)
                _uiState.update { it.copy(conversationId = convId, conversationTitle = conv.title) }
            }

            // Save user message
            val userMsgId = UUID.randomUUID().toString()
            val userMsg = Message(
                id = userMsgId,
                conversationId = convId,
                role = "user",
                content = userMsgContent
            )
            database.messageDao().insert(userMsg)

            // Link attachments to user message
            if (attachmentIds.isNotEmpty()) {
                database.attachmentDao().linkToMessage(attachmentIds, userMsgId)
                _uiState.update { it.copy(attachments = emptyList()) }
            }

            _uiState.update {
                it.copy(messages = it.messages + userMsg, draftInput = "")
            }

            // Add placeholder for assistant message
            val assistantMsgId = UUID.randomUUID().toString()
            val assistantMsg = Message(
                id = assistantMsgId,
                conversationId = convId,
                role = "assistant",
                content = "",
                model = currentState.model
            )
            database.messageDao().insert(assistantMsg)
            _uiState.update {
                it.copy(messages = it.messages + assistantMsg, isStreaming = true)
            }

            // Build messages array for Z.ai API
            val historyMessages = mutableListOf<ChatMessage>()
            if (currentState.systemPrompt.isNotBlank()) {
                historyMessages.add(ChatMessage("system", currentState.systemPrompt))
            }
            historyMessages.add(ChatMessage("system", "تو دستیار هوشمند Z.ai هستی. به زبان کاربر (فارسی یا انگلیسی) پاسخ بده. برای تحلیل فایل‌های ZIP از کد و ساختار استخراج‌شده استفاده کن و تحلیل عمیق ارائه بده. اگر چند نسخه از یک پروژه آپلود شده، تفاوت‌ها و بهبودها را به دقت مقایسه کن. از Markdown برای قالب‌بندی پاسخ استفاده کن."))
            // Add previous messages for context
            currentState.messages.takeLast(10).forEach { msg ->
                historyMessages.add(ChatMessage(msg.role, msg.content))
            }
            // Add current user message with attachment context
            val finalContent = if (attachmentContext.isNotBlank()) {
                "$userMsgContent\n\n[محتوای فایل‌های پیوست برای تحلیل]:\n$attachmentContext"
            } else userMsgContent
            historyMessages.add(ChatMessage("user", finalContent))

            val request = ChatCompletionRequest(
                model = currentState.model,
                messages = historyMessages,
                thinking = ThinkingConfig(if (currentState.thinkingEnabled) "enabled" else "disabled"),
                temperature = currentState.temperature.toDouble(),
                stream = true
            )

            // Stream response
            try {
                zaiClient.streamChatCompletion(request).collectLatest { event ->
                    when (event) {
                        is ZaiApiClient.StreamEvent.Delta -> {
                            database.messageDao().appendContent(assistantMsgId, event.content)
                            _uiState.update { state ->
                                state.copy(
                                    messages = state.messages.map { msg ->
                                        if (msg.id == assistantMsgId) msg.copy(content = msg.content + event.content)
                                        else msg
                                    }
                                )
                            }
                        }
                        is ZaiApiClient.StreamEvent.Done -> {
                            database.messageDao().markError(assistantMsgId, false)
                            _uiState.update { it.copy(isStreaming = false) }
                            database.conversationDao().touch(convId)
                        }
                        is ZaiApiClient.StreamEvent.Error -> {
                            val errorContent = "❌ خطا: ${event.message}"
                            database.messageDao().updateContent(assistantMsgId, errorContent)
                            database.messageDao().markError(assistantMsgId, true)
                            _uiState.update {
                                it.copy(
                                    isStreaming = false,
                                    error = event.message,
                                    messages = it.messages.map { msg ->
                                        if (msg.id == assistantMsgId) msg.copy(content = errorContent, isError = true)
                                        else msg
                                    }
                                )
                            }
                        }
                        is ZaiApiClient.StreamEvent.Open -> {
                            // Stream opened
                        }
                    }
                }
            } catch (e: Exception) {
                val errorContent = "❌ خطای غیرمنتظره: ${e.message}"
                database.messageDao().updateContent(assistantMsgId, errorContent)
                database.messageDao().markError(assistantMsgId, true)
                _uiState.update {
                    it.copy(
                        isStreaming = false,
                        error = e.message,
                        messages = it.messages.map { msg ->
                            if (msg.id == assistantMsgId) msg.copy(content = errorContent, isError = true)
                            else msg
                        }
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SaliZZaiApp.get()
                return ChatViewModel(app.database, app.zaiClient, app.userPrefs) as T
            }
        }
    }
}
