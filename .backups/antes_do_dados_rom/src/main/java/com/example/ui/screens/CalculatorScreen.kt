package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppUiState
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.PriceStepperCard
import com.example.ui.formatCurrency
import com.example.ui.formatNumber
import com.example.ui.theme.*

@Composable
fun CalculatorScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val strings = state.strings
    val language = state.language

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Prices Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("main_fuel_prices_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = VoltageSurfaceContainerLow),
            border = BorderStroke(1.dp, VoltageCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = VoltagePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = strings.pricesCardTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = VoltageOnSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Stepper 1 & 2: Recarga em Casa & Posto (Only for BEV and PHEV)
                if (!state.isHybridConventional) {
                    // Stepper 1: Recarga em Casa
                    PriceStepperCard(
                        title = strings.homeChargingLabel,
                        unit = strings.unitEnergy,
                        icon = Icons.Default.Home,
                        price = state.homeEnergyPrice,
                        stepAmount = 0.05,
                        accentColor = VoltagePrimary,
                        language = language,
                        onMinusClick = { viewModel.stepHomeEnergyPrice(-0.05) },
                        onPlusClick = { viewModel.stepHomeEnergyPrice(0.05) },
                        onDirectValueChange = { viewModel.updateHomeEnergyPrice(it) },
                        testTagPrefix = "home_price",
                        allowZero = true
                    )

                    // Stepper 2: Recarga em Posto
                    PriceStepperCard(
                        title = strings.publicChargingLabel,
                        unit = strings.unitEnergy,
                        icon = Icons.Default.ElectricBolt,
                        price = state.publicEnergyPrice,
                        stepAmount = 0.05,
                        accentColor = VoltageSecondary,
                        language = language,
                        onMinusClick = { viewModel.stepPublicEnergyPrice(-0.05) },
                        onPlusClick = { viewModel.stepPublicEnergyPrice(0.05) },
                        onDirectValueChange = { viewModel.updatePublicEnergyPrice(it) },
                        testTagPrefix = "public_price",
                        allowZero = true
                    )
                }

                // Stepper 3: Gasolina
                val gasTitle = when {
                    state.isElectricOnly -> strings.gasolineBenchmarkLabel
                    state.isHybridConventional -> strings.gasolinePriceLabel
                    else -> strings.gasolineHybridLabel
                }
                PriceStepperCard(
                    title = gasTitle,
                    unit = strings.unitGasoline,
                    icon = Icons.Default.LocalGasStation,
                    price = state.gasolinePrice,
                    stepAmount = 0.10,
                    accentColor = VoltageTertiary,
                    language = language,
                    onMinusClick = { viewModel.stepGasolinePrice(-0.10) },
                    onPlusClick = { viewModel.stepGasolinePrice(0.10) },
                    onDirectValueChange = { viewModel.updateGasolinePrice(it) },
                    testTagPrefix = "gas_price"
                )

                // Stepper 4: Consumo Base Combustão (para carros 100% elétricos e HEVs)
                if (state.isElectricOnly || state.isHybridConventional) {
                    val compTitle = if (state.isHybridConventional) strings.standardIceAverageLabel else strings.iceAverageShortLabel
                    PriceStepperCard(
                        title = compTitle,
                        unit = strings.unitKmL,
                        icon = Icons.Default.Speed,
                        price = state.customComparisonGasKmL,
                        stepAmount = 0.5,
                        accentColor = VoltageTertiary,
                        language = language,
                        valueFormatter = { "${formatNumber(it, 1, language)} km/L" },
                        onMinusClick = { viewModel.stepCustomComparisonGasKmL(-0.5) },
                        onPlusClick = { viewModel.stepCustomComparisonGasKmL(0.5) },
                        onDirectValueChange = { viewModel.updateCustomComparisonGasKmL(it) },
                        testTagPrefix = "comparison_km_l"
                    )
                }
            }
        }

        // Quick shortcut to check results
        Button(
            onClick = { viewModel.setTab(NavigationTab.ECONOMY) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_see_economy"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VoltagePrimaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = VoltageOnPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.btnSeeEconomyAnalysis,
                fontWeight = FontWeight.Bold,
                color = VoltageOnPrimaryContainer
            )
        }
    }
}
