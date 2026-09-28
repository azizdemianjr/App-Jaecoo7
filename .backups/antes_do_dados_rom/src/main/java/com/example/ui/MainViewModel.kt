package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppPreferences
import com.example.data.ChargingLocation
import com.example.data.OdometerEntry
import com.example.data.Vehicle
import com.example.data.VehicleCatalog
import com.example.data.VehicleOdometerDraft
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
    val odometerFilterVehicleOnly: Boolean = true,
    val odometerTotalKm: Double = 0.0,
    val odometerHevKm: Double = 0.0,
    val odometerCustomElectricConsumption: Double = 0.0,
    val odometerCustomGasolineConsumption: Double = 0.0,
    val odometerUseHomeTariff: Boolean = true,
    val odometerChargingLocation: ChargingLocation = ChargingLocation.HOME,
    val odometerTotalStartKm: Double = 0.0,
    val odometerTotalEndKm: Double = 0.0,
    val odometerHevStartKm: Double = 0.0,
    val odometerHevEndKm: Double = 0.0,
    val odometerFuelLiters: Double = 0.0,
    val odometerTripNote: String = "",
    val odometerBatteryStartPercent: Double = 100.0,
    val odometerBatteryMaxPercent: Double = 75.0,
    val odometerRechargeCount: Int = 1,
    val odometerRechargeLocations: List<ChargingLocation> = listOf(ChargingLocation.HOME),
    val odometerRechargeBatteryPercents: List<Double> = emptyList(),
    val odometerRechargeInitialBatteryPercents: List<Double> = emptyList(),
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
    val pixKey: String = "48565a78-f69d-4a0f-917d-f327331b6c6c",
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

    // Odometer / Estimativa calculations (Trip odometer & fuel measurement)
    val odometerDeltaTotalKm: Double
        get() = if (odometerTotalEndKm > odometerTotalStartKm && odometerTotalEndKm > 0.0) {
            odometerTotalEndKm - odometerTotalStartKm
        } else 0.0

    val odometerDeltaHevKm: Double
        get() = when (selectedVehicle.type) {
            VehicleType.BEV -> 0.0
            VehicleType.HEV -> odometerDeltaTotalKm
            VehicleType.PHEV -> if (odometerDeltaTotalKm > 0.0 && odometerHevEndKm >= odometerHevStartKm && odometerHevEndKm > 0.0) {
                odometerHevEndKm - odometerHevStartKm
            } else 0.0
        }

    val odometerEvStartKm: Double
        get() = when (selectedVehicle.type) {
            VehicleType.BEV -> odometerTotalStartKm
            VehicleType.HEV -> 0.0
            VehicleType.PHEV -> if (odometerTotalStartKm > 0.0) (odometerTotalStartKm - odometerHevStartKm).coerceAtLeast(0.0) else 0.0
        }

    val odometerEvEndKm: Double
        get() = when (selectedVehicle.type) {
            VehicleType.BEV -> odometerTotalEndKm
            VehicleType.HEV -> 0.0
            VehicleType.PHEV -> if (odometerTotalEndKm > 0.0) (odometerTotalEndKm - odometerHevEndKm).coerceAtLeast(0.0) else 0.0
        }

    val odometerDeltaEvKm: Double
        get() = when (selectedVehicle.type) {
            VehicleType.BEV -> odometerDeltaTotalKm
            VehicleType.HEV -> 0.0
            VehicleType.PHEV -> if (odometerDeltaTotalKm > 0.0) (odometerDeltaTotalKm - odometerDeltaHevKm).coerceAtLeast(0.0) else 0.0
        }

    val odometerEffectiveTotalKm: Double
        get() = if (odometerDeltaTotalKm > 0) odometerDeltaTotalKm else 0.0

    val odometerEffectiveHevKm: Double
        get() = when (selectedVehicle.type) {
            VehicleType.BEV -> 0.0
            VehicleType.HEV -> odometerEffectiveTotalKm
            VehicleType.PHEV -> if (odometerEffectiveTotalKm <= 0.0) 0.0 else odometerDeltaHevKm.coerceAtMost(odometerEffectiveTotalKm)
        }

    val odometerEffectiveEvKm: Double
        get() = when (selectedVehicle.type) {
            VehicleType.BEV -> odometerEffectiveTotalKm
            VehicleType.HEV -> 0.0
            VehicleType.PHEV -> if (odometerEffectiveTotalKm <= 0.0) 0.0 else (odometerEffectiveTotalKm - odometerEffectiveHevKm).coerceAtLeast(0.0)
        }

    val odometerEffectiveRechargeLocations: List<ChargingLocation>
        get() {
            val count = odometerRechargeCount.coerceAtLeast(1)
            if (odometerRechargeLocations.isEmpty()) {
                return List(count) { odometerChargingLocation }
            }
            if (odometerRechargeLocations.size < count) {
                val last = odometerRechargeLocations.lastOrNull() ?: odometerChargingLocation
                return odometerRechargeLocations + List(count - odometerRechargeLocations.size) { last }
            }
            return odometerRechargeLocations.take(count)
        }

    val odometerEffectiveRechargeInitialBatteryPercents: List<Double>
        get() {
            val count = odometerRechargeCount.coerceAtLeast(1)
            if (odometerRechargeInitialBatteryPercents.isEmpty()) {
                return List(count) { 25.0 }
            }
            if (odometerRechargeInitialBatteryPercents.size < count) {
                val last = odometerRechargeInitialBatteryPercents.lastOrNull() ?: 25.0
                return odometerRechargeInitialBatteryPercents + List(count - odometerRechargeInitialBatteryPercents.size) { last }
            }
            return odometerRechargeInitialBatteryPercents.take(count)
        }

    val odometerEffectiveRechargeBatteryPercents: List<Double>
        get() {
            val count = odometerRechargeCount.coerceAtLeast(1)
            if (odometerRechargeBatteryPercents.isEmpty()) {
                return List(count) { 100.0 }
            }
            if (odometerRechargeBatteryPercents.size < count) {
                val last = odometerRechargeBatteryPercents.lastOrNull() ?: 100.0
                return odometerRechargeBatteryPercents + List(count - odometerRechargeBatteryPercents.size) { last }
            }
            return odometerRechargeBatteryPercents.take(count)
        }

    val odometerEffectiveEnergyPrice: Double
        get() {
            val locs = odometerEffectiveRechargeLocations
            val total = locs.sumOf {
                when (it) {
                    ChargingLocation.HOME -> homeEnergyPrice
                    ChargingLocation.STATION -> publicEnergyPrice
                    ChargingLocation.NONE -> 0.0
                }
            }
            return if (locs.isNotEmpty()) total / locs.size else homeEnergyPrice
        }

    val odometerEffectiveElectricConsumption: Double
        get() = if (odometerCustomElectricConsumption > 0) {
            odometerCustomElectricConsumption
        } else if (selectedVehicle.electricConsumptionKwh100km > 0) {
            selectedVehicle.electricConsumptionKwh100km
        } else {
            15.0
        }

    val odometerEffectiveGasolineConsumption: Double
        get() = if (odometerCustomGasolineConsumption > 0) {
            odometerCustomGasolineConsumption
        } else if (selectedVehicle.gasolineConsumptionKmL > 0) {
            selectedVehicle.gasolineConsumptionKmL
        } else {
            16.0
        }

    val odometerEvReservePercent: Double
        get() = 25.0

    val odometerUsableBatteryPercent: Double
        get() = (odometerBatteryStartPercent - odometerEvReservePercent).coerceIn(0.0, 100.0)

    // Distância EV máxima que a recarga externa da bateria pode fornecer
    val odometerMaxRechargeEvKm: Double
        get() {
            if (selectedVehicle.type == VehicleType.HEV) return 0.0
            if (selectedVehicle.type == VehicleType.BEV) {
                val hasRecharge = odometerEffectiveRechargeLocations.any { it != ChargingLocation.NONE }
                if (!hasRecharge) {
                    return if (odometerEffectiveElectricConsumption > 0) (selectedVehicle.batteryCapacityKwh / odometerEffectiveElectricConsumption) * 100.0 else odometerEffectiveTotalKm
                }
            }
            val hasRecharge = odometerEffectiveRechargeLocations.any { it != ChargingLocation.NONE }
            if (!hasRecharge || odometerEffectiveElectricConsumption <= 0.0) {
                return 0.0
            }
            val locs = odometerEffectiveRechargeLocations
            val finals = odometerEffectiveRechargeBatteryPercents
            val reservePercent = if (selectedVehicle.type == VehicleType.PHEV) odometerEvReservePercent else 0.0
            var totalUsableKwh = 0.0
            for (i in locs.indices) {
                if (locs[i] != ChargingLocation.NONE) {
                    val pFinal = finals.getOrElse(i) { 100.0 }
                    val usableP = (pFinal - reservePercent).coerceIn(0.0, 100.0)
                    totalUsableKwh += (selectedVehicle.batteryCapacityKwh * (usableP / 100.0))
                }
            }
            return (totalUsableKwh / odometerEffectiveElectricConsumption) * 100.0
        }

    // Km elétricos realmente supridos pela recarga da tomada
    val odometerRechargeEvKm: Double
        get() = when (selectedVehicle.type) {
            VehicleType.HEV -> 0.0
            VehicleType.BEV, VehicleType.PHEV -> if (odometerEffectiveTotalKm <= 0.0) 0.0 else odometerEffectiveEvKm.coerceAtMost(odometerMaxRechargeEvKm)
        }

    // Km elétricos adicionais (além da recarga da tomada), que excedem a capacidade de recarga
    val odometerExcessEvKm: Double
        get() = when (selectedVehicle.type) {
            VehicleType.HEV -> 0.0
            VehicleType.BEV, VehicleType.PHEV -> if (odometerEffectiveTotalKm <= 0.0) 0.0 else (odometerEffectiveEvKm - odometerRechargeEvKm).coerceAtLeast(0.0)
        }

    // Indica se o trajeto rodou em modo elétrico mas a distância EV excedeu a capacidade de recarga
    val odometerIsPureEvWithExcess: Boolean
        get() = (selectedVehicle.type == VehicleType.BEV || (selectedVehicle.type == VehicleType.PHEV && odometerEffectiveHevKm <= 0.0)) && odometerEffectiveTotalKm > 0.0 && odometerExcessEvKm > 0.0

    // Km totais sustentados pelo combustível (HEV informado + EV excedente que foi gerado queimando combustível).
    // Se o trajeto foi 100% elétrico (odometerEffectiveHevKm <= 0.0), o motor não funcionou, logo 0 km foram sustentados por combustível.
    val odometerEffectiveFuelKm: Double
        get() = when (selectedVehicle.type) {
            VehicleType.BEV -> 0.0
            VehicleType.HEV -> odometerEffectiveTotalKm
            VehicleType.PHEV -> if (odometerEffectiveTotalKm <= 0.0 || odometerEffectiveHevKm <= 0.0) 0.0
                else (odometerEffectiveHevKm + odometerExcessEvKm).coerceAtLeast(0.0)
        }

    // Média de consumo HEV (km/L): Km sustentados por combustível ÷ Litros abastecidos/utilizados
    val odometerAverageHevKmL: Double
        get() = when (selectedVehicle.type) {
            VehicleType.BEV -> 0.0
            VehicleType.HEV -> if (odometerFuelLiters > 0 && odometerDeltaTotalKm > 0) {
                odometerDeltaTotalKm / odometerFuelLiters
            } else if (selectedVehicle.gasolineConsumptionKmL > 0) {
                selectedVehicle.gasolineConsumptionKmL
            } else 0.0
            VehicleType.PHEV -> if (odometerFuelLiters > 0 && odometerEffectiveFuelKm > 0 && odometerDeltaTotalKm > 0) {
                odometerEffectiveFuelKm / odometerFuelLiters
            } else 0.0
        }

    val odometerAverageHevL100km: Double
        get() = if (odometerAverageHevKmL > 0) {
            100.0 / odometerAverageHevKmL
        } else 0.0

    val odometerGlobalKmL: Double
        get() = if (selectedVehicle.type == VehicleType.BEV) 0.0
        else if (odometerFuelLiters > 0 && odometerDeltaTotalKm > 0) {
            odometerDeltaTotalKm / odometerFuelLiters
        } else 0.0

    val odometerTotalEnergyKwh: Double
        get() {
            if (odometerEffectiveTotalKm <= 0.0 || selectedVehicle.type == VehicleType.HEV) return 0.0
            val hasRecharge = odometerEffectiveRechargeLocations.any { it != ChargingLocation.NONE }
            if (selectedVehicle.type == VehicleType.BEV && !hasRecharge) {
                return odometerEffectiveEvKm * odometerEffectiveElectricConsumption / 100.0
            }
            if (!hasRecharge) return 0.0
            val locs = odometerEffectiveRechargeLocations
            val finals = odometerEffectiveRechargeBatteryPercents
            val reservePercent = if (selectedVehicle.type == VehicleType.PHEV) odometerEvReservePercent else 0.0
            var totalMaxKwh = 0.0
            for (i in locs.indices) {
                if (locs[i] != ChargingLocation.NONE) {
                    val pFinal = finals.getOrElse(i) { 100.0 }
                    val usableP = (pFinal - reservePercent).coerceIn(0.0, 100.0)
                    totalMaxKwh += (selectedVehicle.batteryCapacityKwh * (usableP / 100.0))
                }
            }
            if (totalMaxKwh <= 0.0) return 0.0
            val rawKwh = odometerRechargeEvKm * odometerEffectiveElectricConsumption / 100.0
            return rawKwh.coerceAtMost(totalMaxKwh)
        }

    val odometerTotalGasolineLiters: Double
        get() = when (selectedVehicle.type) {
            VehicleType.BEV -> 0.0
            VehicleType.HEV -> if (odometerFuelLiters > 0) odometerFuelLiters
                else if (selectedVehicle.gasolineConsumptionKmL > 0 && odometerEffectiveTotalKm > 0) odometerEffectiveTotalKm / selectedVehicle.gasolineConsumptionKmL
                else 0.0
            VehicleType.PHEV -> if (odometerEffectiveTotalKm <= 0.0) 0.0
                else if (odometerFuelLiters > 0) odometerFuelLiters
                else if (odometerEffectiveHevKm <= 0.0) 0.0
                else if (odometerEffectiveGasolineConsumption > 0 && odometerEffectiveFuelKm > 0) {
                    odometerEffectiveFuelKm / odometerEffectiveGasolineConsumption
                } else 0.0
        }

    val odometerElectricCost: Double
        get() {
            if (odometerEffectiveTotalKm <= 0.0 || selectedVehicle.type == VehicleType.HEV) return 0.0
            val locs = odometerEffectiveRechargeLocations
            val finals = odometerEffectiveRechargeBatteryPercents
            val inits = odometerEffectiveRechargeInitialBatteryPercents
            if (locs.isEmpty() || locs.all { it == ChargingLocation.NONE }) {
                return if (selectedVehicle.type == VehicleType.BEV) {
                    odometerTotalEnergyKwh * homeEnergyPrice
                } else 0.0
            }
            var remainingKwh = odometerTotalEnergyKwh
            var cost = 0.0
            for (i in locs.indices) {
                if (remainingKwh <= 0.0) break
                val loc = locs[i]
                val pInit = inits.getOrElse(i) { 25.0 }
                val pFinal = finals.getOrElse(i) { 100.0 }
                val usableP = (pFinal - pInit).coerceIn(0.0, 100.0)
                val kwhPerRecharge = if (selectedVehicle.batteryCapacityKwh > 0) {
                    selectedVehicle.batteryCapacityKwh * (usableP / 100.0)
                } else 0.0
                val kwh = if (kwhPerRecharge > 0) remainingKwh.coerceAtMost(kwhPerRecharge) else remainingKwh
                val price = when (loc) {
                    ChargingLocation.HOME -> homeEnergyPrice
                    ChargingLocation.STATION -> publicEnergyPrice
                    ChargingLocation.NONE -> 0.0
                }
                cost += kwh * price
                remainingKwh -= kwh
            }
            return cost
        }

    val odometerGasolineCost: Double
        get() = if (selectedVehicle.type == VehicleType.BEV || odometerEffectiveTotalKm <= 0.0) 0.0
        else if (selectedVehicle.type == VehicleType.HEV) odometerTotalGasolineLiters * gasolinePrice
        else if (odometerEffectiveHevKm <= 0.0 && odometerFuelLiters <= 0.0) 0.0
        else odometerTotalGasolineLiters * gasolinePrice

    val odometerTotalTripCost: Double
        get() = if (odometerEffectiveTotalKm <= 0.0) 0.0 else odometerElectricCost + odometerGasolineCost

    val odometerCostPerKm: Double
        get() = if (odometerEffectiveTotalKm > 0) odometerTotalTripCost / odometerEffectiveTotalKm else 0.0

    val odometerCostPer100Km: Double
        get() = odometerCostPerKm * 100.0

    val odometerEquivalentKmL: Double
        get() = if (odometerTotalTripCost > 0 && gasolinePrice > 0 && odometerEffectiveTotalKm > 0) {
            odometerEffectiveTotalKm / (odometerTotalTripCost / gasolinePrice)
        } else 0.0

    val odometerCost100PercentGas: Double
        get() {
            if (odometerEffectiveTotalKm <= 0.0) return 0.0
            val effGas = when (selectedVehicle.type) {
                VehicleType.BEV -> 12.0
                VehicleType.HEV -> 10.0
                VehicleType.PHEV -> if (selectedVehicle.gasolineConsumptionKmL > 0) selectedVehicle.gasolineConsumptionKmL else customComparisonGasKmL
            }
            return if (effGas > 0) (odometerEffectiveTotalKm / effGas) * gasolinePrice else 0.0
        }

    val odometerSavingsVsGas: Double
        get() = if (odometerEffectiveTotalKm <= 0.0) 0.0 else (odometerCost100PercentGas - odometerTotalTripCost).coerceAtLeast(0.0)

    val odometerSavingsPercent: Double
        get() = if (odometerEffectiveTotalKm <= 0.0 || odometerCost100PercentGas <= 0) {
            0.0
        } else {
            ((odometerSavingsVsGas / odometerCost100PercentGas) * 100.0).coerceIn(0.0, 100.0)
        }

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

    // Filtered odometer entries (by vehicle or all)
    val currentVehicleOdometerEntries: List<OdometerEntry>
        get() = if (odometerFilterVehicleOnly) {
            odometerEntries.filter { it.matchesVehicle(selectedVehicle) }
        } else {
            odometerEntries
        }

    // Accumulated history metrics
    val odometerAccumulatedTotalKm: Double
        get() = currentVehicleOdometerEntries.sumOf { it.totalKm }

    val odometerAccumulatedEvKm: Double
        get() = currentVehicleOdometerEntries.sumOf { it.evKm }

    val odometerAccumulatedSavings: Double
        get() = currentVehicleOdometerEntries.sumOf { it.savingsVsGas }

    val odometerAccumulatedTotalCost: Double
        get() = currentVehicleOdometerEntries.sumOf { it.totalCost }

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

        // Limpar dados legados de mock ou se o total atual for menor ou igual ao inicial
        if ((savedOdometerTotalStartKm == 12000.0 && savedOdometerTotalEndKm == 12650.0) ||
            (savedOdometerTotalEndKm == 1000.0 && savedOdometerHevEndKm == 100.0) ||
            (savedOdometerTotalEndKm <= savedOdometerTotalStartKm)
        ) {
            savedOdometerTotalEndKm = 0.0
            savedOdometerHevEndKm = 0.0
            savedOdometerFuelLiters = 0.0
            prefs.saveOdometerDraft(savedOdometerTotalStartKm, 0.0, savedOdometerHevStartKm, 0.0, 0.0, "")
        }

        val vehicleDraft = prefs.getVehicleOdometerDraft(selected.id)
        if (vehicleDraft != null) {
            savedOdometerTotalStartKm = vehicleDraft.totalStartKm
            savedOdometerTotalEndKm = vehicleDraft.totalEndKm
            savedOdometerHevStartKm = vehicleDraft.hevStartKm
            savedOdometerHevEndKm = vehicleDraft.hevEndKm
            savedOdometerFuelLiters = vehicleDraft.fuelLiters
        } else {
            val vehicleEntries = savedOdometerEntries.filter { it.matchesVehicle(selected) }
            val lastTrip = vehicleEntries.maxByOrNull { it.timestamp }
            if (lastTrip != null && savedOdometerTotalStartKm == 0.0) {
                savedOdometerTotalStartKm = if (lastTrip.totalEndKm > 0) lastTrip.totalEndKm else lastTrip.totalStartKm
                savedOdometerHevStartKm = if (selected.type == VehicleType.PHEV) {
                    if (lastTrip.hevEndKm > 0) lastTrip.hevEndKm else lastTrip.hevStartKm
                } else 0.0
            }
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
            odometerBatteryStartPercent = prefs.getOdometerBatteryStartPercent(),
            odometerBatteryMaxPercent = (prefs.getOdometerBatteryStartPercent() - 25.0).coerceAtLeast(0.0),
            odometerChargingLocation = prefs.getOdometerChargingLocation(),
            odometerUseHomeTariff = (prefs.getOdometerChargingLocation() == ChargingLocation.HOME),
            odometerRechargeCount = prefs.getOdometerRechargeCount(),
            odometerRechargeLocations = prefs.getOdometerRechargeLocations(),
            odometerRechargeBatteryPercents = prefs.getOdometerRechargeBatteryPercents(),
            odometerRechargeInitialBatteryPercents = prefs.getOdometerRechargeInitialBatteryPercents(),
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
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun updateHomeEnergyPrice(price: Double) {
        val rounded = (price * 100.0).roundToInt() / 100.0
        val clamped = rounded.coerceAtLeast(0.0)
        prefs.saveHomeEnergyPrice(clamped)
        _uiState.update { it.copy(homeEnergyPrice = clamped) }
    }

    fun stepHomeEnergyPrice(delta: Double) {
        val currentPrice = _uiState.value.homeEnergyPrice
        val newPrice = ((currentPrice + delta).coerceAtLeast(0.0) * 100.0).roundToInt() / 100.0
        prefs.saveHomeEnergyPrice(newPrice)
        _uiState.update { it.copy(homeEnergyPrice = newPrice) }
    }

    fun updatePublicEnergyPrice(price: Double) {
        val rounded = (price * 100.0).roundToInt() / 100.0
        val clamped = rounded.coerceAtLeast(0.0)
        prefs.savePublicEnergyPrice(clamped)
        _uiState.update { it.copy(publicEnergyPrice = clamped) }
    }

    fun stepPublicEnergyPrice(delta: Double) {
        val currentPrice = _uiState.value.publicEnergyPrice
        val newPrice = ((currentPrice + delta).coerceAtLeast(0.0) * 100.0).roundToInt() / 100.0
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
        val currentVehicle = _uiState.value.selectedVehicle
        if (currentVehicle.id.isNotBlank()) {
            val s = _uiState.value
            prefs.saveVehicleOdometerDraft(
                vehicleId = currentVehicle.id,
                totalStartKm = s.odometerTotalStartKm,
                totalEndKm = s.odometerTotalEndKm,
                hevStartKm = s.odometerHevStartKm,
                hevEndKm = s.odometerHevEndKm,
                fuelLiters = s.odometerFuelLiters,
                tripNote = s.odometerTripNote
            )
        }

        prefs.saveSelectedVehicleId(vehicle.id)

        val vehicleDraft = prefs.getVehicleOdometerDraft(vehicle.id)
        val (newStartTotal, newEndTotal, newStartHev, newEndHev, newFuel, newNote) = if (vehicleDraft != null) {
            vehicleDraft
        } else {
            val vehicleEntries = _uiState.value.odometerEntries.filter { it.matchesVehicle(vehicle) }
            val lastTrip = vehicleEntries.maxByOrNull { it.timestamp }
            if (lastTrip != null) {
                val startTot = if (lastTrip.totalEndKm > 0) lastTrip.totalEndKm else lastTrip.totalStartKm
                val startHev = if (vehicle.type == VehicleType.PHEV) {
                    if (lastTrip.hevEndKm > 0) lastTrip.hevEndKm else lastTrip.hevStartKm
                } else 0.0
                VehicleOdometerDraft(totalStartKm = startTot, hevStartKm = startHev)
            } else {
                VehicleOdometerDraft()
            }
        }

        _uiState.update {
            it.copy(
                selectedVehicle = vehicle,
                odometerCustomElectricConsumption = 0.0,
                odometerCustomGasolineConsumption = 0.0,
                odometerTotalStartKm = newStartTotal,
                odometerTotalEndKm = newEndTotal,
                odometerHevStartKm = if (vehicle.type == VehicleType.PHEV) newStartHev else 0.0,
                odometerHevEndKm = if (vehicle.type == VehicleType.PHEV) newEndHev else 0.0,
                odometerFuelLiters = newFuel,
                odometerTripNote = newNote,
                isChangeVehicleDialogOpen = false
            )
        }
        saveOdometerDraft()
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
                selectedVehicle = defaultSelected,
                odometerCustomElectricConsumption = 0.0,
                odometerCustomGasolineConsumption = 0.0
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
                odometerCustomElectricConsumption = 0.0,
                odometerCustomGasolineConsumption = 0.0,
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
                odometerCustomElectricConsumption = 0.0,
                odometerCustomGasolineConsumption = 0.0,
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

    fun setOdometerChargingLocation(location: ChargingLocation) {
        _uiState.update { s ->
            val count = s.odometerRechargeCount.coerceAtLeast(1)
            val updatedLocs = if (count == 1) listOf(location) else s.odometerEffectiveRechargeLocations
            s.copy(
                odometerChargingLocation = location,
                odometerUseHomeTariff = (location == ChargingLocation.HOME),
                odometerRechargeLocations = updatedLocs
            )
        }
        prefs.saveOdometerChargingLocation(location)
        prefs.saveOdometerRechargeLocations(_uiState.value.odometerRechargeLocations)
        saveOdometerDraft()
    }

    fun setOdometerUseHomeTariff(useHome: Boolean) {
        val location = if (useHome) ChargingLocation.HOME else ChargingLocation.STATION
        setOdometerChargingLocation(location)
    }

    fun updateOdometerRechargeLocation(index: Int, location: ChargingLocation) {
        _uiState.update { s ->
            val count = s.odometerRechargeCount.coerceAtLeast(1)
            val current = s.odometerEffectiveRechargeLocations.toMutableList()
            while (current.size < count) {
                current.add(s.odometerChargingLocation)
            }
            if (index in 0 until count) {
                current[index] = location
            }
            val newPrimary = if (index == 0) location else s.odometerChargingLocation
            s.copy(
                odometerChargingLocation = newPrimary,
                odometerUseHomeTariff = (newPrimary == ChargingLocation.HOME),
                odometerRechargeLocations = current
            )
        }
        prefs.saveOdometerChargingLocation(_uiState.value.odometerChargingLocation)
        prefs.saveOdometerRechargeLocations(_uiState.value.odometerRechargeLocations)
        saveOdometerDraft()
    }

    fun setAllOdometerRechargeLocations(location: ChargingLocation) {
        _uiState.update { s ->
            val count = s.odometerRechargeCount.coerceAtLeast(1)
            val allList = List(count) { location }
            s.copy(
                odometerChargingLocation = location,
                odometerUseHomeTariff = (location == ChargingLocation.HOME),
                odometerRechargeLocations = allList
            )
        }
        prefs.saveOdometerChargingLocation(location)
        prefs.saveOdometerRechargeLocations(_uiState.value.odometerRechargeLocations)
        saveOdometerDraft()
    }

    fun updateOdometerFuelLiters(liters: Double) {
        val rounded = ((liters * 10.0).roundToInt() / 10.0).coerceAtLeast(0.0)
        _uiState.update { it.copy(odometerFuelLiters = rounded) }
        saveOdometerDraft()
    }

    fun updateOdometerRechargeCount(count: Int) {
        val safe = count.coerceAtLeast(1)
        _uiState.update { s ->
            val currentLocs = s.odometerEffectiveRechargeLocations
            val newLocs = when {
                currentLocs.size == safe -> currentLocs
                currentLocs.size < safe -> {
                    val last = currentLocs.lastOrNull() ?: s.odometerChargingLocation
                    currentLocs + List(safe - currentLocs.size) { last }
                }
                else -> currentLocs.take(safe)
            }
            val currentPercents = s.odometerEffectiveRechargeBatteryPercents
            val newPercents = when {
                currentPercents.size == safe -> currentPercents
                currentPercents.size < safe -> {
                    val last = currentPercents.lastOrNull() ?: 100.0
                    currentPercents + List(safe - currentPercents.size) { last }
                }
                else -> currentPercents.take(safe)
            }
            val currentInitPercents = s.odometerEffectiveRechargeInitialBatteryPercents
            val newInitPercents = when {
                currentInitPercents.size == safe -> currentInitPercents
                currentInitPercents.size < safe -> {
                    val last = currentInitPercents.lastOrNull() ?: 25.0
                    currentInitPercents + List(safe - currentInitPercents.size) { last }
                }
                else -> currentInitPercents.take(safe)
            }
            s.copy(
                odometerRechargeCount = safe,
                odometerRechargeLocations = newLocs,
                odometerRechargeBatteryPercents = newPercents,
                odometerRechargeInitialBatteryPercents = newInitPercents
            )
        }
        prefs.saveOdometerRechargeCount(safe)
        prefs.saveOdometerRechargeLocations(_uiState.value.odometerRechargeLocations)
        prefs.saveOdometerRechargeBatteryPercents(_uiState.value.odometerRechargeBatteryPercents)
        prefs.saveOdometerRechargeInitialBatteryPercents(_uiState.value.odometerRechargeInitialBatteryPercents)
    }

    fun stepOdometerRechargeCount(delta: Int) {
        val current = _uiState.value.odometerRechargeCount
        updateOdometerRechargeCount(current + delta)
    }

    fun updateOdometerRechargeInitialBatteryPercent(index: Int, percent: Double) {
        val rounded = ((percent * 10.0).roundToInt() / 10.0).coerceIn(0.0, 100.0)
        _uiState.update { s ->
            val count = s.odometerRechargeCount.coerceAtLeast(1)
            val current = s.odometerEffectiveRechargeInitialBatteryPercents.toMutableList()
            while (current.size < count) {
                current.add(25.0)
            }
            if (index in current.indices) {
                current[index] = rounded
            }
            s.copy(
                odometerRechargeInitialBatteryPercents = current
            )
        }
        prefs.saveOdometerRechargeInitialBatteryPercents(_uiState.value.odometerRechargeInitialBatteryPercents)
        saveOdometerDraft()
    }

    fun stepOdometerRechargeInitialBatteryPercent(index: Int, delta: Double) {
        val currentList = _uiState.value.odometerEffectiveRechargeInitialBatteryPercents
        val currentVal = currentList.getOrElse(index) { 25.0 }
        updateOdometerRechargeInitialBatteryPercent(index, currentVal + delta)
    }

    fun updateOdometerRechargeBatteryPercent(index: Int, percent: Double) {
        val rounded = ((percent * 10.0).roundToInt() / 10.0).coerceIn(0.0, 100.0)
        _uiState.update { s ->
            val count = s.odometerRechargeCount.coerceAtLeast(1)
            val current = s.odometerEffectiveRechargeBatteryPercents.toMutableList()
            while (current.size < count) {
                current.add(100.0)
            }
            if (index in current.indices) {
                current[index] = rounded
            }
            val newPrimary = if (index == 0) rounded else s.odometerBatteryStartPercent
            val newMax = (newPrimary - 25.0).coerceAtLeast(0.0)
            s.copy(
                odometerBatteryStartPercent = newPrimary,
                odometerBatteryMaxPercent = newMax,
                odometerRechargeBatteryPercents = current
            )
        }
        prefs.saveOdometerBatteryStartPercent(_uiState.value.odometerBatteryStartPercent)
        prefs.saveOdometerBatteryMaxPercent(_uiState.value.odometerBatteryMaxPercent)
        prefs.saveOdometerRechargeBatteryPercents(_uiState.value.odometerRechargeBatteryPercents)
        saveOdometerDraft()
    }

    fun stepOdometerRechargeBatteryPercent(index: Int, delta: Double) {
        val currentList = _uiState.value.odometerEffectiveRechargeBatteryPercents
        val currentVal = currentList.getOrElse(index) { 100.0 }
        updateOdometerRechargeBatteryPercent(index, currentVal + delta)
    }

    fun updateOdometerRechargeFinalBatteryPercent(index: Int, percent: Double) {
        updateOdometerRechargeBatteryPercent(index, percent)
    }

    fun stepOdometerRechargeFinalBatteryPercent(index: Int, delta: Double) {
        stepOdometerRechargeBatteryPercent(index, delta)
    }

    fun updateOdometerTripNote(note: String) {
        _uiState.update { it.copy(odometerTripNote = note) }
        saveOdometerDraft()
    }

    fun updateOdometerBatteryStartPercent(percent: Double) {
        val rounded = ((percent * 10.0).roundToInt() / 10.0).coerceIn(0.0, 100.0)
        val usableMax = (rounded - 25.0).coerceAtLeast(0.0)
        _uiState.update { s ->
            val count = s.odometerRechargeCount.coerceAtLeast(1)
            val current = s.odometerEffectiveRechargeBatteryPercents.toMutableList()
            while (current.size < count) {
                current.add(rounded)
            }
            if (current.isNotEmpty()) {
                current[0] = rounded
            }
            s.copy(
                odometerBatteryStartPercent = rounded,
                odometerBatteryMaxPercent = usableMax,
                odometerRechargeBatteryPercents = current
            )
        }
        prefs.saveOdometerBatteryStartPercent(rounded)
        prefs.saveOdometerBatteryMaxPercent(usableMax)
        prefs.saveOdometerRechargeBatteryPercents(_uiState.value.odometerRechargeBatteryPercents)
    }

    fun stepOdometerBatteryStartPercent(delta: Double) {
        val current = _uiState.value.odometerBatteryStartPercent
        updateOdometerBatteryStartPercent(current + delta)
    }

    fun resetOdometerBatteryStartPercent() {
        updateOdometerBatteryStartPercent(100.0)
    }

    fun updateOdometerBatteryMaxPercent(percent: Double) {
        val startVal = (percent + 25.0).coerceIn(0.0, 100.0)
        updateOdometerBatteryStartPercent(startVal)
    }

    fun resetOdometerBatteryMaxPercent() {
        updateOdometerBatteryStartPercent(100.0)
    }

    fun openOdometerHistoryDialog(open: Boolean) {
        _uiState.update { it.copy(isOdometerHistoryDialogOpen = open) }
    }

    fun openInitialOdometerDialog(open: Boolean) {
        if (open && _uiState.value.currentVehicleOdometerEntries.isNotEmpty()) {
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
        if (_uiState.value.currentVehicleOdometerEntries.isNotEmpty()) {
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
        val vehicleId = _uiState.value.selectedVehicle.id
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
        prefs.saveVehicleOdometerDraft(vehicleId, cleanTotal, 0.0, cleanHev, 0.0, 0.0, "")
        saveOdometerDraft()
        val msg = if (_uiState.value.language == AppLanguage.EN_US) {
            "Initial baseline mileage saved!"
        } else {
            "Quilometragem inicial salva com sucesso!"
        }
        showToast(msg)
    }

    fun removeInitialOdometer() {
        if (_uiState.value.currentVehicleOdometerEntries.isNotEmpty()) {
            val msg = if (_uiState.value.language == AppLanguage.EN_US) {
                "Cannot remove initial mileage while trip history exists."
            } else {
                "Não é possível remover a quilometragem inicial com viagens no histórico."
            }
            showToast(msg)
            return
        }
        val vehicleId = _uiState.value.selectedVehicle.id
        _uiState.update {
            it.copy(
                odometerTotalStartKm = 0.0,
                odometerHevStartKm = 0.0
            )
        }
        prefs.saveVehicleOdometerDraft(vehicleId, 0.0, 0.0, 0.0, 0.0, 0.0, "")
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
                odometerTripNote = "",
                odometerChargingLocation = ChargingLocation.HOME,
                odometerUseHomeTariff = true,
                odometerBatteryStartPercent = 100.0,
                odometerBatteryMaxPercent = 75.0,
                odometerRechargeCount = 1
            )
        }
        prefs.saveOdometerChargingLocation(ChargingLocation.HOME)
        prefs.saveOdometerRechargeCount(1)
        saveOdometerDraft()
        showToast(if (_uiState.value.language == AppLanguage.EN_US) "Inputs reset!" else "Odômetro zerado!")
    }

    fun limitTripToMaxRechargeRange() {
        val s = _uiState.value
        val maxKm = s.odometerMaxRechargeEvKm
        if (maxKm > 0) {
            val cleanMax = ((maxKm * 10.0).roundToInt() / 10.0)
            val newEnd = s.odometerTotalStartKm + cleanMax
            updateOdometerTotalEndKm(newEnd)
            val msg = if (s.language == AppLanguage.EN_US) {
                "Mileage limited to max recharge range: ${formatNumber(cleanMax, 1, s.language)} km"
            } else {
                "Quilometragem limitada à autonomia máxima da carga: ${formatNumber(cleanMax, 1, s.language)} km"
            }
            showToast(msg)
        }
    }

    fun autoAdjustRechargeCountForTrip() {
        val s = _uiState.value
        val neededDist = s.odometerEffectiveTotalKm
        if (neededDist <= 0 || s.odometerEffectiveElectricConsumption <= 0) return

        val pFinal = s.odometerEffectiveRechargeBatteryPercents.firstOrNull() ?: 100.0
        val reservePercent = if (s.selectedVehicle.type == VehicleType.PHEV) s.odometerEvReservePercent else 0.0
        val usableP = (pFinal - reservePercent).coerceIn(5.0, 100.0)
        val kwhPerRecharge = s.selectedVehicle.batteryCapacityKwh * (usableP / 100.0)
        val rangePerRecharge = if (kwhPerRecharge > 0) (kwhPerRecharge / s.odometerEffectiveElectricConsumption) * 100.0 else 200.0

        val requiredCount = kotlin.math.ceil(neededDist / rangePerRecharge).toInt().coerceAtLeast(1)
        updateOdometerRechargeCount(requiredCount)

        val msg = if (s.language == AppLanguage.EN_US) {
            "Adjusted to $requiredCount recharges to cover ${formatNumber(neededDist, 1, s.language)} km"
        } else {
            "Ajustado para $requiredCount recargas para cobrir os ${formatNumber(neededDist, 1, s.language)} km"
        }
        showToast(msg)
    }

    fun openSaveOdometerTripDialog(open: Boolean) {
        _uiState.update { it.copy(isSaveOdometerTripDialogOpen = open) }
    }

    fun saveCurrentOdometerTrip(customTitle: String = "") {
        val s = _uiState.value
        if (s.selectedVehicle.type == VehicleType.BEV && s.odometerExcessEvKm > 0.0) {
            val msg = if (s.language == AppLanguage.EN_US) {
                "Cannot save: ${formatNumber(s.odometerDeltaTotalKm, 1, s.language)} km exceeds max recharge range (${formatNumber(s.odometerMaxRechargeEvKm, 1, s.language)} km). Please adjust recharges or limit mileage."
            } else {
                "Não é possível salvar: ${formatNumber(s.odometerDeltaTotalKm, 1, s.language)} km excede a autonomia máxima (${formatNumber(s.odometerMaxRechargeEvKm, 1, s.language)} km) para ${s.odometerRechargeCount} recarga(s). Ajuste as recargas ou limite a quilometragem."
            }
            showToast(msg)
            return
        }

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
            vehicleId = s.selectedVehicle.id,
            vehicleName = s.selectedVehicle.name,
            vehicleType = s.selectedVehicle.type,
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
            chargingLocation = s.odometerChargingLocation,
            batteryCapacityKwh = s.selectedVehicle.batteryCapacityKwh,
            batteryMaxPercent = s.odometerUsableBatteryPercent,
            batteryStartPercent = s.odometerBatteryStartPercent,
            rechargeCount = s.odometerRechargeCount,
            rechargeLocations = s.odometerEffectiveRechargeLocations,
            rechargeBatteryPercents = s.odometerEffectiveRechargeBatteryPercents,
            rechargeInitialBatteryPercents = s.odometerEffectiveRechargeInitialBatteryPercents,
            homeEnergyPrice = s.homeEnergyPrice,
            publicEnergyPrice = s.publicEnergyPrice
        )

        val updatedEntries = listOf(entry) + s.odometerEntries
        prefs.saveOdometerEntries(updatedEntries)

        // Deixa salvo no campo anterior o valor final do último histórico registrado
        val nextStartTotal = if (s.odometerTotalEndKm > 0) s.odometerTotalEndKm else s.odometerTotalStartKm
        val nextStartHev = if (s.selectedVehicle.type == VehicleType.PHEV) {
            if (s.odometerHevEndKm > 0) s.odometerHevEndKm else s.odometerHevStartKm
        } else 0.0

        prefs.saveVehicleOdometerDraft(
            vehicleId = s.selectedVehicle.id,
            totalStartKm = nextStartTotal,
            totalEndKm = 0.0,
            hevStartKm = nextStartHev,
            hevEndKm = 0.0,
            fuelLiters = 0.0,
            tripNote = ""
        )

        _uiState.update {
            it.copy(
                odometerEntries = updatedEntries,
                isSaveOdometerTripDialogOpen = false,
                odometerTotalStartKm = nextStartTotal,
                odometerTotalEndKm = 0.0,
                odometerHevStartKm = nextStartHev,
                odometerHevEndKm = 0.0,
                odometerFuelLiters = 0.0,
                odometerTripNote = "",
                odometerChargingLocation = ChargingLocation.HOME,
                odometerUseHomeTariff = true,
                odometerBatteryStartPercent = 100.0,
                odometerBatteryMaxPercent = 75.0,
                odometerRechargeCount = 1,
                odometerRechargeLocations = listOf(ChargingLocation.HOME),
                odometerRechargeBatteryPercents = listOf(100.0)
            )
        }
        prefs.saveOdometerChargingLocation(ChargingLocation.HOME)
        prefs.saveOdometerRechargeCount(1)
        prefs.saveOdometerRechargeLocations(listOf(ChargingLocation.HOME))
        prefs.saveOdometerRechargeBatteryPercents(listOf(100.0))
        saveOdometerDraft()
        showToast(s.strings.odometerTripSavedSuccess)
    }

    fun applyOdometerEntryAsPrevious(entry: OdometerEntry) {
        val nextStartTotal = if (entry.totalEndKm > 0) entry.totalEndKm else entry.totalStartKm
        val nextStartHev = if (entry.hevEndKm > 0) entry.hevEndKm else entry.hevStartKm

        val currentVehicleId = _uiState.value.selectedVehicle.id
        prefs.saveVehicleOdometerDraft(
            vehicleId = currentVehicleId,
            totalStartKm = nextStartTotal,
            totalEndKm = 0.0,
            hevStartKm = nextStartHev,
            hevEndKm = 0.0,
            fuelLiters = 0.0,
            tripNote = ""
        )

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
        chargingLocation: ChargingLocation,
        batteryStartPercent: Double = 100.0,
        batteryMaxPercent: Double = 75.0,
        rechargeCount: Int = 1,
        rechargeLocations: List<ChargingLocation> = emptyList(),
        rechargeBatteryPercents: List<Double> = emptyList(),
        rechargeInitialBatteryPercents: List<Double> = emptyList()
    ) {
        val deltaTotal = (totalEndKm - totalStartKm).coerceAtLeast(0.0)
        val deltaHev = (hevEndKm - hevStartKm).coerceAtLeast(0.0)
        val deltaEv = (deltaTotal - deltaHev).coerceAtLeast(0.0)

        val homeTariff = _uiState.value.homeEnergyPrice
        val publicTariff = _uiState.value.publicEnergyPrice

        val actualStartPercent = if (batteryStartPercent > 0.0) batteryStartPercent else (batteryMaxPercent + 25.0).coerceIn(0.0, 100.0)
        val actualMaxPercent = (actualStartPercent - 25.0).coerceAtLeast(0.0)
        val safeRechargeCount = rechargeCount.coerceAtLeast(1)

        val safeLocations = if (rechargeLocations.isNotEmpty()) {
            if (rechargeLocations.size < safeRechargeCount) {
                val last = rechargeLocations.lastOrNull() ?: chargingLocation
                rechargeLocations + List(safeRechargeCount - rechargeLocations.size) { last }
            } else {
                rechargeLocations.take(safeRechargeCount)
            }
        } else {
            List(safeRechargeCount) { chargingLocation }
        }

        val safePercents = if (rechargeBatteryPercents.isNotEmpty()) {
            if (rechargeBatteryPercents.size < safeRechargeCount) {
                val last = rechargeBatteryPercents.lastOrNull() ?: 100.0
                rechargeBatteryPercents + List(safeRechargeCount - rechargeBatteryPercents.size) { last }
            } else {
                rechargeBatteryPercents.take(safeRechargeCount)
            }
        } else {
            List(safeRechargeCount) { 100.0 }
        }

        val safeInitPercents = if (rechargeInitialBatteryPercents.isNotEmpty()) {
            if (rechargeInitialBatteryPercents.size < safeRechargeCount) {
                val last = rechargeInitialBatteryPercents.lastOrNull() ?: 25.0
                rechargeInitialBatteryPercents + List(safeRechargeCount - rechargeInitialBatteryPercents.size) { last }
            } else {
                rechargeInitialBatteryPercents.take(safeRechargeCount)
            }
        } else {
            List(safeRechargeCount) { 25.0 }
        }

        val updatedList = _uiState.value.odometerEntries.map { entry ->
            if (entry.id == id) {
                val newEnergyPrice = when (chargingLocation) {
                    ChargingLocation.HOME -> if (entry.chargingLocation == ChargingLocation.HOME && entry.energyPriceKwh > 0) entry.energyPriceKwh else homeTariff
                    ChargingLocation.STATION -> if (entry.chargingLocation == ChargingLocation.STATION && entry.energyPriceKwh > 0) entry.energyPriceKwh else publicTariff
                    ChargingLocation.NONE -> 0.0
                }

                val batCap = if (entry.batteryCapacityKwh > 0) entry.batteryCapacityKwh else _uiState.value.selectedVehicle.batteryCapacityKwh
                val elecCons = if (entry.electricConsumptionKwh100km > 0) entry.electricConsumptionKwh100km else _uiState.value.odometerEffectiveElectricConsumption
                val reservePercent = if (entry.vehicleType == VehicleType.PHEV) 25.0 else 0.0
                var totalUsableKwh = 0.0
                for (i in safeLocations.indices) {
                    if (safeLocations[i] != ChargingLocation.NONE) {
                        val pFinal = safePercents.getOrElse(i) { 100.0 }
                        val usableP = (pFinal - reservePercent).coerceIn(0.0, 100.0)
                        totalUsableKwh += (batCap * (usableP / 100.0))
                    }
                }
                val maxRechargeEvKm = if (elecCons > 0) (totalUsableKwh / elecCons) * 100.0 else 0.0
                val rechargeEvKm = deltaEv.coerceAtMost(maxRechargeEvKm)
                val excessEvKm = (deltaEv - rechargeEvKm).coerceAtLeast(0.0)
                val effectiveFuelKm = if (deltaHev <= 0.0) 0.0 else deltaHev + excessEvKm

                val avgHevKmL = if (fuelLiters > 0 && effectiveFuelKm > 0) effectiveFuelKm / fuelLiters else 0.0
                val avgGlobalKmL = if (fuelLiters > 0 && deltaTotal > 0) deltaTotal / fuelLiters else 0.0

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
                    rechargeLocations = safeLocations,
                    rechargeBatteryPercents = safePercents,
                    rechargeInitialBatteryPercents = safeInitPercents,
                    homeEnergyPrice = homeTariff,
                    publicEnergyPrice = publicTariff,
                    energyPriceKwh = newEnergyPrice,
                    batteryCapacityKwh = if (entry.batteryCapacityKwh > 0) entry.batteryCapacityKwh else _uiState.value.selectedVehicle.batteryCapacityKwh,
                    batteryMaxPercent = actualMaxPercent,
                    batteryStartPercent = actualStartPercent,
                    rechargeCount = safeRechargeCount
                )
            } else {
                entry
            }
        }
        prefs.saveOdometerEntries(updatedList)
        _uiState.update { it.copy(odometerEntries = updatedList) }
        showToast("Medição atualizada com sucesso!")
    }

    fun setOdometerFilterVehicleOnly(vehicleOnly: Boolean) {
        _uiState.update { it.copy(odometerFilterVehicleOnly = vehicleOnly) }
    }

    fun clearCurrentVehicleOdometerEntries() {
        val currentVehicle = _uiState.value.selectedVehicle
        val updated = _uiState.value.odometerEntries.filterNot { it.matchesVehicle(currentVehicle) }
        prefs.saveOdometerEntries(updated)
        prefs.saveVehicleOdometerDraft(currentVehicle.id, 0.0, 0.0, 0.0, 0.0, 0.0, "")
        _uiState.update {
            it.copy(
                odometerEntries = updated,
                odometerTotalStartKm = 0.0,
                odometerTotalEndKm = 0.0,
                odometerHevStartKm = 0.0,
                odometerHevEndKm = 0.0,
                odometerFuelLiters = 0.0,
                odometerTripNote = ""
            )
        }
        val msg = if (_uiState.value.language == AppLanguage.EN_US) {
            "History for ${currentVehicle.name} cleared successfully!"
        } else {
            "Histórico de ${currentVehicle.name} limpo com sucesso!"
        }
        showToast(msg)
    }

    fun clearAllOdometerEntries() {
        prefs.saveOdometerEntries(emptyList())
        _uiState.update {
            it.copy(
                odometerEntries = emptyList(),
                odometerTotalStartKm = 0.0,
                odometerTotalEndKm = 0.0,
                odometerHevStartKm = 0.0,
                odometerHevEndKm = 0.0,
                odometerFuelLiters = 0.0,
                odometerTripNote = ""
            )
        }
        showToast(_uiState.value.strings.odometerHistoryCleared)
    }

    fun clearOdometerHistory() {
        if (_uiState.value.odometerFilterVehicleOnly) {
            clearCurrentVehicleOdometerEntries()
        } else {
            clearAllOdometerEntries()
        }
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
            if (s.odometerExcessEvKm > 0) {
                appendLine(if (isEn) "  ↳ Plug Recharge: ${formatNumber(s.odometerRechargeEvKm, 1, s.language)} km | Engine/Regen: ${formatNumber(s.odometerExcessEvKm, 1, s.language)} km" else "  ↳ Recarga da Tomada: ${formatNumber(s.odometerRechargeEvKm, 1, s.language)} km | Motor/Regen: ${formatNumber(s.odometerExcessEvKm, 1, s.language)} km")
            }
            appendLine(if (isEn) "⛽ Hybrid (HEV): ${formatNumber(s.odometerEffectiveHevKm, 1, s.language)} km (${formatNumber(s.odometerHevPercent, 1, s.language)}%)" else "⛽ Modo Híbrido (HEV): ${formatNumber(s.odometerEffectiveHevKm, 1, s.language)} km (${formatNumber(s.odometerHevPercent, 1, s.language)}%)")
            if (s.odometerExcessEvKm > 0) {
                appendLine(if (isEn) "  ↳ Effective Fuel Distance: ${formatNumber(s.odometerEffectiveFuelKm, 1, s.language)} km" else "  ↳ Km Efetivos a Combustível: ${formatNumber(s.odometerEffectiveFuelKm, 1, s.language)} km")
            }
            if (s.odometerAverageHevKmL > 0) {
                appendLine(if (isEn) "⛽ Average HEV Consumption: ${formatNumber(s.odometerAverageHevKmL, 2, s.language)} km/L" else "⛽ Consumo Médio HEV: ${formatNumber(s.odometerAverageHevKmL, 2, s.language)} km/L")
            }
            if (s.odometerGlobalKmL > 0) {
                appendLine(if (isEn) "🌐 Global Trip Consumption: ${formatNumber(s.odometerGlobalKmL, 2, s.language)} km/L" else "🌐 Consumo Médio Global: ${formatNumber(s.odometerGlobalKmL, 2, s.language)} km/L")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine(if (isEn) "💰 Total Trip Cost: ${formatCurrency(s.odometerTotalTripCost, s.language)}" else "💰 Custo Total do Trajeto: ${formatCurrency(s.odometerTotalTripCost, s.language)}")

            val homeCount = s.odometerEffectiveRechargeLocations.count { it == ChargingLocation.HOME }
            val stationCount = s.odometerEffectiveRechargeLocations.count { it == ChargingLocation.STATION }
            val rechBreakdown = if (s.odometerRechargeCount > 1) {
                if (homeCount > 0 && stationCount > 0) {
                    if (isEn) " (${homeCount}x Home, ${stationCount}x Station)" else " (${homeCount}x Casa, ${stationCount}x Posto)"
                } else if (homeCount > 0) {
                    if (isEn) " (${homeCount}x Home)" else " (${homeCount}x Casa)"
                } else {
                    if (isEn) " (${stationCount}x Station)" else " (${stationCount}x Posto)"
                }
            } else ""

            appendLine(if (isEn) "  • Electric: ${formatCurrency(s.odometerElectricCost, s.language)} (${formatNumber(s.odometerTotalEnergyKwh, 1, s.language)} kWh$rechBreakdown)" else "  • Elétrico: ${formatCurrency(s.odometerElectricCost, s.language)} (${formatNumber(s.odometerTotalEnergyKwh, 1, s.language)} kWh$rechBreakdown)")
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
