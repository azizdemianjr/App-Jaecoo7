package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppPreferences
import com.example.data.ChargingLocation
import com.example.data.OdometerEntry
import com.example.data.Vehicle
import com.example.data.VehicleCatalog
import com.example.data.VehicleSyncEngine
import com.example.data.VehicleSyncResult
import com.example.data.VehicleType
import com.example.data.extractImageUrl
import com.example.ui.localization.AppDictionary
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

data class AppUiState(
    val language: AppLanguage = AppLanguage.PT_BR,
    val currentTab: NavigationTab = NavigationTab.CALCULATOR,
    val selectedVehicle: Vehicle = VehicleCatalog.defaultVehicles.first(),
    val vehiclesList: List<Vehicle> = VehicleCatalog.defaultVehicles,
    val isSyncingCatalog: Boolean = false,
    val homeEnergyPrice: Double = 1.30,
    val publicEnergyPrice: Double = 1.79,
    val gasolinePrice: Double = 6.50,
    val customComparisonGasKmL: Double = 12.0,
    val monthlyKm: Float = 1000f,
    // SoC charge cost section
    val costSocInitial: Int = 20,
    val costSocFinal: Int = 100,
    val thermalLossPercent: Double = 10.0,
    // Custom charge time section
    val timeVoltage: Int = 220,
    val timeCurrent: Int = 10,
    val timeSocInitial: Int = 20,
    val timeSocFinal: Int = 100,
    // Odometer / Estimativa PHEV section state (Exclusive to PHEV)
    val odometerEntries: List<OdometerEntry> = emptyList(),
    val odometerTotalKm: Double = 100.0,
    val odometerHevKm: Double = 40.0,
    val odometerCustomElectricConsumption: Double = 18.0,
    val odometerCustomGasolineConsumption: Double = 17.5,
    val odometerUseHomeTariff: Boolean = true,
    val odometerTotalStartKm: Double = 0.0,
    val odometerTotalEndKm: Double = 0.0,
    val odometerHevStartKm: Double = 0.0,
    val odometerHevEndKm: Double = 0.0,
    val odometerFuelLiters: Double = 0.0,
    val odometerTripNote: String = "",
    val isSaveOdometerTripDialogOpen: Boolean = false,
    val isOdometerHistoryDialogOpen: Boolean = false,
    val isInitialOdometerDialogOpen: Boolean = false,
    // UI Dialog controls
    val isChangeVehicleDialogOpen: Boolean = false,
    val isAddVehicleDialogOpen: Boolean = false,
    val isImageManagerDialogOpen: Boolean = false,
    val isEditParametersDialogOpen: Boolean = false,
    val isExportReportDialogOpen: Boolean = false,
    val isLanguageDialogOpen: Boolean = false,
    val isSyncSummaryDialogOpen: Boolean = false,
    val vehicleBeingEdited: Vehicle? = null,
    val lastSyncTimestamp: Long = 0L,
    val syncSummaryResult: VehicleSyncResult? = null,
    val pixKey: String = "14430966877",
    val toastMessage: String? = null
) {
    val strings: AppDictionary
        get() = AppStrings.get(language)

    // Vehicle classification
    val isElectricOnly: Boolean
        get() = selectedVehicle.type == VehicleType.BEV || selectedVehicle.gasolineConsumptionKmL <= 0.0

    val isHybridPlugin: Boolean
        get() = selectedVehicle.type == VehicleType.PHEV && selectedVehicle.gasolineConsumptionKmL > 0.0

    val isHybridConventional: Boolean
        get() = selectedVehicle.type == VehicleType.HEV

    // Computations
    // Effective gasoline consumption for comparison (for 100% BEVs, uses the customizable comparison value)
    val benchmarkGasolineConsumptionKmL: Double
        get() = if (selectedVehicle.gasolineConsumptionKmL > 0.0) {
            selectedVehicle.gasolineConsumptionKmL
        } else {
            customComparisonGasKmL.coerceAtLeast(1.0)
        }

    // HEV vs ICE Combustion Car Calculations
    // 1. HEV vehicle cost
    val costHevPerKm: Double
        get() = if (selectedVehicle.gasolineConsumptionKmL > 0.0) {
            gasolinePrice / selectedVehicle.gasolineConsumptionKmL
        } else 0.0

    val costHevPer100Km: Double
        get() = costHevPerKm * 100.0

    // 2. Traditional ICE Combustion Car cost
    val costIceBenchmarkPerKm: Double
        get() = if (customComparisonGasKmL > 0.0) {
            gasolinePrice / customComparisonGasKmL.coerceAtLeast(1.0)
        } else 0.0

    val costIceBenchmarkPer100Km: Double
        get() = costIceBenchmarkPerKm * 100.0

    // 3. Liters and Financial Savings for HEV
    val hevMonthlyLitersUsed: Double
        get() = if (selectedVehicle.gasolineConsumptionKmL > 0.0) {
            monthlyKm / selectedVehicle.gasolineConsumptionKmL
        } else 0.0

    val iceMonthlyLitersUsed: Double
        get() = if (customComparisonGasKmL > 0.0) {
            monthlyKm / customComparisonGasKmL.coerceAtLeast(1.0)
        } else 0.0

    val hevMonthlyLitersSaved: Double
        get() = (iceMonthlyLitersUsed - hevMonthlyLitersUsed).coerceAtLeast(0.0)

    val hevAnnualLitersSaved: Double
        get() = hevMonthlyLitersSaved * 12.0

    val hevSavingsPercentVsIce: Double
        get() = if (costIceBenchmarkPerKm > 0.0) {
            ((costIceBenchmarkPerKm - costHevPerKm) / costIceBenchmarkPerKm) * 100.0
        } else 0.0

    val monthlyHevSavingsVsIce: Double
        get() = (costIceBenchmarkPerKm - costHevPerKm) * monthlyKm

    val annualHevSavingsVsIce: Double
        get() = monthlyHevSavingsVsIce * 12.0

    val costHomePerKm: Double
        get() = if (selectedVehicle.electricConsumptionKwh100km > 0.0) {
            (selectedVehicle.electricConsumptionKwh100km / 100.0) * homeEnergyPrice
        } else 0.0

    val costHomePer100Km: Double
        get() = costHomePerKm * 100.0

    val costPublicPerKm: Double
        get() = if (selectedVehicle.electricConsumptionKwh100km > 0.0) {
            (selectedVehicle.electricConsumptionKwh100km / 100.0) * publicEnergyPrice
        } else 0.0

    val costPublicPer100Km: Double
        get() = costPublicPerKm * 100.0

    val costGasPerKm: Double
        get() = if (isHybridConventional) costHevPerKm else if (benchmarkGasolineConsumptionKmL > 0.0) {
            gasolinePrice / benchmarkGasolineConsumptionKmL
        } else 0.0

    val costGasPer100Km: Double
        get() = costGasPerKm * 100.0

    val ceilingPricePublicKwh: Double
        get() = if (selectedVehicle.electricConsumptionKwh100km > 0 && costGasPerKm > 0) {
            costGasPerKm / (selectedVehicle.electricConsumptionKwh100km / 100.0)
        } else 0.0

    val savingsHomeVsGasPercent: Double
        get() = if (costGasPerKm > 0) {
            ((costGasPerKm - costHomePerKm) / costGasPerKm) * 100.0
        } else 0.0

    val savingsPublicVsGasPercent: Double
        get() = if (costGasPerKm > 0) {
            ((costGasPerKm - costPublicPerKm) / costGasPerKm) * 100.0
        } else 0.0

    val savingsHomeVsPublicPercent: Double
        get() = if (costPublicPerKm > 0) {
            ((costPublicPerKm - costHomePerKm) / costPublicPerKm) * 100.0
        } else 0.0

    val monthlyHomeSavingsVsGas: Double
        get() = (costGasPerKm - costHomePerKm) * monthlyKm

    val annualHomeSavingsVsGas: Double
        get() = monthlyHomeSavingsVsGas * 12.0

    val monthlyPublicSavingsVsGas: Double
        get() = (costGasPerKm - costPublicPerKm) * monthlyKm

    val annualPublicSavingsVsGas: Double
        get() = monthlyPublicSavingsVsGas * 12.0

    // Electric-only specific savings (Home vs Public)
    val monthlyHomeSavingsVsPublic: Double
        get() = (costPublicPerKm - costHomePerKm) * monthlyKm

    val annualHomeSavingsVsPublic: Double
        get() = monthlyHomeSavingsVsPublic * 12.0

    // SoC Cost Computations
    val energyRequiredKwh: Double
        get() {
            val delta = ((costSocFinal - costSocInitial).coerceAtLeast(0)) / 100.0
            return (selectedVehicle.batteryCapacityKwh * delta) * (1.0 + thermalLossPercent / 100.0)
        }

    val costRechargeHome: Double
        get() = energyRequiredKwh * homeEnergyPrice

    val costRechargePublic: Double
        get() = energyRequiredKwh * publicEnergyPrice

    // Time Computations
    val timeEnergyRequiredKwh: Double
        get() {
            val delta = ((timeSocFinal - timeSocInitial).coerceAtLeast(0)) / 100.0
            return (selectedVehicle.batteryCapacityKwh * delta) * (1.0 + thermalLossPercent / 100.0)
        }

    val customPowerKw: Double
        get() = (timeVoltage * timeCurrent) / 1000.0

    val customTimeHours: Double
        get() = if (customPowerKw > 0) timeEnergyRequiredKwh / customPowerKw else 0.0

    val preset35TimeHours: Double
        get() = timeEnergyRequiredKwh / 3.5

    val preset70TimeHours: Double
        get() = timeEnergyRequiredKwh / selectedVehicle.maxAcChargeKw.coerceAtLeast(3.0)

    val recommendationBadge: String
        get() = when {
            isElectricOnly -> strings.badgeBevRecommendation
            isHybridConventional -> strings.badgeHevRecommendation
            else -> strings.badgePhevRecommendation
        }

    val bestOptionDescription: String
        get() {
            val isEn = language == AppLanguage.EN_US
            return when {
                isElectricOnly -> {
                    if (costHomePerKm <= costPublicPerKm) {
                        if (isEn) {
                            "Charging at Home is the best choice!\n(${savingsHomeVsGasPercent.roundToInt()}% savings vs. gas car)"
                        } else {
                            "Recarregar em Casa é a melhor escolha!\n(${savingsHomeVsGasPercent.roundToInt()}% de economia vs. carro a combustão)"
                        }
                    } else {
                        if (isEn) {
                            "Public Station Charging is more advantageous right now!\n(${savingsPublicVsGasPercent.roundToInt()}% savings vs. gas car)"
                        } else {
                            "Recarregar no Posto Público é mais vantajoso no momento!\n(${savingsPublicVsGasPercent.roundToInt()}% de economia vs. carro a combustão)"
                        }
                    }
                }
                isHybridConventional -> {
                    val savedPct = hevSavingsPercentVsIce.roundToInt()
                    if (savedPct > 0) {
                        val savedLit = formatNumber(hevMonthlyLitersSaved, 1, language)
                        if (isEn) {
                            "High Efficiency: ${savedPct}% less fuel!\n(Saving $savedLit Liters/month vs. standard gas car)"
                        } else {
                            "Alta Eficiência: ${savedPct}% menos combustível!\n(Economia de $savedLit Litros/mês vs. combustão comum)"
                        }
                    } else {
                        if (isEn) {
                            "Self-Charging Hybrid: ${selectedVehicle.gasolineConsumptionKmL} km/L"
                        } else {
                            "Híbrido Autorrecarregável: ${selectedVehicle.gasolineConsumptionKmL} km/L"
                        }
                    }
                }
                else -> {
                    if (costHomePerKm <= costPublicPerKm && costHomePerKm <= costGasPerKm) {
                        if (isEn) {
                            "Charging at Home is the best option!\n(${savingsHomeVsGasPercent.roundToInt()}% cheaper than gasoline)"
                        } else {
                            "Carregar em Casa é a melhor opção!\n(${savingsHomeVsGasPercent.roundToInt()}% mais barato que gasolina)"
                        }
                    } else if (costPublicPerKm < costHomePerKm && costPublicPerKm <= costGasPerKm) {
                        if (isEn) {
                            "Charging at Public Station is the best option!\n(${savingsPublicVsGasPercent.roundToInt()}% cheaper than gasoline)"
                        } else {
                            "Carregar no Posto Público é a melhor opção!\n(${savingsPublicVsGasPercent.roundToInt()}% mais barato que gasolina)"
                        }
                    } else {
                        if (isEn) {
                            "Using Gasoline is the best option right now."
                        } else {
                            "Usar Gasolina é a melhor opção no momento."
                        }
                    }
                }
            }
        }

    val recommendationSubtitle: String
        get() {
            val isEn = language == AppLanguage.EN_US
            val homeStr = "${formatCurrency(costHomePerKm, language)}/km"
            val pubStr = "${formatCurrency(costPublicPerKm, language)}/km"
            val gasStr = "${formatCurrency(costGasPerKm, language)}/km"
            val homeVsGasPercent = savingsHomeVsGasPercent.roundToInt()
            val homeVsPubPercent = savingsHomeVsPublicPercent.roundToInt()

            return when {
                isElectricOnly -> {
                    if (isEn) {
                        "Home cost: $homeStr ($homeVsGasPercent% more economical than a gas car at $gasStr). Public station: $pubStr."
                    } else {
                        "Custo em casa: $homeStr ($homeVsGasPercent% mais econômico que um carro a gasolina a $gasStr). No posto público: $pubStr."
                    }
                }
                isHybridConventional -> {
                    val hevKmStr = "${formatCurrency(costHevPerKm, language)}/km"
                    val iceKmStr = "${formatCurrency(costIceBenchmarkPerKm, language)}/km"
                    val baseIceKmL = if (customComparisonGasKmL % 1.0 == 0.0) "${customComparisonGasKmL.toInt()}" else formatNumber(customComparisonGasKmL, 1, language)
                    if (isEn) {
                        "HEV cost: $hevKmStr (${selectedVehicle.gasolineConsumptionKmL} km/L) vs. $iceKmStr in standard gas car (benchmark $baseIceKmL km/L). No plug-in charging required."
                    } else {
                        "Custo no HEV: $hevKmStr (${selectedVehicle.gasolineConsumptionKmL} km/L) vs. $iceKmStr no carro 100% combustão (base $baseIceKmL km/L). Não necessita recarga em tomada."
                    }
                }
                else -> {
                    if (isEn) {
                        "Home cost: $homeStr. Public station ($pubStr) ${if (costPublicPerKm <= costGasPerKm) "is also viable vs. gasoline ($gasStr)" else "is higher than gasoline ($gasStr)"}, but home charging is $homeVsPubPercent% cheaper!"
                    } else {
                        "Custo em casa: $homeStr. No posto público ($pubStr) ${if (costPublicPerKm <= costGasPerKm) "também vale a pena vs. gasolina ($gasStr)" else "está mais caro que gasolina ($gasStr)"}, mas em casa é $homeVsPubPercent% mais barato!"
                    }
                }
            }
        }

    // Odometer / Estimativa PHEV calculations (Trip odometer & fuel measurement)
    val odometerDeltaTotalKm: Double
        get() = (odometerTotalEndKm - odometerTotalStartKm).coerceAtLeast(0.0)

    val odometerDeltaHevKm: Double
        get() = (odometerHevEndKm - odometerHevStartKm).coerceAtLeast(0.0)

    val odometerEvStartKm: Double
        get() = (odometerTotalStartKm - odometerHevStartKm).coerceAtLeast(0.0)

    val odometerEvEndKm: Double
        get() = (odometerTotalEndKm - odometerHevEndKm).coerceAtLeast(0.0)

    val odometerDeltaEvKm: Double
        get() = (odometerEvEndKm - odometerEvStartKm).coerceAtLeast(0.0)

    val odometerAverageHevKmL: Double
        get() = if (odometerFuelLiters > 0 && odometerDeltaHevKm > 0) {
            odometerDeltaHevKm / odometerFuelLiters
        } else 0.0

    val odometerAverageHevL100km: Double
        get() = if (odometerAverageHevKmL > 0) {
            100.0 / odometerAverageHevKmL
        } else 0.0

    val odometerGlobalKmL: Double
        get() = if (odometerFuelLiters > 0 && odometerDeltaTotalKm > 0) {
            odometerDeltaTotalKm / odometerFuelLiters
        } else 0.0

    val odometerEffectiveTotalKm: Double
        get() = if (odometerDeltaTotalKm > 0) odometerDeltaTotalKm else if (odometerTotalKm > 0) odometerTotalKm else 0.0

    val odometerEffectiveHevKm: Double
        get() = if (odometerDeltaHevKm > 0) odometerDeltaHevKm else if (odometerHevKm > 0) odometerHevKm else 0.0

    val odometerEffectiveEvKm: Double
        get() = if (odometerDeltaEvKm > 0) odometerDeltaEvKm else (odometerEffectiveTotalKm - odometerEffectiveHevKm).coerceAtLeast(0.0)

    val odometerEffectiveEnergyPrice: Double
        get() = if (odometerUseHomeTariff) homeEnergyPrice else publicEnergyPrice

    val odometerEffectiveElectricConsumption: Double
        get() = if (odometerCustomElectricConsumption > 0) odometerCustomElectricConsumption else selectedVehicle.electricConsumptionKwh100km

    val odometerEffectiveGasolineConsumption: Double
        get() = if (odometerCustomGasolineConsumption > 0) {
            odometerCustomGasolineConsumption
        } else if (selectedVehicle.gasolineConsumptionKmL > 0) {
            selectedVehicle.gasolineConsumptionKmL
        } else {
            17.5
        }

    val odometerTotalEnergyKwh: Double
        get() {
            val rawKwh = (odometerEffectiveEvKm * odometerEffectiveElectricConsumption / 100.0)
            val maxKwh = if (selectedVehicle.batteryCapacityKwh > 0) selectedVehicle.batteryCapacityKwh * 0.75 else 0.0
            return if (maxKwh > 0) rawKwh.coerceAtMost(maxKwh) else rawKwh
        }

    val odometerTotalGasolineLiters: Double
        get() = if (odometerFuelLiters > 0) odometerFuelLiters else if (odometerEffectiveGasolineConsumption > 0) odometerEffectiveHevKm / odometerEffectiveGasolineConsumption else 0.0

    val odometerElectricCost: Double
        get() = odometerTotalEnergyKwh * odometerEffectiveEnergyPrice

    val odometerGasolineCost: Double
        get() = odometerTotalGasolineLiters * gasolinePrice

    val odometerTotalTripCost: Double
        get() = odometerElectricCost + odometerGasolineCost

    val odometerCostPerKm: Double
        get() = if (odometerEffectiveTotalKm > 0) odometerTotalTripCost / odometerEffectiveTotalKm else 0.0

    val odometerCostPer100Km: Double
        get() = odometerCostPerKm * 100.0

    val odometerEquivalentKmL: Double
        get() = if (odometerTotalTripCost > 0 && gasolinePrice > 0) {
            odometerEffectiveTotalKm / (odometerTotalTripCost / gasolinePrice)
        } else 0.0

    val odometerCost100PercentGas: Double
        get() {
            val effGas = if (selectedVehicle.gasolineConsumptionKmL > 0) selectedVehicle.gasolineConsumptionKmL else customComparisonGasKmL
            return if (effGas > 0) (odometerEffectiveTotalKm / effGas) * gasolinePrice else 0.0
        }

    val odometerSavingsVsGas: Double
        get() = (odometerCost100PercentGas - odometerTotalTripCost).coerceAtLeast(0.0)

    val odometerSavingsPercent: Double
        get() = if (odometerCost100PercentGas > 0) {
            ((odometerSavingsVsGas / odometerCost100PercentGas) * 100.0).coerceIn(0.0, 100.0)
        } else 0.0

    val odometerEvPercent: Double
        get() = if (odometerEffectiveTotalKm > 0) {
            ((odometerEffectiveEvKm / odometerEffectiveTotalKm) * 100.0).coerceIn(0.0, 100.0)
        } else 0.0

    val odometerHevPercent: Double
        get() = if (odometerEffectiveTotalKm > 0) {
            ((odometerEffectiveHevKm / odometerEffectiveTotalKm) * 100.0).coerceIn(0.0, 100.0)
        } else 0.0

    val odometerCo2SavedKg: Double
        get() {
            val gasSavedLiters = ((odometerCost100PercentGas - odometerTotalTripCost) / gasolinePrice.coerceAtLeast(1.0)).coerceAtLeast(0.0)
            return gasSavedLiters * 2.31
        }

    // Accumulated history metrics
    val odometerAccumulatedTotalKm: Double
        get() = odometerEntries.sumOf { it.totalKm }

    val odometerAccumulatedEvKm: Double
        get() = odometerEntries.sumOf { it.evKm }

    val odometerAccumulatedSavings: Double
        get() = odometerEntries.sumOf { it.savingsVsGas }

    val odometerAccumulatedTotalCost: Double
        get() = odometerEntries.sumOf { it.totalCost }

    val odometerAccumulatedEvPercent: Double
        get() = if (odometerAccumulatedTotalKm > 0) {
            ((odometerAccumulatedEvKm / odometerAccumulatedTotalKm) * 100.0).coerceIn(0.0, 100.0)
        } else 0.0
}

