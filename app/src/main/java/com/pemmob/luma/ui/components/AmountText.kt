package com.pemmob.luma.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pemmob.luma.ui.theme.LUMATheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    isIncome: Boolean? = null,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val color = when (isIncome) {
        true -> MaterialTheme.colorScheme.secondary // Mapped ke Emerald di LightColorScheme Saku
        false -> MaterialTheme.colorScheme.tertiary // Mapped ke Coral/Red
        null -> MaterialTheme.colorScheme.onSurface
    }
    
    val sign = when (isIncome) {
        true -> "+"
        false -> "-"
        null -> ""
    }
    
    // Formatting currency ke IDR
    val formattedAmount = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }.format(amount).replace("Rp", "Rp ")
    
    val text = if (amount == 0.0) formattedAmount else "$sign$formattedAmount"

    Text(
        text = text,
        color = color,
        style = style.copy(fontWeight = FontWeight.Bold),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAmountText() {
    LUMATheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AmountText(amount = 150000.0, isIncome = true)
            AmountText(amount = 50000.0, isIncome = false)
            AmountText(amount = 1250000.0, isIncome = null)
        }
    }
}
