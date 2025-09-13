package com.example.simplecalendar.settingpage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SettingDirectionItem(text: String, description: String, onCLick: ()->Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(0.8f)
            .clickable{onCLick()}
        ,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                fontSize = 15.sp,
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