package com.example.hydrate.ui.screens.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardOptions
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val isAlarmEnabled by viewModel.isAlarmEnabled.collectAsState()
    val dailyGoal by viewModel.dailyGoalMl.collectAsState()
    val glassSize by viewModel.glassSizeMl.collectAsState()
    val startTime by viewModel.startTimeMins.collectAsState()
    val endTime by viewModel.endTimeMins.collectAsState()
    val interval by viewModel.intervalMins.collectAsState()
    
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) viewModel.setAlarmEnabled(true)
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Alarm Toggle ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Alarms", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(
                    checked = isAlarmEnabled,
                    onCheckedChange = { enabled ->
                        val needsNotificationPermission =
                            enabled &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED

                        if (needsNotificationPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.setAlarmEnabled(enabled)
                        }
                    }
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            ) {
                Text("Grant Exact Alarm Permission (Android 12+)")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // --- Water Goal Section ---
        Text("Water Goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                CustomNumberSetting(
                    label = "Daily Goal",
                    valueText = "${dailyGoal / 1000f} L (${dailyGoal} ml)",
                    unitLabel = "ml",
                    currentValue = dailyGoal,
                    onSave = viewModel::setDailyGoal
                )
                Spacer(modifier = Modifier.height(16.dp))
                DropdownSetting(
                    label = "Glass Size",
                    valueText = "$glassSize ml",
                    options = listOf(100, 150, 200, 250, 300, 500),
                    optionLabels = listOf("100 ml", "150 ml", "200 ml", "250 ml", "300 ml", "500 ml"),
                    onSelect = { viewModel.setGlassSize(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Schedule Section ---
        Text("Alarm Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                TimeSetting(
                    label = "Start Time",
                    minsFromMidnight = startTime,
                    onTimeSelected = { viewModel.setStartTime(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TimeSetting(
                    label = "End Time",
                    minsFromMidnight = endTime,
                    onTimeSelected = { viewModel.setEndTime(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomNumberSetting(
                    label = "Interval",
                    valueText = formatInterval(interval),
                    unitLabel = "minutes",
                    currentValue = interval,
                    onSave = viewModel::setInterval
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Developer Tests", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.testAlarmNow() }, modifier = Modifier.weight(1f)) {
                Text("Test in 1s")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.testAlarmIn10Seconds() }, modifier = Modifier.weight(1f)) {
                Text("Test in 10s")
            }
        }
    }
}

@Composable
fun CustomNumberSetting(
    label: String,
    valueText: String,
    unitLabel: String,
    currentValue: Int,
    onSave: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(valueText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }

    if (showDialog) {
        PositiveNumberDialog(
            title = label,
            unitLabel = unitLabel,
            initialValue = currentValue,
            onDismiss = { showDialog = false },
            onSave = {
                onSave(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun PositiveNumberDialog(
    title: String,
    unitLabel: String,
    initialValue: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var valueText by rememberSaveable(initialValue) { mutableStateOf(initialValue.toString()) }
    var hasError by rememberSaveable(initialValue) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("Enter any positive whole number in $unitLabel.")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = valueText,
                    onValueChange = {
                        valueText = it.filter(Char::isDigit)
                        hasError = false
                    },
                    label = { Text(unitLabel) },
                    singleLine = true,
                    isError = hasError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (hasError) {
                    Text(
                        "Enter a number greater than zero.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = valueText.toIntOrNull()
                    if (value == null || value <= 0) {
                        hasError = true
                    } else {
                        onSave(value)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun <T> DropdownSetting(
    label: String,
    valueText: String,
    options: List<T>,
    optionLabels: List<String>,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(valueText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(optionLabels[index]) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TimeSetting(
    label: String,
    minsFromMidnight: Int,
    onTimeSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val hour = minsFromMidnight / 60
    val minute = minsFromMidnight % 60
    
    val timeText = String.format(
        Locale.getDefault(), "%02d:%02d %s",
        if (hour % 12 == 0) 12 else hour % 12, 
        minute, 
        if (hour >= 12) "PM" else "AM"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                TimePickerDialog(context, { _, selectedHour, selectedMinute ->
                    onTimeSelected(selectedHour * 60 + selectedMinute)
                }, hour, minute, false).show()
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(timeText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

private fun formatInterval(mins: Int): String {
    return when {
        mins < 60 -> "$mins mins"
        mins == 60 -> "1 hour"
        mins % 60 == 0 -> "${mins / 60} hours"
        else -> "${mins / 60}h ${mins % 60}m"
    }
}
