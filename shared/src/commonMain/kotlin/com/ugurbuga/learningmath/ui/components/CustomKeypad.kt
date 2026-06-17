package com.ugurbuga.learningmath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onTickClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("del", "0", "tick")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { item ->
                    KeypadButton(
                        text = item,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (item) {
                                "del" -> onDeleteClick()
                                "tick" -> onTickClick()
                                else -> onNumberClick(item)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val buttonColor = when (text) {
        "tick" -> Color(0xFF4CAF50)
        "del" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when (text) {
        "tick", "del" -> Color.White
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = modifier
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(16.dp))
            .background(buttonColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (text) {
            "del" -> Icon(Icons.Default.Delete, contentDescription = "Delete", tint = contentColor)
            "tick" -> Icon(Icons.Default.Check, contentDescription = "Correct", tint = contentColor)
            else -> Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
