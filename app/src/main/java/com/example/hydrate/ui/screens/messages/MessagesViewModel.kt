package com.example.hydrate.ui.screens.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrate.data.db.HydrateDatabase
import com.example.hydrate.data.model.CustomMessage
import com.example.hydrate.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MessagesViewModel(application: Application) : AndroidViewModel(application) {
    private val db = HydrateDatabase.getDatabase(application)
    private val repo = SettingsRepository(application)

    val messages = db.hydrateDao().getAllCustomMessages().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val selectedMessageText = repo.selectedMessage.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    fun selectMessage(text: String) {
        viewModelScope.launch {
            repo.setSelectedMessage(text)
        }
    }

    fun addMessage(text: String, icon: String = "💕") {
        viewModelScope.launch {
            db.hydrateDao().insertCustomMessage(CustomMessage(text = text, icon = icon))
        }
    }
    
    fun deleteMessage(msg: CustomMessage) {
        viewModelScope.launch {
            db.hydrateDao().deleteCustomMessage(msg)
        }
    }
}
