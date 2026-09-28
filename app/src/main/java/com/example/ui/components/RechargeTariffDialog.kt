package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppDictionary
import com.example.ui.screens.EditablePriceControl
import com.example.ui.theme.VoltagePrimary
import com.example.ui.formatNumber

@Composable
fun RechargeTariffDialog(
    rechargeIndex: Int,
    isSingle: Boolean,
    currentPrice: Double,
    defaultPrice: Double,
    isHome: Boolean,
    homeEnergyPrice: Double,
    publicEnergyPrice: Double,
    strings: AppDictionary,
    onDismiss: () -> Unit,
    onPriceChange: (Double) -> Unit,
    onReset: () -> Unit
) {
    var tempPrice by remember(currentPrice) { mutableDoubleStateOf(currentPrice) }
    val isCustom = Math.abs(tempPrice - defaultPrice) > 0.001

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onPriceChange(tempPrice)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoltagePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("dialog_tariff_confirm_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.odometerSaveEditBtn, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_tariff_cancel_btn")
            ) {
                Text(strings.odometerCancelBtn, color = Color(0xFF94A3B8))
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isHome) Color(0xFF312E81) else Color(0xFF0C4A6E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isHome) Icons.Default.Home else Icons.Default.EvStation,
                        contentDescription = null,
                        tint = if (isHome) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isSingle) {
                            strings.odometerRechargeTariffDialogTitle
                        } else {
                            "${strings.odometerRechargeTariffDialogTitle} - ${String.format(strings.odometerRechargeNumberPrefix, rechargeIndex + 1)}"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = if (isHome) "Local: Em Casa" else "Local: No Posto Público",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isHome) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Preço Atual / Controle de Ajuste Direto
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF070B14),
                    border = BorderStroke(1.dp, if (isCustom) Color(0xFF0284C7) else Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.odometerRechargeTariffLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        EditablePriceControl(
                            value = tempPrice,
                            onValueChange = { tempPrice = it },
                            onStep = { delta -> tempPrice = (tempPrice + delta).coerceAtLeast(0.0) },
                            compact = false,
                            stepAmount = 0.10,
                            testTagPrefix = "dialog_price_control"
                        )

                        if (isCustom) {
                            Text(
                                text = "${strings.odometerRechargeTariffCustomBadge} (Padrão: R$ ${formatNumber(defaultPrice, 2)}/kWh)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Atalhos Rápidos (Presets)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Atalhos rápidos:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )

                    // Presets específicos da localização
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Preset: Tarifa Padrão do Local
                        Surface(
                            onClick = { tempPrice = defaultPrice },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isHome) "Casa Padrão" else "Posto Padrão",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "R$ ${formatNumber(defaultPrice, 2)}",
                                    color = if (isHome) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Preset: Grátis / Solar (R$ 0,00)
                        Surface(
                            onClick = { tempPrice = 0.0 },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF064E3B),
                            border = BorderStroke(1.dp, Color(0xFF059669)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isHome) "Solar (Grátis)" else "Posto Grátis",
                                    color = Color(0xFFA7F3D0),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "R$ 0,00",
                                    color = Color(0xFF34D399),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Valores comuns de mercado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf(1.50, 1.90, 2.20, 2.50).forEach { pricePreset ->
                            Surface(
                                onClick = { tempPrice = pricePreset },
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(0.8.dp, if (Math.abs(tempPrice - pricePreset) < 0.01) Color(0xFF38BDF8) else Color(0xFF1E293B)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "R$ ${formatNumber(pricePreset, 2)}",
                                    color = if (Math.abs(tempPrice - pricePreset) < 0.01) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    fontSize = 10.5.sp,
                                    fontWeight = if (Math.abs(tempPrice - pricePreset) < 0.01) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Botão Restaurar Padrão
                if (isCustom) {
                    OutlinedButton(
                        onClick = {
                            tempPrice = defaultPrice
                            onReset()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF94A3B8)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.odometerRechargeTariffDefaultBtn,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(16.dp)
    )
}
