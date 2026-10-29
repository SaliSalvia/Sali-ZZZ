package com.salizzai.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salizzai.app.SaliZZaiApp
import com.salizzai.app.data.model.AvailableModels
import com.salizzai.app.ui.theme.SaliPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val app = SaliZZaiApp.get()
    val prefs = app.userPrefs
    val zaiClient = app.zaiClient
    val scope = rememberCoroutineScope()

    val theme by prefs.theme.collectAsState(initial = "dark")
    val model by prefs.model.collectAsState(initial = "glm-4.6")
    val temperature by prefs.temperature.collectAsState(initial = 0.7f)
    val thinkingEnabled by prefs.thinkingEnabled.collectAsState(initial = false)
    val systemPrompt by prefs.systemPrompt.collectAsState(initial = "")

    var apiKey by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("https://api.z.ai/api/v1") }
    var hasApiKey by remember { mutableStateOf(zaiClient.hasCredentials()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("تنظیمات") }) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Z.ai API config section
            SettingsSection(title = "حساب Z.ai") {
                if (hasApiKey) {
                    Text("✓ API Key تنظیم شده است", color = SaliPurple)
                    Spacer(modifier = Modifier.height(8.dp))
                    SaveButton("حذف API Key") {
                        scope.launch {
                            zaiClient.clearCredentials()
                            hasApiKey = false
                        }
                    }
                } else {
                    Text(
                        "برای کارکرد چت و تحلیل، نیاز به Z.ai API Key دارید",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("Z.ai API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SaveButton("ذخیره") {
                        scope.launch {
                            zaiClient.setCredentials(baseUrl, apiKey, null, null)
                            hasApiKey = true
                        }
                    }
                }
            }

            // Theme & language
            SettingsSection(title = "ظاهر") {
                Text("تم: $theme")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SaveButton("روشن") {
                        scope.launch { prefs.setTheme("light") }
                    }
                    SaveButton("تیره") {
                        scope.launch { prefs.setTheme("dark") }
                    }
                }
            }

            // AI Model
            SettingsSection(title = "هوش مصنوعی") {
                Text("مدل: $model")
                AvailableModels.ALL.forEach { m ->
                    SaveButton(m.displayName) {
                        scope.launch { prefs.setModel(m.id) }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("تفکر عمیق (Reasoning)", modifier = Modifier.weight(1f))
                    Switch(
                        checked = thinkingEnabled,
                        onCheckedChange = { v ->
                            scope.launch { prefs.setThinkingEnabled(v) }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("دما: ${"%.1f".format(temperature)}")
                Slider(
                    value = temperature,
                    onValueChange = { v ->
                        scope.launch { prefs.setTemperature(v) }
                    },
                    valueRange = 0f..2f
                )
            }

            // System Prompt
            SettingsSection(title = "دستورالعمل سیستم") {
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { v ->
                        scope.launch { prefs.setSystemPrompt(v) }
                    },
                    label = { Text("دستورالعمل سفارشی") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            }

            // About
            SettingsSection(title = "درباره") {
                Text("Sali-ZZai v1.0.0")
                Text("اپ نیتیو اندروید برای Z.ai")
                Text("تمام داده‌ها به‌صورت محلی روی دستگاه ذخیره می‌شوند")
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SaveButton(text: String, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text(text, color = SaliPurple)
    }
}
