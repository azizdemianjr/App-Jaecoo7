package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleType
import com.example.ui.AppUiState
import com.example.ui.MainViewModel
import com.example.ui.components.CostComparisonBarChart
import com.example.ui.components.PriceStepperCard
import com.example.ui.components.shareAppSummary
import com.example.ui.formatCurrency
import com.example.ui.formatNumber
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun EconomyScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = state.strings
    val language = state.language

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vehicle Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vehicle_header"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val vehicleTagColor = when (state.selectedVehicle.type) {
                VehicleType.BEV -> VoltageSecondary
                VehicleType.PHEV -> VoltagePrimary
                VehicleType.HEV -> VoltageTertiary
            }

            // Vehicle Icon Badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(vehicleTagColor.copy(alpha = 0.15f))
                    .clickable { viewModel.openChangeVehicleDialog(true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (state.selectedVehicle.type) {
                        VehicleType.BEV -> Icons.Default.ElectricCar
                        VehicleType.PHEV -> Icons.Default.DirectionsCar
                        VehicleType.HEV -> Icons.Default.DirectionsCar
                    },
                    contentDescription = strings.selectVehicleDesc,
                    tint = vehicleTagColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = state.selectedVehicle.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = VoltageOnSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // Tag Badge
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = VoltagePrimaryContainer.copy(alpha = 0.35f)
                    ) {
                        val tagText = when (state.selectedVehicle.type) {
                            VehicleType.BEV -> strings.typeElectric
                            VehicleType.PHEV -> strings.typePlugInHybrid
                            VehicleType.HEV -> strings.typeConventionalHybrid
                        }
                        Text(
                            text = tagText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VoltagePrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Change model button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = VoltageSurfaceContainerHighest,
                        border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { viewModel.openChangeVehicleDialog(true) }
                    ) {
                        Text(
                            text = strings.changeModelBtn,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VoltageOnSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = strings.appSubtitle,
                    fontSize = 11.sp,
                    color = VoltageOnSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        // Quick Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Share
                IconButton(
                    onClick = { shareAppSummary(context, state) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VoltageSurfaceContainerHigh)
                        .testTag("action_share")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = strings.shareApp,
                        tint = VoltageOnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Print / Export
                IconButton(
                    onClick = { viewModel.openExportReportDialog(true) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VoltageSurfaceContainerHigh)
                        .testTag("action_export_report")
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = strings.exportReportTitle,
                        tint = VoltageOnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reset
                IconButton(
                    onClick = { viewModel.resetToDefaults() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VoltageSurfaceContainerHigh)
                        .testTag("action_reset")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = strings.restoreDefaultsBtn,
                        tint = VoltageOnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Recommendation Box
        val recommendationColor = when {
            state.isElectricOnly -> {
                if (state.costHomePerKm <= state.costPublicPerKm) VoltagePrimary else VoltageSecondary
            }
            state.isHybridConventional -> VoltageTertiary
            else -> {
                when {
                    state.costHomePerKm <= state.costPublicPerKm && state.costHomePerKm <= state.costGasPerKm -> VoltagePrimary
                    state.costPublicPerKm < state.costHomePerKm && state.costPublicPerKm <= state.costGasPerKm -> VoltageSecondary
                    else -> VoltageTertiary
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recommendation_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoltageCardBgHigh),
            border = BorderStroke(1.5.dp, recommendationColor.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header badge
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = recommendationColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, recommendationColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = recommendationColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = state.recommendationBadge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = recommendationColor
                        )
                    }
                }

                // Main headline
                Text(
                    text = state.bestOptionDescription,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        lineHeight = 26.sp
                    ),
                    color = recommendationColor
                )

                // Subtitle
                Text(
                    text = state.recommendationSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VoltageOnSurfaceVariant,
                    lineHeight = 18.sp
                )

                // Metric Highlight Box
                if (state.isElectricOnly) {
                    // BEV: Show Home vs Gas and Home vs Public Savings Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageSurfaceContainerLowest,
                        border = BorderStroke(1.dp, VoltagePrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = VoltagePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = strings.savingsVsGasCarHeader,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = VoltagePrimary
                                )
                            }

                            Text(
                                text = "${formatCurrency(state.costGasPer100Km - state.costHomePer100Km, language)} / 100 km",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                ),
                                color = VoltagePrimary
                            )

                            val homeSavPct = state.savingsHomeVsGasPercent.toInt()
                            val savingsNote = if (language == AppLanguage.EN_US) "Home savings: $homeSavPct% less than gasoline" else "Economia em casa: $homeSavPct% a menos que gasolina"
                            Text(
                                text = savingsNote,
                                fontSize = 11.sp,
                                color = VoltageOnSurfaceVariant.copy(alpha = 0.85f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else if (state.isHybridPlugin) {
                    // PHEV: Show Teto Máximo no Posto vs Gasolina
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageSurfaceContainerLowest,
                        border = BorderStroke(1.dp, VoltageSecondary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = VoltageTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = strings.ceilingHeaderTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = VoltageTertiary
                                )
                            }

                            Text(
                                text = "${formatCurrency(state.ceilingPricePublicKwh, language)} / kWh",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = VoltageSecondary
                            )

                            Text(
                                text = strings.ceilingHeaderSubtitle,
                                fontSize = 11.sp,
                                color = VoltageOnSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    // HEV: Show direct comparison vs 100% ICE car
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageSurfaceContainerLowest,
                        border = BorderStroke(1.dp, VoltageTertiary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = VoltageTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = strings.hevSavingsPer100KmHeader,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = VoltageTertiary
                                )
                            }

                            val diff100km = (state.costIceBenchmarkPer100Km - state.costHevPer100Km).coerceAtLeast(0.0)
                            Text(
                                text = "${formatCurrency(diff100km, language)} / 100 km",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = VoltageTertiary
                            )

                            val baseIceKmLStr = formatNumber(state.customComparisonGasKmL, 1, language)
                            val hevSavText = if (language == AppLanguage.EN_US) {
                                "${state.hevSavingsPercentVsIce.roundToInt()}% savings vs. $baseIceKmLStr km/L of a standard car"
                            } else {
                                "${state.hevSavingsPercentVsIce.roundToInt()}% economia vs. $baseIceKmLStr km/L de um comum"
                            }
                            Text(
                                text = hevSavText,
                                fontSize = 11.sp,
                                color = VoltageOnSurfaceVariant.copy(alpha = 0.85f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (state.isHybridConventional) {
            // ==================== HEV EXCLUSIVE CARDS ====================
            // Card 1: HEV Hybrid Vehicle Cost
            Card(
                modifier = Modifier.fillMaxWidth().testTag("cost_card_hev"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoltageCardBg),
                border = BorderStroke(1.dp, VoltageTertiary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = VoltageTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = state.selectedVehicle.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VoltageTertiary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = VoltageTertiaryContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, VoltageTertiary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "★ ${strings.badgeAutoHybrid}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VoltageTertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatCurrency(state.costHevPerKm, language),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = VoltageTertiary
                        )
                        Text(
                            text = "/km",
                            fontSize = 14.sp,
                            color = VoltageTertiary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val avgConsText = if (language == AppLanguage.EN_US) "Average mileage: ${formatNumber(state.selectedVehicle.gasolineConsumptionKmL, 1, language)} km/L" else "Consumo médio: ${formatNumber(state.selectedVehicle.gasolineConsumptionKmL, 1, language)} km/L"
                        Text(avgConsText, fontSize = 12.sp, color = VoltageTertiary.copy(alpha = 0.85f))
                        Text(
                            "${formatCurrency(state.costHevPer100Km, language)} / 100km",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = VoltageTertiary
                        )
                    }
                }
            }

            // Card 2: Traditional Combustion Benchmark Car
            Card(
                modifier = Modifier.fillMaxWidth().testTag("cost_card_ice_benchmark"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoltageCardBg),
                border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = VoltageOnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.standardIceCarTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VoltageOnSurface
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatCurrency(state.costIceBenchmarkPerKm, language),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = VoltageOnSurface
                        )
                        Text(
                            text = "/km",
                            fontSize = 14.sp,
                            color = VoltageOnSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val baseIceKmLStr = formatNumber(state.customComparisonGasKmL, 1, language)
                        val baseConsText = if (language == AppLanguage.EN_US) "Base mileage: $baseIceKmLStr km/L" else "Consumo base: $baseIceKmLStr km/L"
                        Text(baseConsText, fontSize = 12.sp, color = VoltageOnSurfaceVariant)
                        Text(
                            "${formatCurrency(state.costIceBenchmarkPer100Km, language)} / 100km",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = VoltageOnSurface
                        )
                    }
                }
            }
        } else {
            // ==================== BEV & PHEV CHARGING CARDS ====================
            // Cost Indicator 1: Recarga em Casa
            Card(
                modifier = Modifier.fillMaxWidth().testTag("cost_card_home"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoltageCardBg),
                border = BorderStroke(1.dp, VoltagePrimary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = VoltagePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.homeChargingCardTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VoltagePrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = VoltagePrimaryContainer.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, VoltagePrimary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "★ ${strings.badgeMostEconomical}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VoltagePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatCurrency(state.costHomePerKm, language),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = VoltagePrimary
                        )
                        Text(
                            text = "/km",
                            fontSize = 14.sp,
                            color = VoltagePrimary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("100 km:", fontSize = 12.sp, color = VoltagePrimary.copy(alpha = 0.85f))
                        Text(
                            formatCurrency(state.costHomePer100Km, language),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = VoltagePrimary
                        )
                    }
                }
            }

            // Cost Indicator 2: Recarga em Posto Público
            Card(
                modifier = Modifier.fillMaxWidth().testTag("cost_card_public"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoltageCardBg),
                border = BorderStroke(1.dp, VoltageSecondary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = VoltageSecondary,
                            modifier = Modifier.size(20.dp)
                            )
                        Text(
                            text = strings.publicChargingCardTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VoltageSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatCurrency(state.costPublicPerKm, language),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = VoltageSecondary
                        )
                        Text(
                            text = "/km",
                            fontSize = 14.sp,
                            color = VoltageSecondary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("100 km:", fontSize = 12.sp, color = VoltageSecondary.copy(alpha = 0.85f))
                        Text(
                            formatCurrency(state.costPublicPer100Km, language),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = VoltageSecondary
                        )
                    }
                }
            }

            // Cost Indicator 3: Gasolina (Comparativo para EV ou Consumo Híbrido)
            Card(
                modifier = Modifier.fillMaxWidth().testTag("cost_card_gas"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoltageCardBg),
                border = BorderStroke(1.dp, VoltageTertiary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = VoltageTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (state.isElectricOnly) strings.gasolineComparisonCardTitle else strings.gasolineHybridCardTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VoltageTertiary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatCurrency(state.costGasPerKm, language),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = VoltageTertiary
                        )
                        Text(
                            text = "/km",
                            fontSize = 14.sp,
                            color = VoltageTertiary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val baseKmLStr = "${formatNumber(state.benchmarkGasolineConsumptionKmL, 1, language)} km/L"
                        Text("100 km (base $baseKmLStr):", fontSize = 12.sp, color = VoltageTertiary.copy(alpha = 0.85f))
                        Text(
                            formatCurrency(state.costGasPer100Km, language),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = VoltageTertiary
                        )
                    }
                }
            }
        }

        // Monthly / Annual Savings Slider Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("savings_projection_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoltageSurfaceContainerLow),
            border = BorderStroke(1.dp, VoltageCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = VoltageSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (state.isElectricOnly) {
                                strings.savingsEstimateHomeVsPublicTitle
                            } else {
                                strings.savingsEstimateVsGasTitle
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VoltageOnSurface,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Interactive Monthly Km Stepper Card (type or use - / +)
                PriceStepperCard(
                    title = strings.monthlyMileageLabel,
                    unit = strings.unitKmMonth,
                    icon = Icons.Default.Speed,
                    price = state.monthlyKm.toDouble(),
                    stepAmount = 100.0,
                    accentColor = VoltageSecondary,
                    language = language,
                    valueFormatter = { "${it.toInt()} km" },
                    onMinusClick = { viewModel.stepMonthlyKm(-100f) },
                    onPlusClick = { viewModel.stepMonthlyKm(100f) },
                    onDirectValueChange = { viewModel.setMonthlyKm(it.toFloat()) },
                    testTagPrefix = "monthly_km"
                )

                if (state.isHybridConventional) {
                    // ==================== HEV SAVINGS PROJECTION ====================
                    Text(
                        text = strings.hevSavingsHeader,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = VoltageTertiary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageCardBg,
                        border = BorderStroke(1.dp, VoltageTertiary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.monthlySavingsLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    text = formatCurrency(state.monthlyHevSavingsVsIce, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageTertiary
                                )
                            }

                            HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.estimatedAnnualSavingsLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    text = formatCurrency(state.monthlyHevSavingsVsIce * 12.0, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageTertiary
                                )
                            }

                            HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.gasolineSavedPerMonthLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                val litersStr = "${formatNumber(state.hevMonthlyLitersSaved, 1, language)} L"
                                Text(
                                    text = litersStr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = VoltageTertiary
                                )
                            }
                        }
                    }
                } else if (state.isElectricOnly) {
                    // BEV Specific Projection (Vs Gasoline Car Benchmark)
                    Text(
                        text = strings.homeChargingVsGasHeader,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = VoltagePrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageCardBg,
                        border = BorderStroke(1.dp, VoltagePrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.monthlyHomeSavingsLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    text = formatCurrency(state.monthlyHomeSavingsVsGas, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = VoltagePrimary
                                )
                            }

                            HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.annualHomeSavingsLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    text = formatCurrency(state.annualHomeSavingsVsGas, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = VoltagePrimary
                                )
                            }
                        }
                    }

                    // Posto Público vs Gasolina
                    Text(
                        text = strings.publicChargingVsGasHeader,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = VoltageSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageCardBg,
                        border = BorderStroke(1.dp, VoltageSecondary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.monthlyPublicSavingsLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    text = formatCurrency(state.monthlyPublicSavingsVsGas, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.monthlyPublicSavingsVsGas >= 0) VoltageSecondary else VoltageTertiary
                                )
                            }

                            HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.annualPublicSavingsLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    text = formatCurrency(state.annualPublicSavingsVsGas, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.annualPublicSavingsVsGas >= 0) VoltageSecondary else VoltageTertiary
                                )
                            }
                        }
                    }
                } else {
                    // Recarregando em Casa Box (PHEV)
                    Text(
                        text = strings.rechargingAtHomeVsGasHeader,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = VoltagePrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageCardBg,
                        border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.monthlySavingsLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    text = formatCurrency(state.monthlyHomeSavingsVsGas, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = VoltagePrimary
                                )
                            }

                            HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.estimatedAnnualSavingsLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    text = formatCurrency(state.annualHomeSavingsVsGas, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = VoltagePrimary
                                )
                            }
                        }
                    }

                    // Recarregando em Posto Box
                    Text(
                        text = strings.rechargingAtPublicVsGasHeader,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = VoltageSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VoltageCardBg,
                        border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.extraCostLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    text = formatCurrency(state.monthlyPublicSavingsVsGas, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.monthlyPublicSavingsVsGas >= 0) VoltageSecondary else VoltageTertiary
                                )
                            }

                            HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.annualCostLabel, fontSize = 13.sp, color = VoltageOnSurfaceVariant)
                                Text(
                                    text = formatCurrency(state.annualPublicSavingsVsGas, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.annualPublicSavingsVsGas >= 0) VoltageSecondary else VoltageTertiary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bar Chart Card
        if (state.isHybridConventional) {
            val baseIceKmLStr = formatNumber(state.customComparisonGasKmL, 1, language)
            val baseLabel = if (language == AppLanguage.EN_US) "Standard Gas ($baseIceKmLStr km/L)" else "Combustão Padrão ($baseIceKmLStr km/L)"
            CostComparisonBarChart(
                costHome100km = 0.0,
                costPublic100km = 0.0,
                costGas100km = state.costHevPer100Km,
                language = language,
                customItems = listOf(
                    com.example.ui.components.BarItem(
                        label = "${state.selectedVehicle.name} (${formatNumber(state.selectedVehicle.gasolineConsumptionKmL, 1, language)} km/L)",
                        value = state.costHevPer100Km,
                        color = VoltageTertiary,
                        testTag = "bar_hev"
                    ),
                    com.example.ui.components.BarItem(
                        label = baseLabel,
                        value = state.costIceBenchmarkPer100Km,
                        color = VoltageOnSurfaceVariant,
                        testTag = "bar_ice"
                    )
                )
            )
        } else {
            CostComparisonBarChart(
                costHome100km = state.costHomePer100Km,
                costPublic100km = state.costPublicPer100Km,
                costGas100km = state.costGasPer100Km,
                language = language
            )
        }

        // Adjust parameters button
        OutlinedButton(
            onClick = { viewModel.openEditParametersDialog(true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_adjust_parameters"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, VoltagePrimary)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = VoltagePrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.adjustVehicleParamsBtn, color = VoltagePrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}
