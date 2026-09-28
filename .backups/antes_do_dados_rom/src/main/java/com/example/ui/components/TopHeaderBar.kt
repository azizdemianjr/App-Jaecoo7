package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleType
import com.example.ui.AppUiState
import com.example.ui.formatCurrency
import com.example.ui.formatNumber
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*

@Composable
fun TopHeaderBar(
    state: AppUiState,
    onTitleClick: () -> Unit,
    onEditVehicleClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = state.strings
    val tagColor = when (state.selectedVehicle.type) {
        VehicleType.BEV -> VoltageSecondary
        VehicleType.PHEV -> VoltagePrimary
        VehicleType.HEV -> VoltageTertiary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VoltageSurfaceContainerLow)
            .statusBarsPadding()
            .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(tagColor.copy(alpha = 0.15f))
                    .clickable { onTitleClick() }
                    .testTag("top_vehicle_selector_icon"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = strings.selectVehicleDesc,
                    tint = tagColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clipToBounds(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Bloco do Nome do Veículo + Seta Dropdown (clicável para trocar veículo)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onTitleClick() }
                            .padding(vertical = 2.dp, horizontal = 2.dp)
                            .testTag("top_vehicle_selector_trigger")
                    ) {
                        Text(
                            text = state.selectedVehicle.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = VoltageOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = strings.changeVehicleTooltip,
                            tint = VoltageOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Botão de edição do veículo: anda para o lado de acordo com o tamanho do nome do veículo
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(tagColor.copy(alpha = 0.15f))
                            .clickable { onEditVehicleClick() }
                            .testTag("top_btn_edit_vehicle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = strings.editVehicleTooltip,
                            tint = tagColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = tagColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, tagColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onTitleClick() }
                ) {
                    val tagText = when (state.selectedVehicle.type) {
                        VehicleType.BEV -> strings.typeElectric
                        VehicleType.PHEV -> strings.typePlugInHybrid
                        VehicleType.HEV -> strings.typeConventionalHybrid
                    }
                    Text(
                        text = tagText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Language selector button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = VoltageSurfaceContainerHighest,
                border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLanguageClick() }
                    .testTag("top_language_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = state.language.flag,
                        fontSize = 12.sp
                    )
                    Text(
                        text = state.language.shortCode,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoltageOnSurface
                    )
                }
            }

            IconButton(
                onClick = { shareAppSummary(context, state) },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("share_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = strings.shareApp,
                    tint = VoltageOnSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

fun shareAppSummary(context: Context, state: AppUiState) {
    val isEn = state.language == AppLanguage.EN_US
    val text = buildString {
        when {
            state.isHybridConventional -> {
                if (isEn) {
                    appendLine("🚗 Conventional Hybrid (HEV) Comparison: ${state.selectedVehicle.name}")
                    appendLine("🍃 Hybrid Mileage: ${state.selectedVehicle.gasolineConsumptionKmL} km/L (${formatCurrency(state.costHevPerKm, state.language)}/km)")
                    appendLine("⛽ Standard Gas Car (Base): ${formatCurrency(state.costIceBenchmarkPerKm, state.language)}/km (${formatCurrency(state.costIceBenchmarkPer100Km, state.language)} / 100km)")
                    appendLine("🎯 Savings per 100 km: ${formatCurrency((state.costIceBenchmarkPer100Km - state.costHevPer100Km).coerceAtLeast(0.0), state.language)} (${state.hevSavingsPercentVsIce.toInt()}%)")
                    appendLine("💰 Monthly fuel savings (${state.monthlyKm.toInt()} km/month): ${formatCurrency(state.monthlyHevSavingsVsIce, state.language)}")
                    appendLine("📅 Estimated annual savings: ${formatCurrency(state.annualHevSavingsVsIce, state.language)}")
                } else {
                    appendLine("🚗 Comparativo Híbrido (HEV): ${state.selectedVehicle.name}")
                    appendLine("🍃 Consumo do Híbrido: ${state.selectedVehicle.gasolineConsumptionKmL} km/L (${formatCurrency(state.costHevPerKm, state.language)}/km)")
                    appendLine("⛽ Carro Comum (Base): ${formatCurrency(state.costIceBenchmarkPerKm, state.language)}/km (${formatCurrency(state.costIceBenchmarkPer100Km, state.language)} / 100km)")
                    appendLine("🎯 Economia por 100 km: ${formatCurrency((state.costIceBenchmarkPer100Km - state.costHevPer100Km).coerceAtLeast(0.0), state.language)} (${state.hevSavingsPercentVsIce.toInt()}%)")
                    appendLine("💰 Economia mensal (${state.monthlyKm.toInt()} km/mês): ${formatCurrency(state.monthlyHevSavingsVsIce, state.language)}")
                    appendLine("📅 Economia anual estimada: ${formatCurrency(state.annualHevSavingsVsIce, state.language)}")
                }
            }
            state.isElectricOnly -> {
                if (isEn) {
                    appendLine("⚡ 100% Electric (BEV) Comparison: ${state.selectedVehicle.name}")
                    appendLine("🏠 Home Charging: ${formatCurrency(state.costHomePerKm, state.language)}/km (${formatCurrency(state.costHomePer100Km, state.language)} / 100km)")
                    appendLine("🔌 Public Station: ${formatCurrency(state.costPublicPerKm, state.language)}/km (${formatCurrency(state.costPublicPer100Km, state.language)} / 100km)")
                    appendLine("⛽ Gas Benchmark: ${formatCurrency(state.costGasPerKm, state.language)}/km (${formatCurrency(state.costGasPer100Km, state.language)} / 100km)")
                    appendLine("💰 Monthly home savings (${state.monthlyKm.toInt()} km/month): ${formatCurrency(state.monthlyHomeSavingsVsGas, state.language)}")
                    appendLine("📅 Estimated annual savings: ${formatCurrency(state.annualHomeSavingsVsGas, state.language)}")
                } else {
                    appendLine("⚡ Comparativo 100% Elétrico (BEV): ${state.selectedVehicle.name}")
                    appendLine("🏠 Recarga em Casa: ${formatCurrency(state.costHomePerKm, state.language)}/km (${formatCurrency(state.costHomePer100Km, state.language)} / 100km)")
                    appendLine("🔌 Posto Público: ${formatCurrency(state.costPublicPerKm, state.language)}/km (${formatCurrency(state.costPublicPer100Km, state.language)} / 100km)")
                    appendLine("⛽ Equivalente Gasolina: ${formatCurrency(state.costGasPerKm, state.language)}/km (${formatCurrency(state.costGasPer100Km, state.language)} / 100km)")
                    appendLine("💰 Economia mensal em casa (${state.monthlyKm.toInt()} km/mês): ${formatCurrency(state.monthlyHomeSavingsVsGas, state.language)}")
                    appendLine("📅 Economia anual estimada: ${formatCurrency(state.annualHomeSavingsVsGas, state.language)}")
                }
            }
            else -> {
                if (isEn) {
                    appendLine("⚡ Plug-in Hybrid (PHEV) Comparison: ${state.selectedVehicle.name}")
                    appendLine("🏠 Home Charging: ${formatCurrency(state.costHomePerKm, state.language)}/km (${formatCurrency(state.costHomePer100Km, state.language)} / 100km)")
                    appendLine("🔌 Public Station: ${formatCurrency(state.costPublicPerKm, state.language)}/km (${formatCurrency(state.costPublicPer100Km, state.language)} / 100km)")
                    appendLine("⛽ Gasoline Mode: ${formatCurrency(state.costGasPerKm, state.language)}/km (${formatCurrency(state.costGasPer100Km, state.language)} / 100km)")
                    appendLine("🎯 Max Ceiling at Public Station: ${formatCurrency(state.ceilingPricePublicKwh, state.language)}/kWh")
                    appendLine("💰 Monthly home savings (${state.monthlyKm.toInt()} km/month): ${formatCurrency(state.monthlyHomeSavingsVsGas, state.language)}")
                    appendLine("📅 Annual savings: ${formatCurrency(state.annualHomeSavingsVsGas, state.language)}")
                } else {
                    appendLine("⚡ Comparativo Híbrido Plug-in (PHEV): ${state.selectedVehicle.name}")
                    appendLine("🏠 Recarga em Casa: ${formatCurrency(state.costHomePerKm, state.language)}/km (${formatCurrency(state.costHomePer100Km, state.language)} / 100km)")
                    appendLine("🔌 Posto Público: ${formatCurrency(state.costPublicPerKm, state.language)}/km (${formatCurrency(state.costPublicPer100Km, state.language)} / 100km)")
                    appendLine("⛽ Modo Gasolina: ${formatCurrency(state.costGasPerKm, state.language)}/km (${formatCurrency(state.costGasPer100Km, state.language)} / 100km)")
                    appendLine("🎯 Teto Máximo no Posto: ${formatCurrency(state.ceilingPricePublicKwh, state.language)}/kWh")
                    appendLine("💰 Economia mensal em casa (${state.monthlyKm.toInt()} km/mês): ${formatCurrency(state.monthlyHomeSavingsVsGas, state.language)}")
                    appendLine("📅 Economia anual: ${formatCurrency(state.annualHomeSavingsVsGas, state.language)}")
                }
            }
        }
        val appFooter = if (isEn) "\nCalculated with Voltage (PHEV, HEV & EV Calculator) 🚗⚡" else "\nCalculado no Voltage (Calculadora PHEV, HEV & EV) 🚗⚡"
        appendLine(appFooter)
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareTitle = if (isEn) "Share Summary" else "Compartilhar Resumo"
    val shareIntent = Intent.createChooser(sendIntent, shareTitle)
    context.startActivity(shareIntent)
}