enum class NavigationTab {
    CALCULATOR,
    ECONOMY,
    ODOMETER,
    CHARGING_TIME,
    INFO
}

fun formatDuration(hoursDecimal: Double, language: AppLanguage = AppLanguage.PT_BR): String {
    if (hoursDecimal <= 0) return if (language == AppLanguage.EN_US) "0 hrs 00 mins" else "0h 00min"
    val totalMinutes = (hoursDecimal * 60.0).roundToInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (language == AppLanguage.EN_US) {
        "${hours}h ${String.format(Locale.US, "%02d", minutes)}m"
    } else {
        "${hours}h ${String.format(Locale.US, "%02d", minutes)}min"
    }
}

fun formatCurrency(amount: Double, language: AppLanguage = AppLanguage.PT_BR): String {
    return if (language == AppLanguage.EN_US) {
        String.format(Locale.US, "$ %.2f", amount)
    } else {
        String.format(Locale.US, "R$ %.2f", amount).replace('.', ',')
    }
}

fun formatNumber(amount: Double, decimals: Int = 2, language: AppLanguage = AppLanguage.PT_BR): String {
    val formatted = String.format(Locale.US, "%.${decimals}f", amount)
    return if (language == AppLanguage.EN_US) formatted else formatted.replace('.', ',')
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPreferences(application)

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private fun createInitialState(): AppUiState {
        val rawSaved = prefs.getVehiclesList()
        val savedVehicles = if (!rawSaved.isNullOrEmpty()) {
            val savedIds = rawSaved.map { it.id }.toSet()
            val newCatalogVehicles = VehicleCatalog.defaultVehicles.filter { it.id !in savedIds }
            rawSaved + newCatalogVehicles
        } else {
            VehicleCatalog.defaultVehicles
        }
        val savedVehicleId = prefs.getSelectedVehicleId()
        val selected = savedVehicles.find { it.id == savedVehicleId }
            ?: savedVehicles.firstOrNull()
            ?: VehicleCatalog.defaultVehicles.first()
        val savedLangCode = prefs.getLanguage()
        val language = AppLanguage.fromCode(savedLangCode)
        val savedOdometerEntries = prefs.getOdometerEntries()
        var savedOdometerTotalStartKm = prefs.getOdometerDraftTotalStartKm()
        var savedOdometerTotalEndKm = prefs.getOdometerDraftTotalEndKm()
        var savedOdometerHevStartKm = prefs.getOdometerDraftHevStartKm()
        var savedOdometerHevEndKm = prefs.getOdometerDraftHevEndKm()
        var savedOdometerFuelLiters = prefs.getOdometerDraftFuelLiters()
        val savedOdometerTripNote = prefs.getOdometerDraftTripNote()

        // Limpar dados legados de mock para que o total atual e o hev atual comecem sem valores
        if ((savedOdometerTotalStartKm == 12000.0 && savedOdometerTotalEndKm == 12650.0) ||
            (savedOdometerTotalEndKm == 1000.0 && savedOdometerHevEndKm == 100.0) ||
            (savedOdometerEntries.isEmpty() && savedOdometerTotalEndKm <= savedOdometerTotalStartKm)
        ) {
            savedOdometerTotalEndKm = 0.0
            savedOdometerHevEndKm = 0.0
            savedOdometerFuelLiters = 0.0
            prefs.saveOdometerDraft(savedOdometerTotalStartKm, 0.0, savedOdometerHevStartKm, 0.0, 0.0, "")
        }

        return AppUiState(
            language = language,
            selectedVehicle = selected,
            vehiclesList = savedVehicles,
            homeEnergyPrice = prefs.getHomeEnergyPrice(),
            publicEnergyPrice = prefs.getPublicEnergyPrice(),
            gasolinePrice = prefs.getGasolinePrice(),
            customComparisonGasKmL = prefs.getCustomComparisonGasKmL(),
            monthlyKm = prefs.getMonthlyKm(),
            costSocInitial = prefs.getCostSocInitial(),
            costSocFinal = prefs.getCostSocFinal(),
            thermalLossPercent = prefs.getThermalLossPercent(),
            timeVoltage = prefs.getTimeVoltage(),
            timeCurrent = prefs.getTimeCurrent(),
            timeSocInitial = prefs.getTimeSocInitial(),
            timeSocFinal = prefs.getTimeSocFinal(),
            odometerEntries = savedOdometerEntries,
            odometerTotalStartKm = savedOdometerTotalStartKm,
            odometerTotalEndKm = savedOdometerTotalEndKm,
            odometerHevStartKm = savedOdometerHevStartKm,
            odometerHevEndKm = savedOdometerHevEndKm,
            odometerFuelLiters = savedOdometerFuelLiters,
            odometerTripNote = savedOdometerTripNote,
            lastSyncTimestamp = prefs.getLastSyncTimestamp()
        )
    }

    fun setLanguage(language: AppLanguage) {
        prefs.saveLanguage(language.code)
        _uiState.update { it.copy(language = language, isLanguageDialogOpen = false) }
        val msg = if (language == AppLanguage.EN_US) "Language set to English (US)" else "Idioma alterado para Português (Brasil)"
        showToast(msg)
    }

    fun openLanguageDialog(open: Boolean) {
        _uiState.update { it.copy(isLanguageDialogOpen = open) }
    }

    fun setTab(tab: NavigationTab) {
        val targetTab = if (tab == NavigationTab.ODOMETER && _uiState.value.selectedVehicle.type != VehicleType.PHEV) {
            NavigationTab.CALCULATOR
        } else {
            tab
        }
        _uiState.update { it.copy(currentTab = targetTab) }
    }

    fun updateHomeEnergyPrice(price: Double) {
        val rounded = (price * 100.0).roundToInt() / 100.0
        prefs.saveHomeEnergyPrice(rounded)
        _uiState.update { it.copy(homeEnergyPrice = rounded) }
    }

    fun stepHomeEnergyPrice(delta: Double) {
        val currentPrice = _uiState.value.homeEnergyPrice
        val newPrice = ((currentPrice + delta).coerceAtLeast(0.05) * 100.0).roundToInt() / 100.0
        prefs.saveHomeEnergyPrice(newPrice)
        _uiState.update { it.copy(homeEnergyPrice = newPrice) }
    }

    fun updatePublicEnergyPrice(price: Double) {
        val rounded = (price * 100.0).roundToInt() / 100.0
        prefs.savePublicEnergyPrice(rounded)
        _uiState.update { it.copy(publicEnergyPrice = rounded) }
    }

    fun stepPublicEnergyPrice(delta: Double) {
        val currentPrice = _uiState.value.publicEnergyPrice
        val newPrice = ((currentPrice + delta).coerceAtLeast(0.05) * 100.0).roundToInt() / 100.0
        prefs.savePublicEnergyPrice(newPrice)
        _uiState.update { it.copy(publicEnergyPrice = newPrice) }
    }

    fun updateGasolinePrice(price: Double) {
        val rounded = (price * 100.0).roundToInt() / 100.0
        prefs.saveGasolinePrice(rounded)
        _uiState.update { it.copy(gasolinePrice = rounded) }
    }

    fun stepGasolinePrice(delta: Double) {
        val currentPrice = _uiState.value.gasolinePrice
        val newPrice = ((currentPrice + delta).coerceAtLeast(0.50) * 100.0).roundToInt() / 100.0
        prefs.saveGasolinePrice(newPrice)
        _uiState.update { it.copy(gasolinePrice = newPrice) }
    }

    fun updateCustomComparisonGasKmL(kmL: Double) {
        val rounded = (kmL * 10.0).roundToInt() / 10.0
        prefs.saveCustomComparisonGasKmL(rounded)
        _uiState.update { it.copy(customComparisonGasKmL = rounded) }
    }

    fun stepCustomComparisonGasKmL(delta: Double) {
        val currentKmL = _uiState.value.customComparisonGasKmL
        val newKmL = (((currentKmL + delta).coerceIn(1.0, 50.0)) * 10.0).roundToInt() / 10.0
        prefs.saveCustomComparisonGasKmL(newKmL)
        _uiState.update { it.copy(customComparisonGasKmL = newKmL) }
    }

    fun setMonthlyKm(km: Float) {
        val validKm = km.coerceIn(10f, 100000f)
        prefs.saveMonthlyKm(validKm)
        _uiState.update { it.copy(monthlyKm = validKm) }
    }

    fun stepMonthlyKm(delta: Float) {
        val currentKm = _uiState.value.monthlyKm
        val newKm = (currentKm + delta).coerceIn(10f, 100000f)
        prefs.saveMonthlyKm(newKm)
        _uiState.update { it.copy(monthlyKm = newKm) }
    }

    fun selectVehicle(vehicle: Vehicle) {
        prefs.saveSelectedVehicleId(vehicle.id)
        val nextTab = if (vehicle.type != VehicleType.PHEV && _uiState.value.currentTab == NavigationTab.ODOMETER) {
            NavigationTab.CALCULATOR
        } else {
            _uiState.value.currentTab
        }
        _uiState.update {
            it.copy(
                selectedVehicle = vehicle,
                currentTab = nextTab,
                odometerCustomElectricConsumption = vehicle.electricConsumptionKwh100km,
                odometerCustomGasolineConsumption = if (vehicle.gasolineConsumptionKmL > 0) vehicle.gasolineConsumptionKmL else 17.5,
                isChangeVehicleDialogOpen = false
            )
        }
    }

    fun addNewVehicle(vehicle: Vehicle) {
        val currentList = _uiState.value.vehiclesList
        val updatedList = listOf(vehicle) + currentList.filter { v -> v.id != vehicle.id }
        prefs.saveVehiclesList(updatedList)
        prefs.saveSelectedVehicleId(vehicle.id)
        _uiState.update {
            it.copy(
                vehiclesList = updatedList,
                selectedVehicle = vehicle,
                isAddVehicleDialogOpen = false,
                isChangeVehicleDialogOpen = false
            )
        }
        showToast("Veículo '${vehicle.name}' salvo com sucesso!")
    }

    fun deleteVehicle(vehicleId: String) {
        val target = _uiState.value.vehiclesList.find { it.id == vehicleId }
        val updatedList = _uiState.value.vehiclesList.filter { v -> v.id != vehicleId }
        val newSelected = if (_uiState.value.selectedVehicle.id == vehicleId) {
            updatedList.firstOrNull() ?: VehicleCatalog.defaultVehicles.first()
        } else {
            _uiState.value.selectedVehicle
        }
        prefs.saveVehiclesList(updatedList)
        prefs.saveSelectedVehicleId(newSelected.id)
        _uiState.update {
            it.copy(
                vehiclesList = updatedList,
                selectedVehicle = newSelected
            )
        }
        val name = target?.name ?: "Veículo"
        showToast("$name removido da lista.")
    }

    fun deleteCustomVehicle(vehicleId: String) {
        deleteVehicle(vehicleId)
    }

    fun restoreDefaultVehicles() {
        val defaultList = VehicleCatalog.defaultVehicles
        val defaultSelected = defaultList.first()
        prefs.saveVehiclesList(defaultList)
        prefs.saveSelectedVehicleId(defaultSelected.id)
        _uiState.update {
            it.copy(
                vehiclesList = defaultList,
                selectedVehicle = defaultSelected
            )
        }
        showToast("Catálogo padrão de veículos restaurado.")
    }

    fun resetSingleVehicleToFactory(vehicleId: String) {
        val currentList = _uiState.value.vehiclesList
        val updatedList = VehicleSyncEngine.resetSingleToFactory(vehicleId, currentList)
        val target = updatedList.find { it.id == vehicleId }
        prefs.saveVehiclesList(updatedList)
        
        val updatedSelected = if (_uiState.value.selectedVehicle.id == vehicleId && target != null) {
            target
        } else {
            _uiState.value.selectedVehicle
        }
        prefs.saveSelectedVehicleId(updatedSelected.id)
        
        _uiState.update {
            it.copy(
                vehiclesList = updatedList,
                selectedVehicle = updatedSelected,
                vehicleBeingEdited = null,
                isEditParametersDialogOpen = false
            )
        }
        showToast("Ficha técnica oficial restaurada para '${target?.name ?: "Veículo"}'!")
    }

    fun openEditParametersForVehicle(vehicle: Vehicle) {
        _uiState.update {
            it.copy(
                vehicleBeingEdited = vehicle,
                isEditParametersDialogOpen = true
            )
        }
    }

    fun updateVehicleCustomValues(
        name: String,
        batteryCapacity: Double,
        electricConsumption: Double,
        gasolineConsumption: Double,
        maxAcCharge: Double,
        imageUrl: String,
        targetVehicleId: String? = null
    ) {
        val cleanUrl = extractImageUrl(imageUrl)
        val targetId = targetVehicleId ?: _uiState.value.vehicleBeingEdited?.id ?: _uiState.value.selectedVehicle.id
        val targetVehicle = _uiState.value.vehiclesList.find { it.id == targetId } ?: _uiState.value.selectedVehicle
        
        val updatedVehicle = targetVehicle.copy(
            name = name.ifBlank { "Veículo Customizado" },
            batteryCapacityKwh = batteryCapacity.coerceAtLeast(1.0),
            electricConsumptionKwh100km = electricConsumption.coerceAtLeast(0.0),
            gasolineConsumptionKmL = gasolineConsumption.coerceAtLeast(0.0),
            maxAcChargeKw = maxAcCharge.coerceAtLeast(0.0),
            imageUrl = cleanUrl,
            isCustom = true
        )
        val updatedList = _uiState.value.vehiclesList.map { v -> if (v.id == updatedVehicle.id) updatedVehicle else v }
        prefs.saveVehiclesList(updatedList)
        
        val newSelected = if (_uiState.value.selectedVehicle.id == updatedVehicle.id) {
            prefs.saveSelectedVehicleId(updatedVehicle.id)
            updatedVehicle
        } else {
            _uiState.value.selectedVehicle
        }

        _uiState.update {
            it.copy(
                vehiclesList = updatedList,
                selectedVehicle = newSelected,
                vehicleBeingEdited = null,
                isEditParametersDialogOpen = false,
                isImageManagerDialogOpen = false
            )
        }
        showToast("Especificações de '${updatedVehicle.name}' atualizadas com sucesso!")
    }

    fun updateVehicleImage(rawInput: String) {
        val cleanUrl = extractImageUrl(rawInput)
        val updatedVehicle = _uiState.value.selectedVehicle.copy(imageUrl = cleanUrl)
        val updatedList = _uiState.value.vehiclesList.map { v -> if (v.id == updatedVehicle.id) updatedVehicle else v }
        prefs.saveVehiclesList(updatedList)
        _uiState.update {
            it.copy(
                vehiclesList = updatedList,
                selectedVehicle = updatedVehicle,
                isImageManagerDialogOpen = false
            )
        }
        showToast("Imagem do veículo atualizada!")
    }

    fun setCostSoc(initial: Int, final: Int) {
        val validInitial = initial.coerceIn(0, 99)
        val validFinal = final.coerceIn(validInitial + 1, 100)
        prefs.saveCostSoc(validInitial, validFinal)
        _uiState.update {
            it.copy(
                costSocInitial = validInitial,
                costSocFinal = validFinal
            )
        }
    }

    fun setTimeParameters(voltage: Int, current: Int, initial: Int, final: Int) {
        val validVoltage = voltage.coerceAtLeast(110)
        val validCurrent = current.coerceAtLeast(6)
        val validInitial = initial.coerceIn(0, 99)
        val validFinal = final.coerceIn(validInitial + 1, 100)
        prefs.saveTimeParameters(validVoltage, validCurrent, validInitial, validFinal)
        _uiState.update {
            it.copy(
                timeVoltage = validVoltage,
                timeCurrent = validCurrent,
                timeSocInitial = validInitial,
                timeSocFinal = validFinal
            )
        }
    }

    fun resetToDefaults() {
        prefs.clearAll()
        val defaultVehicle = VehicleCatalog.defaultVehicles.first()
        _uiState.update {
            AppUiState(
                selectedVehicle = defaultVehicle,
                vehiclesList = VehicleCatalog.defaultVehicles
            )
        }
        showToast("Configurações restauradas para o padrão!")
    }

    fun openChangeVehicleDialog(open: Boolean) {
        _uiState.update { it.copy(isChangeVehicleDialogOpen = open) }
    }

    fun openAddVehicleDialog(open: Boolean) {
        _uiState.update { it.copy(isAddVehicleDialogOpen = open) }
    }

    fun openImageManagerDialog(open: Boolean) {
        _uiState.update { it.copy(isImageManagerDialogOpen = open) }
    }

    fun openEditParametersDialog(open: Boolean) {
        _uiState.update { 
            it.copy(
                isEditParametersDialogOpen = open,
                vehicleBeingEdited = if (open) it.selectedVehicle else null
            ) 
        }
    }

    fun openExportReportDialog(open: Boolean) {
        _uiState.update { it.copy(isExportReportDialogOpen = open) }
    }

    fun openSyncSummaryDialog(open: Boolean) {
        _uiState.update { it.copy(isSyncSummaryDialogOpen = open) }
    }

    fun syncVehiclesFromCloud() {
        if (_uiState.value.isSyncingCatalog) return
        _uiState.update { it.copy(isSyncingCatalog = true) }
        viewModelScope.launch {
            try {
                val cloudFleet = VehicleCatalog.fetchLatestVehicleDatabase()
                val currentList = _uiState.value.vehiclesList
                
                // Use intelligent merge engine
                val (updatedList, syncResult) = VehicleSyncEngine.smartSync(currentList, cloudFleet)
                
                prefs.saveVehiclesList(updatedList)
                val now = System.currentTimeMillis()
                prefs.saveLastSyncTimestamp(now)
                
                // Update selected vehicle in case its specs updated
                val currentSelectedId = _uiState.value.selectedVehicle.id
                val refreshedSelected = updatedList.find { it.id == currentSelectedId } ?: _uiState.value.selectedVehicle
                prefs.saveSelectedVehicleId(refreshedSelected.id)

                _uiState.update {
                    it.copy(
                        vehiclesList = updatedList,
                        selectedVehicle = refreshedSelected,
                        isSyncingCatalog = false,
                        lastSyncTimestamp = now,
                        syncSummaryResult = syncResult,
                        isSyncSummaryDialogOpen = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncingCatalog = false) }
                showToast("Erro ao sincronizar catálogo online. Tente novamente.")
            }
        }
    }

    // ==========================================
    // PHEV ESTIMATIVA (ODOMETER & HEV) METHODS
    // ==========================================

    fun updateOdometerTotalKm(km: Double) {
        val rounded = ((km * 10.0).roundToInt() / 10.0).coerceAtLeast(0.0)
        _uiState.update { it.copy(odometerTotalKm = rounded) }
        saveOdometerDraft()
    }

    fun updateOdometerHevKm(km: Double) {
        val rounded = ((km * 10.0).roundToInt() / 10.0).coerceAtLeast(0.0)
        _uiState.update { it.copy(odometerHevKm = rounded) }
        saveOdometerDraft()
    }

    fun updateOdometerElectricConsumption(kwh: Double) {
        val rounded = ((kwh * 10.0).roundToInt() / 10.0).coerceAtLeast(5.0)
        _uiState.update { it.copy(odometerCustomElectricConsumption = rounded) }
    }

    fun updateOdometerGasolineConsumption(kmL: Double) {
        val rounded = ((kmL * 10.0).roundToInt() / 10.0).coerceAtLeast(2.0)
        _uiState.update { it.copy(odometerCustomGasolineConsumption = rounded) }
    }

    fun setOdometerUseHomeTariff(useHome: Boolean) {
        _uiState.update { it.copy(odometerUseHomeTariff = useHome) }
        saveOdometerDraft()
    }

    fun updateOdometerFuelLiters(liters: Double) {
        val rounded = ((liters * 10.0).roundToInt() / 10.0).coerceAtLeast(0.0)
        _uiState.update { it.copy(odometerFuelLiters = rounded) }
        saveOdometerDraft()
    }

    fun updateOdometerTripNote(note: String) {
        _uiState.update { it.copy(odometerTripNote = note) }
        saveOdometerDraft()
    }

    fun openOdometerHistoryDialog(open: Boolean) {
        _uiState.update { it.copy(isOdometerHistoryDialogOpen = open) }
    }

    fun openInitialOdometerDialog(open: Boolean) {
        if (open && _uiState.value.odometerEntries.isNotEmpty()) {
            val msg = if (_uiState.value.language == AppLanguage.EN_US) {
                "Cannot edit initial mileage while trip history exists."
            } else {
                "Não é possível editar a quilometragem inicial com viagens no histórico."
            }
            showToast(msg)
            return
        }
        _uiState.update { it.copy(isInitialOdometerDialogOpen = open) }
    }

    fun saveInitialOdometer(totalKm: Double, hevKm: Double) {
        if (_uiState.value.odometerEntries.isNotEmpty()) {
            val msg = if (_uiState.value.language == AppLanguage.EN_US) {
                "Cannot edit initial mileage while trip history exists."
            } else {
                "Não é possível editar a quilometragem inicial com viagens no histórico."
            }
            showToast(msg)
            return
        }
        val cleanTotal = ((totalKm * 10.0).roundToInt() / 10.0).coerceAtLeast(0.0)
        val cleanHev = ((hevKm * 10.0).roundToInt() / 10.0).coerceAtLeast(0.0)
        _uiState.update {
            it.copy(
                odometerTotalStartKm = cleanTotal,
                odometerHevStartKm = cleanHev,
                odometerTotalEndKm = 0.0,
                odometerHevEndKm = 0.0,
                odometerFuelLiters = 0.0,
                isInitialOdometerDialogOpen = false
            )
        }
        saveOdometerDraft()
        val msg = if (_uiState.value.language == AppLanguage.EN_US) {
            "Initial baseline mileage saved!"
        } else {
            "Quilometragem inicial salva com sucesso!"
        }
        showToast(msg)
    }

    fun removeInitialOdometer() {
        if (_uiState.value.odometerEntries.isNotEmpty()) {
            val msg = if (_uiState.value.language == AppLanguage.EN_US) {
                "Cannot remove initial mileage while trip history exists."
            } else {
                "Não é possível remover a quilometragem inicial com viagens no histórico."
            }
            showToast(msg)
            return
        }
        _uiState.update {
            it.copy(
                odometerTotalStartKm = 0.0,
                odometerHevStartKm = 0.0
            )
        }
        saveOdometerDraft()
        val msg = if (_uiState.value.language == AppLanguage.EN_US) {
            "Initial baseline mileage removed."
        } else {
            "Quilometragem inicial removida com sucesso."
        }
        showToast(msg)
    }

    fun updateOdometerTotalStartKm(km: Double) {
        val rounded = ((km * 10.0).roundToInt() / 10.0).coerceAtLeast(0.0)
        _uiState.update { it.copy(odometerTotalStartKm = rounded) }
        saveOdometerDraft()
    }

    fun updateOdometerTotalEndKm(km: Double) {
        val rounded = ((km * 10.0).roundToInt() / 10.0).coerceAtLeast(0.0)
        _uiState.update { it.copy(odometerTotalEndKm = rounded) }
        saveOdometerDraft()
    }

    fun updateOdometerHevStartKm(km: Double) {
        val rounded = ((km * 10.0).roundToInt() / 10.0).coerceAtLeast(0.0)
        _uiState.update { it.copy(odometerHevStartKm = rounded) }
        saveOdometerDraft()
    }

    fun updateOdometerHevEndKm(km: Double) {
        val rounded = ((km * 10.0).roundToInt() / 10.0).coerceAtLeast(0.0)
        _uiState.update { it.copy(odometerHevEndKm = rounded) }
        saveOdometerDraft()
    }

    private fun saveOdometerDraft() {
        val s = _uiState.value
        prefs.saveOdometerDraft(
            totalKm = s.odometerTotalKm,
            hevKm = s.odometerHevKm,
            useHomeTariff = s.odometerUseHomeTariff
        )
        prefs.saveOdometerDraft(
            totalStartKm = s.odometerTotalStartKm,
            totalEndKm = s.odometerTotalEndKm,
            hevStartKm = s.odometerHevStartKm,
            hevEndKm = s.odometerHevEndKm,
            fuelLiters = s.odometerFuelLiters,
            tripNote = s.odometerTripNote
        )
    }

    fun resetOdometerInputs() {
        _uiState.update {
            it.copy(
                odometerTotalKm = 0.0,
                odometerHevKm = 0.0,
                odometerTotalStartKm = 0.0,
                odometerTotalEndKm = 0.0,
                odometerHevStartKm = 0.0,
                odometerHevEndKm = 0.0,
                odometerFuelLiters = 0.0,
                odometerTripNote = ""
            )
        }
        saveOdometerDraft()
        showToast(if (_uiState.value.language == AppLanguage.EN_US) "Inputs reset!" else "Odômetro zerado!")
    }

    fun openSaveOdometerTripDialog(open: Boolean) {
        _uiState.update { it.copy(isSaveOdometerTripDialogOpen = open) }
    }

    fun saveCurrentOdometerTrip(customTitle: String = "") {
        val s = _uiState.value
        val defaultTitle = if (s.language == AppLanguage.EN_US) {
            "Trip ${s.selectedVehicle.name}"
        } else {
            "Viagem ${s.selectedVehicle.name}"
        }
        val finalTitle = when {
            customTitle.isNotBlank() -> customTitle.trim()
            s.odometerTripNote.isNotBlank() -> s.odometerTripNote.trim()
            else -> defaultTitle
        }

        val entry = OdometerEntry(
            title = finalTitle,
            vehicleName = s.selectedVehicle.name,
            totalKm = s.odometerDeltaTotalKm,
            evKm = s.odometerDeltaEvKm,
            hevKm = s.odometerDeltaHevKm,
            fuelLiters = s.odometerFuelLiters,
            averageHevKmL = s.odometerAverageHevKmL,
            averageGlobalKmL = s.odometerGlobalKmL,
            totalStartKm = s.odometerTotalStartKm,
            totalEndKm = s.odometerTotalEndKm,
            hevStartKm = s.odometerHevStartKm,
            hevEndKm = s.odometerHevEndKm,
            electricConsumptionKwh100km = s.odometerEffectiveElectricConsumption,
            gasolineConsumptionKmL = if (s.odometerAverageHevKmL > 0) s.odometerAverageHevKmL else s.odometerEffectiveGasolineConsumption,
            energyPriceKwh = s.odometerEffectiveEnergyPrice,
            gasPriceLiter = s.gasolinePrice,
            chargingLocation = if (s.odometerUseHomeTariff) ChargingLocation.HOME else ChargingLocation.STATION,
            batteryCapacityKwh = s.selectedVehicle.batteryCapacityKwh
        )

        val updatedEntries = listOf(entry) + s.odometerEntries
        prefs.saveOdometerEntries(updatedEntries)

        // Deixa salvo no campo anterior o valor final do último histórico registrado
        val nextStartTotal = if (s.odometerTotalEndKm > 0) s.odometerTotalEndKm else s.odometerTotalStartKm
        val nextStartHev = if (s.odometerHevEndKm > 0) s.odometerHevEndKm else s.odometerHevStartKm

        _uiState.update {
            it.copy(
                odometerEntries = updatedEntries,
                isSaveOdometerTripDialogOpen = false,
                odometerTotalStartKm = nextStartTotal,
                odometerTotalEndKm = 0.0,
                odometerHevStartKm = nextStartHev,
                odometerHevEndKm = 0.0,
                odometerFuelLiters = 0.0,
                odometerTripNote = ""
            )
        }
        saveOdometerDraft()
        showToast(s.strings.odometerTripSavedSuccess)
    }

    fun applyOdometerEntryAsPrevious(entry: OdometerEntry) {
        val nextStartTotal = if (entry.totalEndKm > 0) entry.totalEndKm else entry.totalStartKm
        val nextStartHev = if (entry.hevEndKm > 0) entry.hevEndKm else entry.hevStartKm

        _uiState.update {
            it.copy(
                odometerTotalStartKm = nextStartTotal,
                odometerTotalEndKm = 0.0,
                odometerHevStartKm = nextStartHev,
                odometerHevEndKm = 0.0,
                odometerFuelLiters = 0.0
            )
        }
        saveOdometerDraft()
        showToast("Odômetros finais aplicados como anteriores!")
    }

    fun deleteOdometerEntry(id: String) {
        val updated = _uiState.value.odometerEntries.filter { it.id != id }
        prefs.saveOdometerEntries(updated)
        _uiState.update { it.copy(odometerEntries = updated) }
    }

    fun updateOdometerEntry(
        id: String,
        title: String,
        totalStartKm: Double,
        totalEndKm: Double,
        hevStartKm: Double,
        hevEndKm: Double,
        fuelLiters: Double,
        chargingLocation: ChargingLocation
    ) {
        val deltaTotal = (totalEndKm - totalStartKm).coerceAtLeast(0.0)
        val deltaHev = (hevEndKm - hevStartKm).coerceAtLeast(0.0)
        val deltaEv = (deltaTotal - deltaHev).coerceAtLeast(0.0)
        val avgHevKmL = if (fuelLiters > 0 && deltaHev > 0) deltaHev / fuelLiters else 0.0
        val avgGlobalKmL = if (fuelLiters > 0 && deltaTotal > 0) deltaTotal / fuelLiters else 0.0

        val homeTariff = _uiState.value.homeEnergyPrice
        val publicTariff = _uiState.value.publicEnergyPrice

        val updatedList = _uiState.value.odometerEntries.map { entry ->
            if (entry.id == id) {
                val newEnergyPrice = if (chargingLocation == ChargingLocation.HOME) {
                    if (entry.chargingLocation == ChargingLocation.HOME && entry.energyPriceKwh > 0) entry.energyPriceKwh else homeTariff
                } else {
                    if (entry.chargingLocation == ChargingLocation.STATION && entry.energyPriceKwh > 0) entry.energyPriceKwh else publicTariff
                }

                entry.copy(
                    title = title,
                    totalStartKm = totalStartKm,
                    totalEndKm = totalEndKm,
                    hevStartKm = hevStartKm,
                    hevEndKm = hevEndKm,
                    totalKm = deltaTotal,
                    hevKm = deltaHev,
                    evKm = deltaEv,
                    fuelLiters = fuelLiters,
                    averageHevKmL = avgHevKmL,
                    averageGlobalKmL = avgGlobalKmL,
                    gasolineConsumptionKmL = if (avgHevKmL > 0) avgHevKmL else entry.gasolineConsumptionKmL,
                    chargingLocation = chargingLocation,
                    energyPriceKwh = newEnergyPrice,
                    batteryCapacityKwh = if (entry.batteryCapacityKwh > 0) entry.batteryCapacityKwh else _uiState.value.selectedVehicle.batteryCapacityKwh
                )
            } else {
                entry
            }
        }
        prefs.saveOdometerEntries(updatedList)
        _uiState.update { it.copy(odometerEntries = updatedList) }
        showToast("Medição atualizada com sucesso!")
    }

    fun clearAllOdometerEntries() {
        prefs.saveOdometerEntries(emptyList())
        _uiState.update { it.copy(odometerEntries = emptyList()) }
        showToast(_uiState.value.strings.odometerHistoryCleared)
    }

    fun clearOdometerHistory() {
        clearAllOdometerEntries()
    }

    fun generateOdometerShareText(): String {
        val s = _uiState.value
        val isEn = s.language == AppLanguage.EN_US
        val v = s.selectedVehicle
        return buildString {
            appendLine(if (isEn) "🚗 *PHEV ESTIMATE REPORT*" else "🚗 *RELATÓRIO ESTIMATIVA PHEV*")
            appendLine("🚘 *${v.name}* (${v.tag})")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine(if (isEn) "📏 Total Distance: ${formatNumber(s.odometerEffectiveTotalKm, 1, s.language)} km" else "📏 Distância Total: ${formatNumber(s.odometerEffectiveTotalKm, 1, s.language)} km")
            appendLine(if (isEn) "  • Previous: ${formatNumber(s.odometerTotalStartKm, 1, s.language)} km | Current: ${formatNumber(s.odometerTotalEndKm, 1, s.language)} km" else "  • Anterior: ${formatNumber(s.odometerTotalStartKm, 1, s.language)} km | Atual: ${formatNumber(s.odometerTotalEndKm, 1, s.language)} km")
            appendLine(if (isEn) "⚡ Electric (EV): ${formatNumber(s.odometerEffectiveEvKm, 1, s.language)} km (${formatNumber(s.odometerEvPercent, 1, s.language)}%)" else "⚡ Modo Elétrico (EV): ${formatNumber(s.odometerEffectiveEvKm, 1, s.language)} km (${formatNumber(s.odometerEvPercent, 1, s.language)}%)")
            appendLine(if (isEn) "⛽ Hybrid (HEV): ${formatNumber(s.odometerEffectiveHevKm, 1, s.language)} km (${formatNumber(s.odometerHevPercent, 1, s.language)}%)" else "⛽ Modo Híbrido (HEV): ${formatNumber(s.odometerEffectiveHevKm, 1, s.language)} km (${formatNumber(s.odometerHevPercent, 1, s.language)}%)")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine(if (isEn) "💰 Total Trip Cost: ${formatCurrency(s.odometerTotalTripCost, s.language)}" else "💰 Custo Total do Trajeto: ${formatCurrency(s.odometerTotalTripCost, s.language)}")
            appendLine(if (isEn) "  • Electric: ${formatCurrency(s.odometerElectricCost, s.language)} (${formatNumber(s.odometerTotalEnergyKwh, 1, s.language)} kWh)" else "  • Elétrico: ${formatCurrency(s.odometerElectricCost, s.language)} (${formatNumber(s.odometerTotalEnergyKwh, 1, s.language)} kWh)")
            appendLine(if (isEn) "  • Gasoline: ${formatCurrency(s.odometerGasolineCost, s.language)} (${formatNumber(s.odometerTotalGasolineLiters, 1, s.language)} L)" else "  • Gasolina: ${formatCurrency(s.odometerGasolineCost, s.language)} (${formatNumber(s.odometerTotalGasolineLiters, 1, s.language)} L)")
            appendLine(if (isEn) "📊 Cost per km: ${formatCurrency(s.odometerCostPerKm, s.language)}/km (${formatCurrency(s.odometerCostPer100Km, s.language)} / 100 km)" else "📊 Custo por km: ${formatCurrency(s.odometerCostPerKm, s.language)}/km (${formatCurrency(s.odometerCostPer100Km, s.language)} / 100 km)")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine("⚡ *Voltage - PHEV Calculator*")
        }
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
