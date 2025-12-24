package com.mnn.cameranote.screens.messagelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MessageScreen(onBack: () -> Unit, onDetailClick: (Long) -> Unit, viewModel: MessageViewModel = koinViewModel()) {
    val messages by viewModel.messages.collectAsStateWithLifecycle(emptyList())
    WeChatConversationList(
        messages = messages, onItemClick = onDetailClick
    )
}