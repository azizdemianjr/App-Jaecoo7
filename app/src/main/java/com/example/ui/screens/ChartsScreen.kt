package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExamplePhevTestData
import com.example.data.Jaecoo8TestData
import com.example.data.OdometerEntry
import com.example.data.VehicleType
import com.example.ui.AppUiState
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.*
import com.example.ui.formatNumber
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChartsScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val language = state.language
    val strings = AppStrings.get(language)

    var timeGranularity by remember { mutableStateOf(ChartTimeGranularity.DAY) }
    var powertrainFilter by remember { mutableStateOf(ChartPowertrainFilter.ALL) }
    var metricType by remember { mutableStateOf(ChartMetricType.FUEL_EFFICIENCY_KM_L) }
    var presentationType by remember { mutableStateOf(ChartPresentationType.BAR) }
    var selectedPoint by remember { mutableStateOf<ChartDataPoint?>(null) }

    LaunchedEffect(state.selectedVehicle.id, state.selectedVehicle.type) {
        when (state.selectedVehicle.type) {
            VehicleType.BEV -> {
                powertrainFilter = ChartPowertrainFilter.EV
                metricType = ChartMetricType.ENERGY_KWH
            }
            VehicleType.HEV -> {
                powertrainFilter = ChartPowertrainFilter.HEV
                metricType = ChartMetricType.FUEL_EFFICIENCY_KM_L
            }
            VehicleType.PHEV -> {
                if (powertrainFilter == ChartPowertrainFilter.HEV && metricType == ChartMetricType.ENERGY_KWH) {
                    metricType = ChartMetricType.FUEL_EFFICIENCY_KM_L
                } else if (powertrainFilter == ChartPowertrainFilter.EV && metricType == ChartMetricType.FUEL_EFFICIENCY_KM_L) {
                    metricType = ChartMetricType.ENERGY_KWH
                }
            }
        }
    }

    LaunchedEffect(powertrainFilter) {
        if (state.selectedVehicle.type == VehicleType.PHEV) {
            if (powertrainFilter == ChartPowertrainFilter.HEV && metricType == ChartMetricType.ENERGY_KWH) {
                metricType = ChartMetricType.FUEL_EFFICIENCY_KM_L
            } else if (powertrainFilter == ChartPowertrainFilter.EV && metricType == ChartMetricType.FUEL_EFFICIENCY_KM_L) {
                metricType = ChartMetricType.ENERGY_KWH
            }
        }
    }

    val tripsToAnalyze = remember(state.odometerEntries, state.selectedVehicle) {
        state.odometerEntries
            .filter { it.matchesVehicle(state.selectedVehicle) }
            .sortedBy { it.timestamp }
    }

    val latestTripCal = remember(tripsToAnalyze) {
        Calendar.getInstance().apply {
            if (tripsToAnalyze.isNotEmpty()) {
                timeInMillis = tripsToAnalyze.last().timestamp
            }
        }
    }
    var selectedDayMonth by remember { mutableIntStateOf(latestTripCal.get(Calendar.MONTH) + 1) }
    var selectedDayYear by remember { mutableIntStateOf(latestTripCal.get(Calendar.YEAR)) }
    var selectedMonthYear by remember { mutableIntStateOf(latestTripCal.get(Calendar.YEAR)) }

    val availableYears = remember(tripsToAnalyze) {
        val cal = Calendar.getInstance()
        val years = tripsToAnalyze.map {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.YEAR)
        }.toSet().toMutableList()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        if (!years.contains(currentYear)) years.add(currentYear)
        if (!years.contains(currentYear - 1)) years.add(currentYear - 1)
        years.sortedDescending()
    }

    val tripsInScope = remember(tripsToAnalyze, timeGranularity, selectedDayMonth, selectedDayYear, selectedMonthYear) {
        when (timeGranularity) {
            ChartTimeGranularity.DAY -> {
                val cal = Calendar.getInstance()
                tripsToAnalyze.filter { trip ->
                    cal.timeInMillis = trip.timestamp
                    val tripMonth = cal.get(Calendar.MONTH) + 1
                    val tripYear = cal.get(Calendar.YEAR)
                    tripMonth == selectedDayMonth && tripYear == selectedDayYear
                }
            }
            ChartTimeGranularity.MONTH -> {
                val cal = Calendar.getInstance()
                tripsToAnalyze.filter { trip ->
                    cal.timeInMillis = trip.timestamp
                    val tripYear = cal.get(Calendar.YEAR)
                    tripYear == selectedMonthYear
                }
            }
            ChartTimeGranularity.YEAR -> {
                tripsToAnalyze
            }
        }
    }

    val chartDataPoints = remember(tripsInScope, timeGranularity, powertrainFilter, metricType, language, state.gasolinePrice) {
        groupTripsByPeriod(tripsInScope, timeGranularity, powertrainFilter, metricType, language, state.gasolinePrice)
    }

    // Limpa a seleção caso os dados mudem
    LaunchedEffect(chartDataPoints) {
        if (selectedPoint != null && chartDataPoints.none { it.key == selectedPoint?.key }) {
            selectedPoint = null
        }
    }

    val locale = if (language == AppLanguage.EN_US) Locale.US else Locale("pt", "BR")

    // Cálculo das métricas gerais
    val totalRecordedTrips = tripsInScope.size
    val totalRecordedKm = tripsInScope.sumOf { it.totalKm }
    val totalEvKm = tripsInScope.sumOf { it.evKm }
    val totalHevKm = tripsInScope.sumOf { it.hevKm }
    val totalEffectiveFuelKm = tripsInScope.sumOf { it.effectiveFuelKm }
    val totalExcessEvKm = tripsInScope.sumOf { it.excessEvKm }
    val totalPureEvKm = tripsInScope.sumOf { (it.totalKm - it.effectiveFuelKm).coerceAtLeast(0.0) }
    val totalFuelLiters = tripsInScope.sumOf { it.totalGasolineLiters }
    val totalCost = tripsInScope.sumOf { it.totalCost }
    val totalElectricCost = tripsInScope.sumOf { it.electricCost }
    val totalGasCost = tripsInScope.sumOf { it.gasolineCost }

    val overallAvgHevKmL = if (totalEffectiveFuelKm > 0 && totalFuelLiters > 0) {
        totalEffectiveFuelKm / totalFuelLiters
    } else if (totalHevKm > 0 && totalFuelLiters > 0) {
        totalHevKm / totalFuelLiters
    } else {
        val valid = tripsInScope.filter { it.realHevKmL > 0 }
        if (valid.isNotEmpty()) valid.map { it.realHevKmL }.average() else 0.0
    }

    val bestEfficiencyKmL = tripsInScope.mapNotNull {
        val v = if (it.realHevKmL > 0) it.realHevKmL else null
        v
    }.maxOrNull() ?: overallAvgHevKmL

    val totalEnergyKwhScope = tripsInScope.sumOf { entry ->
        if (entry.totalEnergyKwh > 0.0) {
            entry.totalEnergyKwh
        } else if (entry.evKm > 0.0) {
            val cons = if (entry.electricConsumptionKwh100km > 0.0) entry.electricConsumptionKwh100km else 17.5
            entry.evKm * cons / 100.0
        } else {
            0.0
        }
    }

    // Configuração dos KPIs de acordo com o Tipo de Veículo e Modo de Propulsão selecionado
    val kpiCard1Title = when (state.selectedVehicle.type) {
        VehicleType.BEV -> {
            if (metricType == ChartMetricType.COST_PER_KM) {
                if (language == AppLanguage.EN_US) "Electric Cost / km" else "Custo Elétrico / km"
            } else {
                if (language == AppLanguage.EN_US) "Total Energy (kWh)" else "Energia Consumida"
            }
        }
        VehicleType.HEV -> strings.chartsAvgEfficiency
        VehicleType.PHEV -> when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> if (language == AppLanguage.EN_US) "Global Average" else "Média Global"
            ChartPowertrainFilter.HEV -> strings.chartsAvgEfficiency
            ChartPowertrainFilter.EV -> if (language == AppLanguage.EN_US) "Electric Cost / km" else "Custo Elétrico / km"
        }
    }

    val kpiCard1Value = when (state.selectedVehicle.type) {
        VehicleType.BEV -> {
            if (metricType == ChartMetricType.COST_PER_KM) {
                val evCostKm = if (totalRecordedKm > 0) totalElectricCost / totalRecordedKm else 0.0
                "R$ ${formatNumber(evCostKm, 2, language)}/km"
            } else {
                "${formatNumber(totalEnergyKwhScope, 1, language)} kWh"
            }
        }
        VehicleType.HEV -> "${formatNumber(overallAvgHevKmL, 1, language)} km/L"
        VehicleType.PHEV -> when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> {
                val global = if (totalRecordedKm > 0 && totalFuelLiters > 0) totalRecordedKm / totalFuelLiters else overallAvgHevKmL
                "${formatNumber(global, 1, language)} km/L"
            }
            ChartPowertrainFilter.HEV -> "${formatNumber(overallAvgHevKmL, 1, language)} km/L"
            ChartPowertrainFilter.EV -> {
                val evCostKm = if (totalEvKm > 0) totalElectricCost / totalEvKm else 0.0
                "R$ ${formatNumber(evCostKm, 2, language)}/km"
            }
        }
    }

    val kpiCard1Subtitle = when (state.selectedVehicle.type) {
        VehicleType.BEV -> {
            when (timeGranularity) {
                ChartTimeGranularity.DAY -> "${formatNumber(totalRecordedKm, 0, language)} km elétricos (mês)"
                ChartTimeGranularity.MONTH -> "${formatNumber(totalRecordedKm, 0, language)} km elétricos ($selectedMonthYear)"
                ChartTimeGranularity.YEAR -> "${formatNumber(totalRecordedKm, 0, language)} km 100% elétricos"
            }
        }
        VehicleType.HEV -> {
            when (timeGranularity) {
                ChartTimeGranularity.DAY -> "${formatNumber(totalHevKm, 0, language)} km comb. (mês)"
                ChartTimeGranularity.MONTH -> "${formatNumber(totalHevKm, 0, language)} km comb. ($selectedMonthYear)"
                ChartTimeGranularity.YEAR -> "${formatNumber(totalHevKm, 0, language)} km combustão"
            }
        }
        VehicleType.PHEV -> when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> {
                when (timeGranularity) {
                    ChartTimeGranularity.DAY -> "${formatNumber(totalRecordedKm, 0, language)} km no mês"
                    ChartTimeGranularity.MONTH -> "${formatNumber(totalRecordedKm, 0, language)} km em $selectedMonthYear"
                    ChartTimeGranularity.YEAR -> "${formatNumber(totalRecordedKm, 0, language)} km total"
                }
            }
            ChartPowertrainFilter.HEV -> {
                val displayHev = if (totalEffectiveFuelKm > 0) totalEffectiveFuelKm else totalHevKm
                when (timeGranularity) {
                    ChartTimeGranularity.DAY -> "${formatNumber(displayHev, 0, language)} km comb. (mês)"
                    ChartTimeGranularity.MONTH -> "${formatNumber(displayHev, 0, language)} km comb. ($selectedMonthYear)"
                    ChartTimeGranularity.YEAR -> "${formatNumber(displayHev, 0, language)} km combustão"
                }
            }
            ChartPowertrainFilter.EV -> {
                val displayEv = if (totalPureEvKm > 0) totalPureEvKm else totalEvKm
                when (timeGranularity) {
                    ChartTimeGranularity.DAY -> "${formatNumber(displayEv, 0, language)} km elétricos"
                    ChartTimeGranularity.MONTH -> "${formatNumber(displayEv, 0, language)} km EV ($selectedMonthYear)"
                    ChartTimeGranularity.YEAR -> "${formatNumber(displayEv, 0, language)} km 100% elétricos"
                }
            }
        }
    }

    val kpiCard1AccentColor = when (state.selectedVehicle.type) {
        VehicleType.BEV -> Color(0xFFA78BFA)
        VehicleType.HEV -> Color(0xFFF59E0B)
        VehicleType.PHEV -> when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> Color(0xFF38BDF8)
            ChartPowertrainFilter.HEV -> Color(0xFF10B981)
            ChartPowertrainFilter.EV -> Color(0xFFA78BFA)
        }
    }

    val kpiCard1Icon = when (state.selectedVehicle.type) {
        VehicleType.BEV -> Icons.Default.Bolt
        VehicleType.HEV -> Icons.Default.LocalGasStation
        VehicleType.PHEV -> when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> Icons.Default.ElectricCar
            ChartPowertrainFilter.HEV -> Icons.Default.LocalGasStation
            ChartPowertrainFilter.EV -> Icons.Default.Bolt
        }
    }

    val kpiCard2Title = when (state.selectedVehicle.type) {
        VehicleType.BEV -> if (language == AppLanguage.EN_US) "Total Distance" else "Distância Total"
        VehicleType.HEV -> strings.chartsBestEfficiency
        VehicleType.PHEV -> when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> strings.chartsBestEfficiency
            ChartPowertrainFilter.HEV -> if (language == AppLanguage.EN_US) "Best HEV Engine" else "Melhor Motor HEV"
            ChartPowertrainFilter.EV -> if (language == AppLanguage.EN_US) "Electric Share" else "Parcela Elétrica"
        }
    }

    val kpiCard2Value = when (state.selectedVehicle.type) {
        VehicleType.BEV -> "${formatNumber(totalRecordedKm, 0, language)} km"
        VehicleType.HEV -> "${formatNumber(bestEfficiencyKmL, 1, language)} km/L"
        VehicleType.PHEV -> when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> "${formatNumber(bestEfficiencyKmL, 1, language)} km/L"
            ChartPowertrainFilter.HEV -> "${formatNumber(bestEfficiencyKmL, 1, language)} km/L"
            ChartPowertrainFilter.EV -> {
                val evDist = if (totalPureEvKm > 0) totalPureEvKm else totalEvKm
                val evPct = if (totalRecordedKm > 0) (evDist / totalRecordedKm) * 100.0 else 0.0
                "${formatNumber(evPct, 0, language)}%"
            }
        }
    }

    val kpiCard2Subtitle = when (state.selectedVehicle.type) {
        VehicleType.BEV -> "$totalRecordedTrips viagens 100% elétricas"
        VehicleType.HEV -> "${formatNumber(totalFuelLiters, 1, language)} L gasolina"
        VehicleType.PHEV -> when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> "$totalRecordedTrips viagens"
            ChartPowertrainFilter.HEV -> "${formatNumber(totalFuelLiters, 1, language)} L gasolina"
            ChartPowertrainFilter.EV -> {
                val displayEv = if (totalPureEvKm > 0) totalPureEvKm else totalEvKm
                "${formatNumber(displayEv, 0, language)} de ${formatNumber(totalRecordedKm, 0, language)} km"
            }
        }
    }

    val kpiCard2AccentColor = when (state.selectedVehicle.type) {
        VehicleType.BEV -> Color(0xFF38BDF8)
        VehicleType.HEV -> Color(0xFF10B981)
        VehicleType.PHEV -> when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> Color(0xFF10B981)
            ChartPowertrainFilter.HEV -> Color(0xFFF59E0B)
            ChartPowertrainFilter.EV -> Color(0xFF38BDF8)
        }
    }

    val kpiCard2Icon = when (state.selectedVehicle.type) {
        VehicleType.BEV -> Icons.Default.ElectricCar
        VehicleType.HEV -> Icons.Default.ThumbUp
        VehicleType.PHEV -> when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> Icons.Default.Star
            ChartPowertrainFilter.HEV -> Icons.Default.ThumbUp
            ChartPowertrainFilter.EV -> Icons.Default.BatteryChargingFull
        }
    }

    // Tendência Recente (comparação entre as duas metades mais recentes dos dados)
    val trendStatus = remember(chartDataPoints) {
        if (chartDataPoints.size < 2) {
            TrendResult(TrendDirection.STABLE, 0.0)
        } else {
            val mid = chartDataPoints.size / 2
            val firstHalf = chartDataPoints.take(mid).map { it.value }.average()
            val secondHalf = chartDataPoints.drop(mid).map { it.value }.average()
            if (firstHalf > 0) {
                val changePercent = ((secondHalf - firstHalf) / firstHalf) * 100.0
                when {
                    changePercent > 3.0 -> TrendResult(TrendDirection.IMPROVING, changePercent)
                    changePercent < -3.0 -> TrendResult(TrendDirection.DECLINING, changePercent)
                    else -> TrendResult(TrendDirection.STABLE, changePercent)
                }
            } else {
                TrendResult(TrendDirection.STABLE, 0.0)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("charts_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cabeçalho da Tela
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = strings.chartsScreenTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = VoltageOnSurface,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = strings.chartsScreenSubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = VoltageOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // 1.1 Card do Veículo Selecionado com troca rápida para qualquer veículo
        item {
            val vType = state.selectedVehicle.type
            val typeTagColor = when (vType) {
                VehicleType.BEV -> Color(0xFFA78BFA)
                VehicleType.PHEV -> Color(0xFF38BDF8)
                VehicleType.HEV -> Color(0xFFF59E0B)
            }
            val typeBgColor = when (vType) {
                VehicleType.BEV -> Color(0xFF3B0764)
                VehicleType.PHEV -> Color(0xFF0C2B4E)
                VehicleType.HEV -> Color(0xFF451A03)
            }
            val typeName = when (vType) {
                VehicleType.BEV -> if (language == AppLanguage.EN_US) "100% Electric (BEV)" else "100% Elétrico (BEV)"
                VehicleType.PHEV -> if (language == AppLanguage.EN_US) "Plug-in Hybrid (PHEV)" else "Híbrido Plug-in (PHEV)"
                VehicleType.HEV -> if (language == AppLanguage.EN_US) "Conventional Hybrid (HEV)" else "Híbrido Convencional (HEV)"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openChangeVehicleDialog(true) }
                    .testTag("charts_vehicle_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1626)),
                border = BorderStroke(1.dp, Color(0xFF1E3A5F))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = typeBgColor,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (vType) {
                                        VehicleType.BEV -> Icons.Default.Bolt
                                        VehicleType.PHEV -> Icons.Default.ElectricCar
                                        VehicleType.HEV -> Icons.Default.LocalGasStation
                                    },
                                    contentDescription = null,
                                    tint = typeTagColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = state.selectedVehicle.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VoltageOnSurface,
                                        fontSize = 15.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = typeBgColor
                                ) {
                                    Text(
                                        text = vType.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = typeTagColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "$typeName • ${tripsToAnalyze.size} ${if (language == AppLanguage.EN_US) "trips analyzed" else "viagens analisadas"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E3A5F).copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.EN_US) "Switch" else "Trocar",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // Banner informativo/ação de dados de exemplo para o carro Exemplo PHEV
        if (state.selectedVehicle.id == ExamplePhevTestData.VEHICLE_ID || state.selectedVehicle.name.contains("Exemplo", ignoreCase = true)) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F1E33),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth().testTag("example_phev_test_data_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "💡", fontSize = 16.sp)
                                }
                            }
                            Column {
                                Text(
                                    text = if (language == AppLanguage.EN_US) "Exemplo PHEV • Demo (1 Year)" else "Exemplo PHEV • Demonstração (1 Ano)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = if (tripsToAnalyze.isNotEmpty()) {
                                        if (language == AppLanguage.EN_US) "${tripsToAnalyze.size} example trips active to understand charts" else "${tripsToAnalyze.size} viagens de exemplo para entender o funcionamento"
                                    } else {
                                        if (language == AppLanguage.EN_US) "No example trips loaded. Tap to restore." else "Nenhuma viagem carregada. Toque para restaurar."
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = VoltageOnSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (tripsToAnalyze.isEmpty()) {
                            Button(
                                onClick = { viewModel.seedExampleTestData() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_seed_example_test_data")
                            ) {
                                Text(
                                    text = if (language == AppLanguage.EN_US) "Restore" else "Restaurar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.removeExampleTestData() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF87171)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_remove_example_test_data")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.EN_US) "Clear" else "Apagar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Se não houver viagens registradas no Room
        if (tripsToAnalyze.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .testTag("charts_empty_state_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF102A45),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            text = strings.chartsEmptyTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = VoltageOnSurface,
                                textAlign = TextAlign.Center
                            )
                        )

                        Text(
                            text = strings.chartsEmptyDesc,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = VoltageOnSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        )

                        Button(
                            onClick = { viewModel.setTab(NavigationTab.ODOMETER) },
                            colors = ButtonDefaults.buttonColors(containerColor = VoltagePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_charts_go_to_trips")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.chartsGoToTrips, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // 3. KPI Summary Cards (Média, Melhor Eficiência, Tendência Recente)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryKpiCard(
                        title = kpiCard1Title,
                        value = kpiCard1Value,
                        subtitle = kpiCard1Subtitle,
                        accentColor = kpiCard1AccentColor,
                        icon = kpiCard1Icon,
                        modifier = Modifier.weight(1f)
                    )

                    SummaryKpiCard(
                        title = kpiCard2Title,
                        value = kpiCard2Value,
                        subtitle = kpiCard2Subtitle,
                        accentColor = kpiCard2AccentColor,
                        icon = kpiCard2Icon,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Card de Tendência Recente
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1D33)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val (trendIcon, trendColor, trendText) = when (trendStatus.direction) {
                                TrendDirection.IMPROVING -> Triple(
                                    Icons.AutoMirrored.Filled.TrendingUp,
                                    Color(0xFF10B981),
                                    strings.chartsTrendImproving
                                )
                                TrendDirection.DECLINING -> Triple(
                                    Icons.AutoMirrored.Filled.TrendingDown,
                                    Color(0xFFF59E0B),
                                    strings.chartsTrendDeclining
                                )
                                TrendDirection.STABLE -> Triple(
                                    Icons.AutoMirrored.Filled.TrendingFlat,
                                    Color(0xFF94A3B8),
                                    strings.chartsTrendStable
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = trendColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = trendIcon,
                                        contentDescription = null,
                                        tint = trendColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = strings.chartsTrendLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.5.sp
                                    )
                                )
                                Text(
                                    text = trendText,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }

                        if (trendStatus.changePercent != 0.0) {
                            val sign = if (trendStatus.changePercent > 0) "+" else ""
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = if (trendStatus.changePercent > 0) Color(0xFF064E3B) else Color(0xFF78350F)
                            ) {
                                Text(
                                    text = "$sign${formatNumber(trendStatus.changePercent, 1, language)}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (trendStatus.changePercent > 0) Color(0xFF34D399) else Color(0xFFFCD34D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Seletor de Período de Tempo (Mês / Ano) e Modo de Propulsão (HEV+EV, HEV, EV)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A192F)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Linha 1: Botão Dia com Seletor de Mês e Ano na frente
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeGranularityButton(
                                title = strings.chartsPeriodDay,
                                icon = Icons.Default.Today,
                                isSelected = timeGranularity == ChartTimeGranularity.DAY,
                                onClick = { timeGranularity = ChartTimeGranularity.DAY },
                                modifier = Modifier.weight(1f),
                                testTag = "granularity_day"
                            )

                            DayMonthYearPicker(
                                selectedMonth = selectedDayMonth,
                                selectedYear = selectedDayYear,
                                availableYears = availableYears,
                                isActive = timeGranularity == ChartTimeGranularity.DAY,
                                onMonthSelected = { m ->
                                    selectedDayMonth = m
                                    timeGranularity = ChartTimeGranularity.DAY
                                },
                                onYearSelected = { y ->
                                    selectedDayYear = y
                                    timeGranularity = ChartTimeGranularity.DAY
                                },
                                onPrevMonth = {
                                    if (selectedDayMonth == 1) {
                                        selectedDayMonth = 12
                                        selectedDayYear -= 1
                                    } else {
                                        selectedDayMonth -= 1
                                    }
                                    timeGranularity = ChartTimeGranularity.DAY
                                },
                                onNextMonth = {
                                    if (selectedDayMonth == 12) {
                                        selectedDayMonth = 1
                                        selectedDayYear += 1
                                    } else {
                                        selectedDayMonth += 1
                                    }
                                    timeGranularity = ChartTimeGranularity.DAY
                                },
                                language = language,
                                modifier = Modifier.weight(2f)
                            )
                        }

                        // Linha 2: Botão Mês com Seletor de Ano na frente
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeGranularityButton(
                                title = strings.chartsPeriodMonth,
                                icon = Icons.Default.CalendarMonth,
                                isSelected = timeGranularity == ChartTimeGranularity.MONTH,
                                onClick = { timeGranularity = ChartTimeGranularity.MONTH },
                                modifier = Modifier.weight(1f),
                                testTag = "granularity_month"
                            )

                            YearPicker(
                                selectedYear = selectedMonthYear,
                                availableYears = availableYears,
                                isActive = timeGranularity == ChartTimeGranularity.MONTH,
                                onYearSelected = { y ->
                                    selectedMonthYear = y
                                    timeGranularity = ChartTimeGranularity.MONTH
                                },
                                onPrevYear = {
                                    selectedMonthYear -= 1
                                    timeGranularity = ChartTimeGranularity.MONTH
                                },
                                onNextYear = {
                                    selectedMonthYear += 1
                                    timeGranularity = ChartTimeGranularity.MONTH
                                },
                                language = language,
                                modifier = Modifier.weight(2f)
                            )
                        }

                        // Linha 3: Botão Ano
                        TimeGranularityButton(
                            title = strings.chartsPeriodYear,
                            icon = Icons.Default.DateRange,
                            isSelected = timeGranularity == ChartTimeGranularity.YEAR,
                            onClick = { timeGranularity = ChartTimeGranularity.YEAR },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "granularity_year"
                        )

                        HorizontalDivider(
                            color = Color(0xFF1E3A5F).copy(alpha = 0.6f),
                            thickness = 1.dp
                        )

                        // Linha: Filtros de Propulsão adaptados ao tipo de veículo selecionado
                        when (state.selectedVehicle.type) {
                            VehicleType.BEV -> {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1E1035),
                                    border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().testTag("bev_mode_indicator")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFFA78BFA),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (language == AppLanguage.EN_US) "100% Electric Vehicle (BEV) • Electric metrics active" else "Veículo 100% Elétrico (BEV) • Métricas elétricas ativas",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFE9D5FF),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                            VehicleType.HEV -> {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF261505),
                                    border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().testTag("hev_mode_indicator")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalGasStation,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (language == AppLanguage.EN_US) "Conventional Hybrid (HEV) • Combustion & Hybrid metrics active" else "Híbrido Convencional (HEV) • Métricas de combustão e híbrido ativas",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFFDE68A),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                            VehicleType.PHEV -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (language == AppLanguage.EN_US) "Propulsion Mode:" else "Modo de Propulsão:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color(0xFF94A3B8),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PowertrainFilterButton(
                                            title = "HEV+EV",
                                            subtitle = if (language == AppLanguage.EN_US) "Combined" else "Combinado",
                                            icon = Icons.Default.ElectricCar,
                                            isSelected = powertrainFilter == ChartPowertrainFilter.ALL,
                                            accentColor = Color(0xFF38BDF8),
                                            onClick = { powertrainFilter = ChartPowertrainFilter.ALL },
                                            modifier = Modifier.weight(1f),
                                            testTag = "filter_hev_ev"
                                        )
                                        PowertrainFilterButton(
                                            title = "HEV",
                                            subtitle = if (language == AppLanguage.EN_US) "Hybrid" else "Híbrido",
                                            icon = Icons.Default.LocalGasStation,
                                            isSelected = powertrainFilter == ChartPowertrainFilter.HEV,
                                            accentColor = Color(0xFFF59E0B),
                                            onClick = {
                                                powertrainFilter = ChartPowertrainFilter.HEV
                                                if (metricType == ChartMetricType.ENERGY_KWH) {
                                                    metricType = ChartMetricType.FUEL_EFFICIENCY_KM_L
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            testTag = "filter_hev"
                                        )
                                        PowertrainFilterButton(
                                            title = "EV",
                                            subtitle = if (language == AppLanguage.EN_US) "Electric" else "100% Elétrico",
                                            icon = Icons.Default.Bolt,
                                            isSelected = powertrainFilter == ChartPowertrainFilter.EV,
                                            accentColor = Color(0xFFA78BFA),
                                            onClick = {
                                                powertrainFilter = ChartPowertrainFilter.EV
                                                if (metricType == ChartMetricType.FUEL_EFFICIENCY_KM_L) {
                                                    metricType = ChartMetricType.ENERGY_KWH
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            testTag = "filter_ev"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Seletor de Métrica e Tipo de Gráfico (Área ou Barras)
            item {
                val isKmLDisabled = powertrainFilter == ChartPowertrainFilter.EV || state.selectedVehicle.type == VehicleType.BEV
                val isKwhDisabled = powertrainFilter == ChartPowertrainFilter.HEV || state.selectedVehicle.type == VehicleType.HEV

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Métricas em Chips (km/L, kWh, R$/km) adaptadas ao veículo
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (state.selectedVehicle.type != VehicleType.BEV) {
                            MetricSelectChip(
                                title = "km/L",
                                isSelected = metricType == ChartMetricType.FUEL_EFFICIENCY_KM_L,
                                enabled = !isKmLDisabled,
                                color = if (powertrainFilter == ChartPowertrainFilter.HEV) Color(0xFFF59E0B) else Color(0xFF10B981),
                                onClick = { metricType = ChartMetricType.FUEL_EFFICIENCY_KM_L },
                                testTag = "metric_km_l"
                            )
                        }
                        if (state.selectedVehicle.type != VehicleType.HEV) {
                            MetricSelectChip(
                                title = "kWh",
                                isSelected = metricType == ChartMetricType.ENERGY_KWH,
                                enabled = !isKwhDisabled,
                                color = Color(0xFFA78BFA),
                                onClick = { metricType = ChartMetricType.ENERGY_KWH },
                                testTag = "metric_kwh"
                            )
                        }
                        MetricSelectChip(
                            title = "R$/km",
                            isSelected = metricType == ChartMetricType.COST_PER_KM,
                            enabled = true,
                            color = Color(0xFF38BDF8),
                            onClick = { metricType = ChartMetricType.COST_PER_KM },
                            testTag = "metric_cost_km"
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Alternador Área / Barras
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F1E33),
                        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            IconButton(
                                onClick = { presentationType = ChartPresentationType.AREA },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (presentationType == ChartPresentationType.AREA) Color(0xFF1E3A5F) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .testTag("chart_type_area")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = strings.chartsChartTypeLine,
                                    tint = if (presentationType == ChartPresentationType.AREA) Color(0xFF38BDF8) else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { presentationType = ChartPresentationType.BAR },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (presentationType == ChartPresentationType.BAR) Color(0xFF1E3A5F) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .testTag("chart_type_bar")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = strings.chartsChartTypeBar,
                                    tint = if (presentationType == ChartPresentationType.BAR) Color(0xFF38BDF8) else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. O Gráfico Interativo Recharts (EfficiencyTrendChart)
            item {
                EfficiencyTrendChart(
                    dataPoints = chartDataPoints,
                    metricType = metricType,
                    presentationType = presentationType,
                    selectedPoint = selectedPoint,
                    onPointSelected = { selectedPoint = it },
                    powertrainFilter = powertrainFilter,
                    language = language
                )
            }

            // 7. Lista Detalhada da Evolução por Período
            item {
                Text(
                    text = strings.chartsPeriodBreakdown,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = VoltageOnSurface,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(chartDataPoints.reversed(), key = { it.key }) { item ->
                val isSelected = selectedPoint?.key == item.key
                PeriodDetailCard(
                    dataPoint = item,
                    metricType = metricType,
                    powertrainFilter = powertrainFilter,
                    isSelected = isSelected,
                    onClick = {
                        selectedPoint = if (isSelected) null else item
                    },
                    language = language
                )
            }
        }
    }
}

private enum class TrendDirection {
    IMPROVING,
    DECLINING,
    STABLE
}

private data class TrendResult(
    val direction: TrendDirection,
    val changePercent: Double
)

@Composable
private fun SummaryKpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    fontSize = 17.sp
                )
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64748B),
                    fontSize = 10.5.sp
                )
            )
        }
    }
}

@Composable
private fun TimeGranularityButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val bg = if (isSelected) VoltagePrimary else Color(0xFF0F1E33)
    val contentColor = if (isSelected) Color.White else Color(0xFF94A3B8)
    val border = if (isSelected) BorderStroke(1.dp, VoltagePrimary) else BorderStroke(1.dp, Color(0xFF1E3A5F))

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = border,
        modifier = modifier
            .height(42.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            )
        }
    }
}

@Composable
private fun DayMonthYearPicker(
    selectedMonth: Int, // 1..12
    selectedYear: Int,
    availableYears: List<Int>,
    isActive: Boolean,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }

    val monthNamesPt = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
    val monthFullNamesPt = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
    val monthNamesEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthFullNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    val shortMonths = if (language == AppLanguage.EN_US) monthNamesEn else monthNamesPt
    val fullMonths = if (language == AppLanguage.EN_US) monthFullNamesEn else monthFullNamesPt
    val currentMonthShort = shortMonths.getOrElse(selectedMonth - 1) { "Mês" }

    val borderColor = if (isActive) VoltagePrimary else Color(0xFF1E3A5F)
    val containerBg = if (isActive) VoltagePrimary.copy(alpha = 0.12f) else Color(0xFF0F1E33)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerBg,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.height(42.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Seta anterior
            IconButton(
                onClick = onPrevMonth,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = if (language == AppLanguage.EN_US) "Previous month" else "Mês anterior",
                    tint = if (isActive) VoltagePrimary else Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Seletor Mês
            Box {
                Surface(
                    onClick = { monthMenuExpanded = true },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isActive) VoltagePrimary.copy(alpha = 0.18f) else Color(0xFF1E293B),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = currentMonthShort,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isActive) VoltagePrimary else Color(0xFFE2E8F0),
                                fontSize = 12.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isActive) VoltagePrimary else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = monthMenuExpanded,
                    onDismissRequest = { monthMenuExpanded = false },
                    modifier = Modifier.background(Color(0xFF0A192F))
                ) {
                    fullMonths.forEachIndexed { index, name ->
                        val mNum = index + 1
                        val isCurr = mNum == selectedMonth
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isCurr) VoltagePrimary else Color.White,
                                        fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            onClick = {
                                onMonthSelected(mNum)
                                monthMenuExpanded = false
                            },
                            leadingIcon = if (isCurr) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = VoltagePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            // Seletor Ano
            Box {
                Surface(
                    onClick = { yearMenuExpanded = true },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isActive) VoltagePrimary.copy(alpha = 0.18f) else Color(0xFF1E293B),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isActive) Color.White else Color(0xFFE2E8F0),
                                fontSize = 12.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isActive) VoltagePrimary else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = yearMenuExpanded,
                    onDismissRequest = { yearMenuExpanded = false },
                    modifier = Modifier.background(Color(0xFF0A192F))
                ) {
                    availableYears.forEach { yr ->
                        val isCurr = yr == selectedYear
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = yr.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isCurr) VoltagePrimary else Color.White,
                                        fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            onClick = {
                                onYearSelected(yr)
                                yearMenuExpanded = false
                            },
                            leadingIcon = if (isCurr) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = VoltagePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            // Seta próximo
            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (language == AppLanguage.EN_US) "Next month" else "Próximo mês",
                    tint = if (isActive) VoltagePrimary else Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun YearPicker(
    selectedYear: Int,
    availableYears: List<Int>,
    isActive: Boolean,
    onYearSelected: (Int) -> Unit,
    onPrevYear: () -> Unit,
    onNextYear: () -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var yearMenuExpanded by remember { mutableStateOf(false) }

    val borderColor = if (isActive) VoltagePrimary else Color(0xFF1E3A5F)
    val containerBg = if (isActive) VoltagePrimary.copy(alpha = 0.12f) else Color(0xFF0F1E33)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerBg,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.height(42.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPrevYear,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = if (language == AppLanguage.EN_US) "Previous year" else "Ano anterior",
                    tint = if (isActive) VoltagePrimary else Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }

            Box {
                Surface(
                    onClick = { yearMenuExpanded = true },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isActive) VoltagePrimary.copy(alpha = 0.18f) else Color(0xFF1E293B),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.EN_US) "Year $selectedYear" else "Ano $selectedYear",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isActive) VoltagePrimary else Color(0xFFE2E8F0),
                                fontSize = 12.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isActive) VoltagePrimary else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = yearMenuExpanded,
                    onDismissRequest = { yearMenuExpanded = false },
                    modifier = Modifier.background(Color(0xFF0A192F))
                ) {
                    availableYears.forEach { yr ->
                        val isCurr = yr == selectedYear
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = yr.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isCurr) VoltagePrimary else Color.White,
                                        fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            onClick = {
                                onYearSelected(yr)
                                yearMenuExpanded = false
                            },
                            leadingIcon = if (isCurr) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = VoltagePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            IconButton(
                onClick = onNextYear,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (language == AppLanguage.EN_US) "Next year" else "Próximo ano",
                    tint = if (isActive) VoltagePrimary else Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PowertrainFilterButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val bg = if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0xFF0F1E33)
    val contentColor = if (isSelected) accentColor else Color(0xFF94A3B8)
    val border = if (isSelected) BorderStroke(1.5.dp, accentColor) else BorderStroke(1.dp, Color(0xFF1E3A5F))

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = border,
        modifier = modifier
            .height(52.dp)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = contentColor,
                        fontSize = 12.sp
                    )
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    color = if (isSelected) accentColor.copy(alpha = 0.85f) else Color(0xFF64748B)
                )
            )
        }
    }
}

