package com.example.simplecalendar.calendarpage

import android.graphics.drawable.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.R

@Composable
fun SettingDirectionItem(text: String, description: String, onCLick: ()->Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable{onCLick()}
        ,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                fontWeight = FontWeight.Light
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate to $text",
            tint = Color.Gray
        )
    }
}