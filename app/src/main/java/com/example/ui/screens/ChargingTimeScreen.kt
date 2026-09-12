package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppUiState
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.formatCurrency
import com.example.ui.formatDuration
import com.example.ui.formatNumber
import com.example.ui.theme.*

@Composable
fun ChargingTimeScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val strings = state.strings
    val language = state.language
    var editingField by remember { mutableStateOf<String?>(null) }
    var editDialogValue by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.isHybridConventional) {
            // ==================== HEV SPECIAL CARD (NO EXTERNAL CHARGING) ====================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_hev_no_plug"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VoltageSurfaceContainerLow),
                border = BorderStroke(1.5.dp, VoltageTertiary.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoltageTertiaryContainer.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = VoltageTertiary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = VoltageTertiaryContainer.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, VoltageTertiary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = strings.typeConventionalHybrid.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VoltageTertiary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = strings.hevTimeCardTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = VoltageOnSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = strings.hevTimeDescription.replace("Jaecoo 7", state.selectedVehicle.name).replace("O veículo", state.selectedVehicle.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VoltageOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = VoltageCardBg,
                        border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (language == com.example.ui.localization.AppLanguage.EN_US) "Average Mileage:" else "Consumo Médio:", fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    "${formatNumber(state.selectedVehicle.gasolineConsumptionKmL, 1, language)} km/L",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageTertiary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(strings.costPerKm + ":", fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    "${formatCurrency(state.costHevPerKm, language)} / km",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageTertiary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(strings.costPer100Km + ":", fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    formatCurrency(state.costHevPer100Km, language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = VoltageTertiary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.setTab(NavigationTab.ECONOMY) },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_goto_economy_hev"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VoltageTertiaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = VoltageOnTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.btnSeeHevEconomy,
                            fontWeight = FontWeight.Bold,
                            color = VoltageOnTertiary
                        )
                    }
                }
            }
        } else {
            // ==================== BEV & PHEV CHARGE COST & TIME CARDS ====================
            // Card 1: Custo de uma recarga
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_charge_cost"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VoltageSurfaceContainerLow),
                border = BorderStroke(1.dp, VoltageCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = VoltagePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = strings.socCostSectionTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                lineHeight = 20.sp
                            ),
                            color = VoltageOnSurface
                        )
                    }

                    // 2 SoC Input Boxes (Initial and Final)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // SoC Inicial
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    editingField = "cost_soc_initial"
                                    editDialogValue = state.costSocInitial.toString()
                                }
                                .testTag("btn_cost_soc_initial"),
                            color = VoltageCardBg,
                            border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = strings.socInitialLabel,
                                    fontSize = 12.sp,
                                    color = VoltageOnSurfaceVariant
                                )
                                Text(
                                    text = "${state.costSocInitial}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    color = VoltageOnSurface
                                )
                            }
                        }

                        // SoC Final
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    editingField = "cost_soc_final"
                                    editDialogValue = state.costSocFinal.toString()
                                }
                                .testTag("btn_cost_soc_final"),
                            color = VoltageCardBg,
                            border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = strings.socFinalLabel,
                                    fontSize = 12.sp,
                                    color = VoltageOnSurfaceVariant
                                )
                                Text(
                                    text = "${state.costSocFinal}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    color = VoltageOnSurface
                                )
                            }
                        }
                    }

                    // Data Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.energyRequiredWithLoss,
                            fontSize = 13.sp,
                            color = VoltageOnSurfaceVariant
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${formatNumber(state.energyRequiredKwh, 2, language)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = VoltageOnSurface
                            )
                            Text(
                                text = "kWh",
                                fontSize = 12.sp,
                                color = VoltageOnSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.homeChargeCost,
                            fontSize = 13.sp,
                            color = VoltageOnSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(state.costRechargeHome, language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = VoltagePrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.publicChargeCost,
                            fontSize = 13.sp,
                            color = VoltageOnSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(state.costRechargePublic, language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = VoltageSecondary
                        )
                    }

                    HorizontalDivider(color = VoltageOutline.copy(alpha = 0.25f))

                    val estNote = if (language == com.example.ui.localization.AppLanguage.EN_US) {
                        "* Calculated based on usable battery capacity of ${formatNumber(state.selectedVehicle.batteryCapacityKwh, 1, language)} kWh."
                    } else {
                        "* Estimativa calculada com base na capacidade de bateria útil de ${formatNumber(state.selectedVehicle.batteryCapacityKwh, 1, language)} kWh."
                    }
                    Text(
                        text = estNote,
                        fontSize = 11.sp,
                        color = VoltageOnSurfaceVariant.copy(alpha = 0.7f),
                        lineHeight = 15.sp
                    )
                }
            }

            // Card 2: Estimativa do Tempo de Recarga Customizada
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_charge_time"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VoltageSurfaceContainerLow),
                border = BorderStroke(1.dp, VoltageCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = VoltageSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = strings.customTimeSectionTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                lineHeight = 20.sp
                            ),
                            color = VoltageOnSurface
                        )
                    }

                    // 4 Grid Inputs: Voltage, Current, SoC Init, SoC Final
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Tensão
                            ParamInputBox(
                                title = strings.voltageLabel,
                                value = "${state.timeVoltage}",
                                onClick = {
                                    editingField = "time_voltage"
                                    editDialogValue = state.timeVoltage.toString()
                                },
                                modifier = Modifier.weight(1f).testTag("btn_time_voltage")
                            )

                            // Corrente
                            ParamInputBox(
                                title = strings.currentLabel,
                                value = "${state.timeCurrent}",
                                onClick = {
                                    editingField = "time_current"
                                    editDialogValue = state.timeCurrent.toString()
                                },
                                modifier = Modifier.weight(1f).testTag("btn_time_current")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // SoC Inicial
                            ParamInputBox(
                                title = strings.socInitialLabel,
                                value = "${state.timeSocInitial}",
                                onClick = {
                                    editingField = "time_soc_initial"
                                    editDialogValue = state.timeSocInitial.toString()
                                },
                                modifier = Modifier.weight(1f).testTag("btn_time_soc_initial")
                            )

                            // SoC Final
                            ParamInputBox(
                                title = strings.socFinalLabel,
                                value = "${state.timeSocFinal}",
                                onClick = {
                                    editingField = "time_soc_final"
                                    editDialogValue = state.timeSocFinal.toString()
                                },
                                modifier = Modifier.weight(1f).testTag("btn_time_soc_final")
                            )
                        }
                    }

                    // Configuração Customizada Highlighted Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageCardBg,
                        border = BorderStroke(1.5.dp, VoltagePrimary.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth().testTag("custom_time_result_box")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = strings.customConfigBadge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp,
                                color = VoltagePrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${strings.powerLabel}: ${formatNumber(state.customPowerKw, 2, language)} kW",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageOnSurface
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = strings.estimatedTimeLabel,
                                        fontSize = 11.sp,
                                        color = VoltageOnSurfaceVariant
                                    )
                                    Text(
                                        text = formatDuration(state.customTimeHours, language),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp
                                        ),
                                        color = VoltageOnSurface
                                    )
                                }
                            }
                        }
                    }

                    // Preset 1: 3.5 kW
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageCardBg,
                        border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.outletPreset35,
                                fontSize = 12.sp,
                                color = VoltageOnSurfaceVariant
                            )
                            Text(
                                text = formatDuration(state.preset35TimeHours, language),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = VoltageOnSurface
                            )
                        }
                    }

                    // Preset 2: 7.0 kW Wallbox AC
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageCardBg,
                        border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.wallboxPreset,
                                fontSize = 12.sp,
                                color = VoltageOnSurfaceVariant
                            )
                            Text(
                                text = formatDuration(state.preset70TimeHours, language),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = VoltageOnSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog for quick numeric entry
    if (editingField != null) {
        val isEn = language == com.example.ui.localization.AppLanguage.EN_US
        val title = when (editingField) {
            "cost_soc_initial" -> if (isEn) "Initial SoC (%)" else "SoC Inicial da Recarga (%)"
            "cost_soc_final" -> if (isEn) "Final SoC (%)" else "SoC Final da Recarga (%)"
            "time_voltage" -> if (isEn) "Electric Voltage (Volts)" else "Tensão Elétrica (Volts)"
            "time_current" -> if (isEn) "Electric Current (Amps)" else "Corrente Elétrica (Amperes)"
            "time_soc_initial" -> if (isEn) "Initial SoC (%)" else "SoC Inicial (%)"
            "time_soc_final" -> if (isEn) "Final SoC (%)" else "SoC Final (%)"
            else -> if (isEn) "Adjust Value" else "Ajustar Valor"
        }

        AlertDialog(
            onDismissRequest = { editingField = null },
            title = { Text(title, color = VoltageOnSurface) },
            text = {
                OutlinedTextField(
                    value = editDialogValue,
                    onValueChange = { editDialogValue = it.filter { ch -> ch.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_param_field")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = editDialogValue.toIntOrNull()
                        if (num != null) {
                            when (editingField) {
                                "cost_soc_initial" -> viewModel.setCostSoc(num, state.costSocFinal)
                                "cost_soc_final" -> viewModel.setCostSoc(state.costSocInitial, num)
                                "time_voltage" -> viewModel.setTimeParameters(num, state.timeCurrent, state.timeSocInitial, state.timeSocFinal)
                                "time_current" -> viewModel.setTimeParameters(state.timeVoltage, num, state.timeSocInitial, state.timeSocFinal)
                                "time_soc_initial" -> viewModel.setTimeParameters(state.timeVoltage, state.timeCurrent, num, state.timeSocFinal)
                                "time_soc_final" -> viewModel.setTimeParameters(state.timeVoltage, state.timeCurrent, state.timeSocInitial, num)
                            }
                        }
                        editingField = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoltagePrimaryContainer)
                ) {
                    Text(strings.btnSave, color = VoltageOnPrimaryContainer)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingField = null }) {
                    Text(strings.btnCancel, color = VoltageOnSurfaceVariant)
                }
            },
            containerColor = VoltageSurfaceContainerHigh
        )
    }
}

@Composable
fun ParamInputBox(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = VoltageCardBg,
        border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = VoltageOnSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = VoltageOnSurface
            )
        }
    }
}
