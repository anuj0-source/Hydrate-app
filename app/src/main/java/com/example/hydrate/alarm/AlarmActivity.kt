package com.example.hydrate.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import com.example.hydrate.ui.components.BunnyIllustration
import com.example.hydrate.ui.theme.HydrateTheme
import com.example.hydrate.ui.theme.PastelBlushPink

class AlarmActivity : ComponentActivity() {
    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Wake up screen and show over keyguard/lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }

        enableEdgeToEdge()
        setContent {
            HydrateTheme {
                val message by viewModel.customMessage.collectAsState()
                
                AlarmScreen(
                    message = message,
                    onDrinkWater = {
                        stopAlarmService()
                        viewModel.drinkWater {
                            finish()
                        }
                    },
                    onSnooze = {
                        stopAlarmService()
                        viewModel.snooze {
                            finish()
                        }
                    }
                )
            }
        }
    }

    private fun stopAlarmService() {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
        }
        startService(stopIntent)
    }
}

@Composable
fun AlarmScreen(message: String, onDrinkWater: () -> Unit, onSnooze: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelBlushPink),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = message,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            BunnyIllustration()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Take a little sip for me 🥺")
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(onClick = onDrinkWater) {
                Text("DRINK WATER 💧")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onSnooze) {
                Text("Snooze 5 minutes ⏰")
            }
        }
    }
}