@Composable
private fun MetricSelectChip(
    title: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    testTag: String = ""
) {
    val bg = when {
        !enabled -> Color(0xFF070F1D).copy(alpha = 0.6f)
        isSelected -> color.copy(alpha = 0.22f)
        else -> Color(0xFF0F1E33)
    }
    val textColor = when {
        !enabled -> Color(0xFF475569)
        isSelected -> color
        else -> Color(0xFF94A3B8)
    }
    val border = when {
        !enabled -> BorderStroke(0.6.dp, Color(0xFF1E293B).copy(alpha = 0.35f))
        isSelected -> BorderStroke(1.dp, color)
        else -> BorderStroke(0.8.dp, Color(0xFF1E3A5F))
    }

    Surface(
        onClick = if (enabled) onClick else { {} },
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = border,
        modifier = Modifier
            .height(34.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.5.sp
                )
            )
        }
    }
}

@Composable
private fun PeriodDetailCard(
    dataPoint: ChartDataPoint,
    metricType: ChartMetricType,
    powertrainFilter: ChartPowertrainFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
    language: AppLanguage = AppLanguage.PT_BR
) {
    val borderColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E3A5F)
    val containerColor = if (isSelected) Color(0xFF0F233D) else Color(0xFF091322)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("period_card_${dataPoint.key}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = dataPoint.fullDateLabel,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val (mainDistanceText, subBreakdownText) = when (powertrainFilter) {
                    ChartPowertrainFilter.ALL -> {
                        val tripsLabel = if (dataPoint.tripsCount == 1) "1 viagem" else "${dataPoint.tripsCount} viag."
                        val main = "$tripsLabel • ${formatNumber(dataPoint.totalKm, 0, language)} km"
                        val sub = if (dataPoint.excessEvKm > 0) {
                            "(${formatNumber(dataPoint.pureEvKm, 0, language)} EV puro / ${formatNumber(dataPoint.effectiveFuelKm, 0, language)} HEV comb.)"
                        } else {
                            "(${formatNumber(dataPoint.evKm, 0, language)} EV / ${formatNumber(dataPoint.hevKm, 0, language)} HEV)"
                        }
                        Pair(main, sub)
                    }
                    ChartPowertrainFilter.HEV -> {
                        val tripsLabel = if (dataPoint.tripsCount == 1) "1 viagem" else "${dataPoint.tripsCount} viag."
                        val displayHev = if (dataPoint.effectiveFuelKm > 0) dataPoint.effectiveFuelKm else dataPoint.hevKm
                        if (dataPoint.excessEvKm > 0) {
                            val main = "$tripsLabel • ${formatNumber(displayHev, 0, language)} km comb."
                            val sub = "(${formatNumber(dataPoint.hevKm, 0, language)} HEV + ${formatNumber(dataPoint.excessEvKm, 0, language)} reg.)"
                            Pair(main, sub)
                        } else {
                            val main = "$tripsLabel • ${formatNumber(displayHev, 0, language)} km HEV"
                            Pair(main, null)
                        }
                    }
                    ChartPowertrainFilter.EV -> {
                        val tripsLabel = if (dataPoint.tripsCount == 1) "1 viagem" else "${dataPoint.tripsCount} viag."
                        val displayEv = if (dataPoint.pureEvKm > 0) dataPoint.pureEvKm else if (dataPoint.evKm > 0) dataPoint.evKm else dataPoint.totalKm
                        val main = "$tripsLabel • ${formatNumber(displayEv, 0, language)} km EV"
                        Pair(main, null)
                    }
                }
                Text(
                    text = mainDistanceText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subBreakdownText != null) {
                    Text(
                        text = subBreakdownText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        ),
                        maxLines = 3,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val valueText = when (metricType) {
                    ChartMetricType.FUEL_EFFICIENCY_KM_L -> {
                        if (powertrainFilter == ChartPowertrainFilter.EV) "${formatNumber(dataPoint.value, 1, language)} km/L eq."
                        else "${formatNumber(dataPoint.value, 1, language)} km/L"
                    }
                    ChartMetricType.ENERGY_KWH -> "${formatNumber(dataPoint.value, 1, language)} kWh"
                    ChartMetricType.COST_PER_KM -> "R$ ${formatNumber(dataPoint.value, 2, language)}/km"
                    ChartMetricType.EV_PERCENT -> "${formatNumber(dataPoint.value, 0, language)}% EV"
                }

                val valueColor = when {
                    metricType == ChartMetricType.COST_PER_KM -> Color(0xFF38BDF8) // Sempre azul para todos os modos (HEV+EV, HEV, EV)
                    metricType == ChartMetricType.ENERGY_KWH -> Color(0xFFA78BFA) // Roxo para kWh
                    powertrainFilter == ChartPowertrainFilter.HEV -> Color(0xFFF59E0B) // Laranja para HEV (km/L)
                    powertrainFilter == ChartPowertrainFilter.EV -> Color(0xFFA78BFA) // Roxo para EV
                    metricType == ChartMetricType.EV_PERCENT -> Color(0xFFA78BFA) // Roxo
                    else -> Color(0xFF10B981) // Verde esmeralda p/ km/L combinado
                }

                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = valueColor,
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )

                if (powertrainFilter != ChartPowertrainFilter.EV && dataPoint.fuelLiters > 0) {
                    Text(
                        text = "${formatNumber(dataPoint.fuelLiters, 1, language)} L gas",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFF59E0B),
                            fontSize = 10.sp
                        ),
                        maxLines = 1
                    )
                } else if (powertrainFilter == ChartPowertrainFilter.EV && dataPoint.totalCost > 0) {
                    Text(
                        text = "R$ ${formatNumber(dataPoint.totalCost, 2, language)} energia",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFA78BFA),
                            fontSize = 10.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Agrupa viagens vindas do Room por dia, mês ou ano e extrai os pontos para o gráfico filtrados por propulsão.
 */
private fun groupTripsByPeriod(
    trips: List<OdometerEntry>,
    granularity: ChartTimeGranularity,
    powertrainFilter: ChartPowertrainFilter,
    metricType: ChartMetricType,
    language: AppLanguage,
    gasolinePrice: Double = 6.09
): List<ChartDataPoint> {
    if (trips.isEmpty()) return emptyList()

    val locale = if (language == AppLanguage.EN_US) Locale.US else Locale("pt", "BR")

    val keyFormat = when (granularity) {
        ChartTimeGranularity.DAY -> SimpleDateFormat("yyyy-MM-dd", Locale.US)
        ChartTimeGranularity.MONTH -> SimpleDateFormat("yyyy-MM", Locale.US)
        ChartTimeGranularity.YEAR -> SimpleDateFormat("yyyy", Locale.US)
    }

    val shortLabelFormat = when (granularity) {
        ChartTimeGranularity.DAY -> SimpleDateFormat("dd/MM", locale)
        ChartTimeGranularity.MONTH -> SimpleDateFormat("MMM/yy", locale)
        ChartTimeGranularity.YEAR -> SimpleDateFormat("yyyy", locale)
    }

    val fullLabelFormat = when (granularity) {
        ChartTimeGranularity.DAY -> SimpleDateFormat("dd 'de' MMM, yyyy", locale)
        ChartTimeGranularity.MONTH -> SimpleDateFormat("MMMM 'de' yyyy", locale)
        ChartTimeGranularity.YEAR -> SimpleDateFormat(if (language == AppLanguage.EN_US) "'Year' yyyy" else "'Ano' yyyy", locale)
    }

    val groups = LinkedHashMap<String, MutableList<OdometerEntry>>()

    for (trip in trips) {
        val date = Date(trip.timestamp)
        val key = keyFormat.format(date)
        groups.getOrPut(key) { mutableListOf() }.add(trip)
    }

    return groups.map { (key, entries) ->
        val firstDate = Date(entries.first().timestamp)
        val shortLabel = shortLabelFormat.format(firstDate)
        val fullDateLabel = fullLabelFormat.format(firstDate).replaceFirstChar { it.uppercase() }

        val totalKm = entries.sumOf { it.totalKm }
        val evKm = entries.sumOf { it.evKm }
        val hevKm = entries.sumOf { it.hevKm }
        val effectiveFuelKm = entries.sumOf { it.effectiveFuelKm }
        val excessEvKm = entries.sumOf { it.excessEvKm }
        val pureEvKm = entries.sumOf { (it.totalKm - it.effectiveFuelKm).coerceAtLeast(0.0) }
        val fuelLiters = entries.sumOf { it.totalGasolineLiters }
        val totalCost = entries.sumOf { it.totalCost }
        val electricCost = entries.sumOf { it.electricCost }
        val gasolineCost = entries.sumOf { it.gasolineCost }
        val energyKwh = entries.sumOf { entry ->
            if (entry.totalEnergyKwh > 0.0) {
                entry.totalEnergyKwh
            } else if (entry.evKm > 0.0) {
                val consumption = if (entry.electricConsumptionKwh100km > 0.0) entry.electricConsumptionKwh100km else 17.5
                entry.evKm * consumption / 100.0
            } else {
                0.0
            }
        }

        val value: Double = when (powertrainFilter) {
            ChartPowertrainFilter.ALL -> {
                when (metricType) {
                    ChartMetricType.FUEL_EFFICIENCY_KM_L -> {
                        if (totalKm > 0 && fuelLiters > 0) totalKm / fuelLiters else 0.0
                    }
                    ChartMetricType.ENERGY_KWH -> energyKwh
                    ChartMetricType.COST_PER_KM -> {
                        if (totalKm > 0) totalCost / totalKm else 0.0
                    }
                    ChartMetricType.EV_PERCENT -> {
                        if (totalKm > 0) (evKm / totalKm) * 100.0 else 0.0
                    }
                }
            }
            ChartPowertrainFilter.HEV -> {
                when (metricType) {
                    ChartMetricType.FUEL_EFFICIENCY_KM_L -> {
                        val displayHev = if (effectiveFuelKm > 0) effectiveFuelKm else hevKm
                        if (displayHev > 0 && fuelLiters > 0) {
                            displayHev / fuelLiters
                        } else {
                            val valid = entries.filter { it.realHevKmL > 0 }
                            if (valid.isNotEmpty()) valid.map { it.realHevKmL }.average() else 0.0
                        }
                    }
                    ChartMetricType.ENERGY_KWH -> 0.0
                    ChartMetricType.COST_PER_KM -> {
                        val displayHev = if (effectiveFuelKm > 0) effectiveFuelKm else hevKm
                        if (displayHev > 0) gasolineCost / displayHev else 0.0
                    }
                    ChartMetricType.EV_PERCENT -> 0.0
                }
            }
            ChartPowertrainFilter.EV -> {
                when (metricType) {
                    ChartMetricType.ENERGY_KWH -> energyKwh
                    ChartMetricType.COST_PER_KM -> {
                        val dist = if (pureEvKm > 0) pureEvKm else if (evKm > 0) evKm else totalKm
                        val cost = if (electricCost > 0) electricCost else totalCost
                        if (dist > 0) cost / dist else 0.0
                    }
                    ChartMetricType.FUEL_EFFICIENCY_KM_L -> {
                        val dist = if (pureEvKm > 0) pureEvKm else if (evKm > 0) evKm else totalKm
                        val cost = if (electricCost > 0) electricCost else totalCost
                        val costKm = if (dist > 0) cost / dist else 0.0
                        if (costKm > 0 && gasolinePrice > 0) gasolinePrice / costKm else 0.0
                    }
                    ChartMetricType.EV_PERCENT -> 100.0
                }
            }
        }

        ChartDataPoint(
            key = key,
            shortLabel = shortLabel,
            fullDateLabel = fullDateLabel,
            value = value,
            totalKm = totalKm,
            evKm = evKm,
            hevKm = hevKm,
            pureEvKm = pureEvKm,
            effectiveFuelKm = effectiveFuelKm,
            excessEvKm = excessEvKm,
            fuelLiters = fuelLiters,
            energyKwh = energyKwh,
            totalCost = when (powertrainFilter) {
                ChartPowertrainFilter.ALL -> totalCost
                ChartPowertrainFilter.HEV -> gasolineCost
                ChartPowertrainFilter.EV -> if (electricCost > 0) electricCost else totalCost
            },
            tripsCount = entries.size
        )
    }
}
