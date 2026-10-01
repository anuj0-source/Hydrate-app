package com.example.hydrate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hydrate.R

@Composable
fun BunnyIllustration(modifier: Modifier = Modifier.size(150.dp)) {
    Image(
        painter = painterResource(id = R.drawable.ic_bunny_logo),
        contentDescription = "Cute Bunny",
        modifier = modifier
    )
}
