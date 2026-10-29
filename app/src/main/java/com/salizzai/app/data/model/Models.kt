package com.salizzai.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── API request models ─────────────────────────────────────────────

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String = "glm-4.6",
    val messages: List<ChatMessage>,
    @SerialName("thinking")
    val thinking: ThinkingConfig? = null,
    val temperature: Double? = null,
    val stream: Boolean = true
)

@Serializable
data class ThinkingConfig(
    val type: String = "disabled" // or "enabled"
)

// ─── API response models ────────────────────────────────────────────

@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<Choice> = emptyList(),
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int = 0,
    val message: ChoiceMessage? = null,
    val delta: Delta? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class ChoiceMessage(
    val role: String = "assistant",
    val content: String = ""
)

@Serializable
data class Delta(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0
)

// ─── Image generation ───────────────────────────────────────────────

@Serializable
data class ImageGenerationRequest(
    val prompt: String,
    val size: String = "1024x1024",
    val model: String? = null
)

@Serializable
data class ImageGenerationResponse(
    val created: Long = 0,
    val data: List<ImageData> = emptyList()
)

@Serializable
data class ImageData(
    val base64: String? = null,
    val url: String? = null
)

// ─── Web search ─────────────────────────────────────────────────────

@Serializable
data class WebSearchResult(
    val url: String,
    val name: String,
    val snippet: String = "",
    @SerialName("host_name")
    val hostName: String = "",
    val rank: Int = 0,
    val date: String = "",
    val favicon: String = ""
)

// ─── Available models ───────────────────────────────────────────────

data class ZaiModel(
    val id: String,
    val displayName: String,
    val description: String,
    val supportsThinking: Boolean = false,
    val badge: String? = null
)

object AvailableModels {
    val ALL = listOf(
        ZaiModel(
            id = "glm-4.6",
            displayName = "GLM-4.6",
            description = "جدیدترین و قدرتمندترین مدل Z.ai",
            supportsThinking = true,
            badge = "پیشنهادی"
        ),
        ZaiModel(
            id = "glm-4.5",
            displayName = "GLM-4.5",
            description = "نسخه پایدار قبلی",
            supportsThinking = true
        ),
        ZaiModel(
            id = "glm-4-plus",
            displayName = "GLM-4-Plus",
            description = "نسخه پلاس - تعادل بهینه"
        ),
        ZaiModel(
            id = "glm-4-air",
            displayName = "GLM-4-Air",
            description = "سریع و سبک",
            badge = "سریع"
        ),
        ZaiModel(
            id = "glm-4-airx",
            displayName = "GLM-4-AirX",
            description = "فوق سریع برای پاسخ‌های فوری",
            badge = "فوق سریع"
        )
    )
}

// ─── File tree models ───────────────────────────────────────────────

@Serializable
data class FileTreeNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val ext: String? = null,
    val children: List<FileTreeNode> = emptyList()
)
