package com.example.hydrate.ui.screens.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydrate.ui.theme.WhiteCard

@Composable
fun MessagesScreen(viewModel: MessagesViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val selected by viewModel.selectedMessageText.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newMessageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        Text("Messages 💌", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Pick a cute message for your water reminders 💕", style = MaterialTheme.typography.bodyLarge)
        
        Spacer(Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages, key = { it.id }) { msg ->
                val isSelected = msg.text == selected
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { viewModel.selectMessage(msg.text) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else WhiteCard
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(msg.icon, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                msg.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "✨ Selected for alarm",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.deleteMessage(msg) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (showAddDialog) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = WhiteCard),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("New Message", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newMessageText,
                        onValueChange = { newMessageText = it },
                        placeholder = { Text("Enter a cute message...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Row {
                        Button(
                            onClick = {
                                if (newMessageText.isNotBlank()) {
                                    viewModel.addMessage(newMessageText)
                                    newMessageText = ""
                                    showAddDialog = false
                                }
                            },
                            shape = CircleShape
                        ) {
                            Text("Save")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { showAddDialog = false },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("+ Add Custom Message 💌", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
