package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.formatCurrency
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*

@Composable
fun PriceStepperCard(
    title: String,
    unit: String,
    icon: ImageVector,
    price: Double,
    stepAmount: Double = 0.05,
    accentColor: Color = VoltagePrimary,
    language: AppLanguage = AppLanguage.PT_BR,
    valueFormatter: (Double) -> String = { formatCurrency(it, language) },
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
    onDirectValueChange: (Double) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VoltageCardBg),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = unit,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = VoltageOnSurfaceVariant.copy(alpha = 0.85f)
                )
            }

            // Stepper Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = VoltageInputBg,
                border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minus Button
                    IconButton(
                        onClick = onMinusClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(VoltageSurfaceContainerHigh)
                            .testTag("${testTagPrefix}_minus")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = if (language == AppLanguage.EN_US) "Decrease value" else "Diminuir valor",
                            tint = VoltageOnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Value Display (clickable to direct input)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showEditDialog = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("${testTagPrefix}_value_display"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = valueFormatter(price),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            ),
                            color = accentColor
                        )
                    }

                    // Plus Button
                    IconButton(
                        onClick = onPlusClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(VoltageSurfaceContainerHigh)
                            .testTag("${testTagPrefix}_plus")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (language == AppLanguage.EN_US) "Increase value" else "Aumentar valor",
                            tint = VoltageOnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val isEn = language == AppLanguage.EN_US
        val defaultFormatted = if (price % 1.0 == 0.0) {
            price.toInt().toString()
        } else {
            val formatted = String.format(java.util.Locale.US, "%.2f", price)
            if (isEn) formatted else formatted.replace('.', ',')
        }
        var inputString by remember { mutableStateOf(defaultFormatted) }
        var hasError by remember { mutableStateOf(false) }

        fun parseUserInput(raw: String): Double? {
            val clean = raw.trim()
                .replace("R$", "", ignoreCase = true)
                .replace("km/L", "", ignoreCase = true)
                .replace("km/mês", "", ignoreCase = true)
                .replace("km", "", ignoreCase = true)
                .replace("kWh", "", ignoreCase = true)
                .replace("kW", "", ignoreCase = true)
                .trim()
            if (clean.isEmpty()) return null

            if (clean.contains('.') && clean.contains(',')) {
                val dotIdx = clean.indexOf('.')
                val commaIdx = clean.indexOf(',')
                return if (dotIdx < commaIdx) {
                    clean.replace(".", "").replace(',', '.').toDoubleOrNull()
                } else {
                    clean.replace(",", "").toDoubleOrNull()
                }
            }

            if (clean.contains(',')) {
                return clean.replace(',', '.').toDoubleOrNull()
            }

            if (clean.contains('.')) {
                val parts = clean.split('.')
                // Handle Brazilian thousand notation (e.g. 1.500 km or 2.000 km)
                if (parts.size == 2 && parts[1].length == 3 && parts[0].toIntOrNull() != null && (unit.contains("km", ignoreCase = true) || price >= 50.0)) {
                    val withoutDot = clean.replace(".", "").toDoubleOrNull()
                    if (withoutDot != null && withoutDot >= 50.0) {
                        return withoutDot
                    }
                }
                return clean.toDoubleOrNull()
            }

            return clean.toDoubleOrNull()
        }

        AlertDialog(
            onDismissRequest = {
                keyboardController?.hide()
                focusManager.clearFocus()
                showEditDialog = false
            },
            title = { Text(if (isEn) "Adjust $title" else "Ajustar $title", color = VoltageOnSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (isEn) "Enter exact value ($unit):" else "Insira o valor exato ($unit):", color = VoltageOnSurfaceVariant)
                    OutlinedTextField(
                        value = inputString,
                        onValueChange = {
                            inputString = it
                            hasError = false
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = hasError,
                        supportingText = if (hasError) {
                            {
                                Text(
                                    text = if (isEn) "Please enter a valid number greater than zero." else "Insira um número válido maior que zero.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        } else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = VoltageOnSurface,
                            unfocusedTextColor = VoltageOnSurface,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = VoltageOutline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("${testTagPrefix}_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = parseUserInput(inputString)
                        if (parsed != null && parsed > 0.0) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onDirectValueChange(parsed)
                            showEditDialog = false
                        } else {
                            hasError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoltagePrimaryContainer)
                ) {
                    Text(if (isEn) "Save" else "Salvar", color = VoltageOnPrimaryContainer)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    showEditDialog = false
                }) {
                    Text(if (isEn) "Cancel" else "Cancelar", color = VoltageOnSurfaceVariant)
                }
            },
            containerColor = VoltageSurfaceContainerHigh
        )
    }
}

