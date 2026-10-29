package com.salizzai.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.salizzai.app.feature.chat.ChatScreen
import com.salizzai.app.feature.discover.DiscoverScreen
import com.salizzai.app.feature.history.HistoryScreen
import com.salizzai.app.feature.imagegen.ImageGenScreen
import com.salizzai.app.feature.settings.SettingsScreen

sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    object Chat : Tab("chat", "چت", Icons.Filled.Message)
    object History : Tab("history", "تاریخچه", Icons.Filled.History)
    object Discover : Tab("discover", "کشف", Icons.Filled.Search)
    object ImageGen : Tab("image", "تصویر", Icons.Filled.Image)
    object Settings : Tab("settings", "تنظیمات", Icons.Filled.Settings)
}

@Composable
fun SaliZZaiAppNavigation() {
    var currentTab by remember { mutableStateOf<Tab>(Tab.Chat) }
    var pendingConversationId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Tab.Chat, Tab.History, Tab.Discover, Tab.ImageGen, Tab.Settings
                ).forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab.route == tab.route,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    ) { padding ->
        when (currentTab) {
            is Tab.Chat -> ChatScreen(
                conversationId = pendingConversationId,
                onOpenHistory = { currentTab = Tab.History },
                modifier = Modifier.padding(padding)
            )
            is Tab.History -> HistoryScreen(
                onOpenConversation = { id ->
                    pendingConversationId = id
                    currentTab = Tab.Chat
                },
                onNewChat = {
                    pendingConversationId = null
                    currentTab = Tab.Chat
                },
                modifier = Modifier.padding(padding)
            )
            is Tab.Discover -> DiscoverScreen(modifier = Modifier.padding(padding))
            is Tab.ImageGen -> ImageGenScreen(modifier = Modifier.padding(padding))
            is Tab.Settings -> SettingsScreen(modifier = Modifier.padding(padding))
        }
    }
}
