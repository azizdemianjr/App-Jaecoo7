package com.example.data

import com.example.ui.localization.AppLanguage
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ChargingLocation {
    HOME,
    STATION,
    NONE;

    fun label(language: AppLanguage): String {
        return when (this) {
            HOME -> if (language == AppLanguage.EN_US) "Home" else "Em Casa"
            STATION -> if (language == AppLanguage.EN_US) "Charging Station" else "Posto de Recarga"
            NONE -> if (language == AppLanguage.EN_US) "No Recharge (EV Limit)" else "Sem Recarga (Bateria no Limite)"
        }
    }
}

data class VehicleOdometerDraft(
    val totalStartKm: Double = 0.0,
    val totalEndKm: Double = 0.0,
    val hevStartKm: Double = 0.0,
    val hevEndKm: Double = 0.0,
    val fuelLiters: Double = 0.0,
    val tripNote: String = ""
)

data class OdometerEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val vehicleId: String = "",
    val vehicleName: String,
    val vehicleType: VehicleType = VehicleType.PHEV,
    val totalKm: Double,
    val evKm: Double,
    val hevKm: Double,
    val electricConsumptionKwh100km: Double = 18.0,
    val gasolineConsumptionKmL: Double = 16.0,
    val energyPriceKwh: Double = 1.30,
    val gasPriceLiter: Double = 6.50,
    val fuelLiters: Double = 0.0,
    val averageHevKmL: Double = 0.0,
    val averageGlobalKmL: Double = 0.0,
    val totalStartKm: Double = 0.0,
    val totalEndKm: Double = 0.0,
    val hevStartKm: Double = 0.0,
    val hevEndKm: Double = 0.0,
    val chargingLocation: ChargingLocation = ChargingLocation.HOME,
    val batteryCapacityKwh: Double = 18.3,
    val batteryMaxPercent: Double = 75.0,
    val batteryStartPercent: Double = if (batteryMaxPercent <= 0.0) 25.0 else (batteryMaxPercent + 25.0).coerceAtMost(100.0),
    val rechargeCount: Int = 1,
    val rechargeLocations: List<ChargingLocation> = emptyList(),
    val rechargeBatteryPercents: List<Double> = emptyList(),
    val rechargeInitialBatteryPercents: List<Double> = emptyList(),
    val rechargePrices: List<Double> = emptyList(),
    val homeEnergyPrice: Double = 0.0,
    val publicEnergyPrice: Double = 0.0
) {
    val effectiveRechargeLocations: List<ChargingLocation>
        get() {
            val count = rechargeCount.coerceAtLeast(1)
            if (rechargeLocations.isEmpty()) {
                return List(count) { chargingLocation }
            }
            if (rechargeLocations.size < count) {
                val last = rechargeLocations.lastOrNull() ?: chargingLocation
                return rechargeLocations + List(count - rechargeLocations.size) { last }
            }
            return rechargeLocations.take(count)
        }

    val effectiveRechargeInitialBatteryPercents: List<Double>
        get() {
            val count = rechargeCount.coerceAtLeast(1)
            if (rechargeInitialBatteryPercents.isEmpty()) {
                return List(count) { 25.0 }
            }
            if (rechargeInitialBatteryPercents.size < count) {
                val last = rechargeInitialBatteryPercents.lastOrNull() ?: 25.0
                return rechargeInitialBatteryPercents + List(count - rechargeInitialBatteryPercents.size) { last }
            }
            return rechargeInitialBatteryPercents.take(count)
        }

    val effectiveRechargeBatteryPercents: List<Double>
        get() {
            val count = rechargeCount.coerceAtLeast(1)
            if (rechargeBatteryPercents.isEmpty()) {
                return List(count) { 100.0 }
            }
            if (rechargeBatteryPercents.size < count) {
                return rechargeBatteryPercents + List(count - rechargeBatteryPercents.size) { 100.0 }
            }
            return rechargeBatteryPercents.take(count)
        }

    val effectiveRechargePrices: List<Double>
        get() {
            val count = rechargeCount.coerceAtLeast(1)
            val locs = effectiveRechargeLocations
            val fallbackHome = if (homeEnergyPrice > 0.0) homeEnergyPrice else 1.30
            val fallbackPublic = if (publicEnergyPrice > 0.0) publicEnergyPrice else 2.10
            return List(count) { i ->
                val custom = rechargePrices.getOrNull(i)
                if (custom != null && custom >= 0.0) {
                    custom
                } else {
                    when (locs.getOrElse(i) { ChargingLocation.HOME }) {
                        ChargingLocation.HOME -> fallbackHome
                        ChargingLocation.STATION -> fallbackPublic
                        ChargingLocation.NONE -> 0.0
                    }
                }
            }
        }

    // Ev reserve limit for PHEVs (standard 25% where combustion ICE turns on)
    val evReservePercent: Double
        get() = 25.0

    // Usable battery percentage from the grid: from batteryStartPercent down to evReservePercent (25%), or totalUsableBatteryPercent for multi-recharge
    val usableBatteryPercent: Double
        get() = if (rechargeCount > 1) totalUsableBatteryPercent else (batteryStartPercent - evReservePercent).coerceIn(0.0, 100.0)

    // Distância EV máxima que a recarga externa da bateria pode fornecer
    val totalUsableBatteryPercent: Double
        get() {
            val locs = effectiveRechargeLocations
            val finals = effectiveRechargeBatteryPercents
            val inits = effectiveRechargeInitialBatteryPercents
            val reservePercent = if (vehicleType == VehicleType.PHEV) 25.0 else 0.0
            val activeIndices = locs.indices.filter { locs[it] != ChargingLocation.NONE }
            if (activeIndices.isEmpty()) return 0.0
            var totalP = 0.0
            for (idx in activeIndices.indices) {
                val i = activeIndices[idx]
                val pFinal = finals.getOrElse(i) { 100.0 }
                val usableP = if (idx == activeIndices.size - 1) {
                    (pFinal - reservePercent).coerceIn(0.0, 100.0)
                } else {
                    val nextI = activeIndices[idx + 1]
                    val nextInit = inits.getOrElse(nextI) { 0.0 }
                    val deltaToNext = (pFinal - nextInit).coerceAtLeast(0.0)
                    val leg0 = if (idx == 0) {
                        val pInit = inits.getOrElse(i) { 25.0 }
                        if (pFinal < 100.0) {
                            (100.0 - pFinal).coerceAtLeast(0.0)
                        } else if (pInit > reservePercent) {
                            (100.0 - pInit).coerceAtLeast(0.0)
                        } else {
                            0.0
                        }
                    } else {
                        0.0
                    }
                    leg0 + deltaToNext
                }
                totalP += usableP
            }
            return totalP
        }

    val maxRechargeEvKm: Double
        get() {
            if (vehicleType == VehicleType.HEV) return 0.0
            val consumption = if (electricConsumptionKwh100km > 0.0) electricConsumptionKwh100km else 18.0
            val batteryCap = if (batteryCapacityKwh > 0.0) batteryCapacityKwh else 18.3
            val hasRecharge = effectiveRechargeLocations.any { it != ChargingLocation.NONE }
            if (!hasRecharge) {
                return 0.0
            }
            val totalUsableKwh = batteryCap * (totalUsableBatteryPercent / 100.0)
            return if (totalUsableKwh > 0.0) (totalUsableKwh / consumption) * 100.0 else 0.0
        }

    // Km elétricos realmente supridos pela recarga da tomada
    val rechargeEvKm: Double
        get() = when (vehicleType) {
            VehicleType.HEV -> 0.0
            VehicleType.BEV, VehicleType.PHEV -> if (totalKm <= 0.0) 0.0 else evKm.coerceAtMost(maxRechargeEvKm)
        }

    // Km elétricos adicionais (além da recarga da tomada), que excedem a capacidade de recarga (EV gerado pelo motor e regeneração)
    val excessEvKm: Double
        get() = when (vehicleType) {
            VehicleType.HEV -> 0.0
            VehicleType.BEV, VehicleType.PHEV -> if (totalKm <= 0.0) 0.0 else (evKm - rechargeEvKm).coerceAtLeast(0.0)
        }

    // Indica se o trajeto foi em modo elétrico mas a quilometragem excedeu a capacidade de recarga
    val isPureEvWithExcess: Boolean
        get() = (vehicleType == VehicleType.BEV || (vehicleType == VehicleType.PHEV && hevKm <= 0.0 && fuelLiters <= 0.0)) && totalKm > 0.0 && excessEvKm > 0.0

    // Km totais sustentados pelo combustível (HEV informado + EV gerado pelo motor a combustão e regeneração).
    val effectiveFuelKm: Double
        get() = when (vehicleType) {
            VehicleType.BEV -> 0.0
            VehicleType.HEV -> totalKm
            VehicleType.PHEV -> when {
                hevKm > 0.0 -> (hevKm + excessEvKm).coerceAtLeast(0.0)
                fuelLiters > 0.0 && totalKm > 0.0 -> (totalKm - rechargeEvKm).coerceAtLeast(0.0)
                else -> 0.0
            }
        }

    // Percentages (Modo de condução conforme exibido no painel do veículo)
    val evPercent: Double
        get() = if (totalKm > 0) ((evKm / totalKm) * 100.0).coerceIn(0.0, 100.0) else 0.0

    val hevPercent: Double
        get() = if (totalKm > 0) ((hevKm / totalKm) * 100.0).coerceIn(0.0, 100.0) else 0.0

    // Effective real consumption from fuel liters if available, else theoretical (0.0 if no fuel driven)
    val realHevKmL: Double
        get() = when {
            effectiveFuelKm <= 0.0 -> 0.0
            fuelLiters > 0.0 -> effectiveFuelKm / fuelLiters
            averageHevKmL > 0.0 -> averageHevKmL
            else -> gasolineConsumptionKmL
        }

    val realHevL100km: Double
        get() = if (effectiveFuelKm > 0.0 && realHevKmL > 0.0) 100.0 / realHevKmL else 0.0

    val realGlobalKmL: Double
        get() = if (averageGlobalKmL > 0) averageGlobalKmL else if (fuelLiters > 0 && totalKm > 0) totalKm / fuelLiters else 0.0

    // Consumptions and costs: gasto elétrico limitado pela capacidade de bateria da recarga
    val totalEnergyKwh: Double
        get() {
            if (vehicleType == VehicleType.HEV) return 0.0
            val consumption = if (electricConsumptionKwh100km > 0.0) electricConsumptionKwh100km else 18.0
            val batteryCap = if (batteryCapacityKwh > 0.0) batteryCapacityKwh else 18.3
            val hasRecharge = effectiveRechargeLocations.any { it != ChargingLocation.NONE }

            // Se for BEV calcula consumo direto; se for PHEV sem recarga na tomada, a rede externa forneceu 0 kWh
            if (!hasRecharge) {
                return if (vehicleType == VehicleType.BEV && evKm > 0.0) (evKm * consumption / 100.0) else 0.0
            }

            val totalMaxKwh = batteryCap * (totalUsableBatteryPercent / 100.0)
            val rawKwh = if (evKm > 0.0) (evKm * consumption / 100.0) else 0.0
            return if (totalMaxKwh > 0.0) rawKwh.coerceAtMost(totalMaxKwh) else if (vehicleType == VehicleType.BEV) rawKwh else 0.0
        }

    val totalGasolineLiters: Double
        get() = when {
            vehicleType == VehicleType.BEV -> 0.0
            fuelLiters > 0.0 -> fuelLiters
            vehicleType == VehicleType.HEV -> {
                val effKmL = if (realHevKmL > 0.0) realHevKmL else if (gasolineConsumptionKmL > 0.0) gasolineConsumptionKmL else 14.0
                if (effKmL > 0.0) totalKm / effKmL else 0.0
            }
            hevKm <= 0.0 -> 0.0
            effectiveFuelKm > 0.0 -> {
                val effKmL = if (realHevKmL > 0.0) realHevKmL else if (gasolineConsumptionKmL > 0.0) gasolineConsumptionKmL else 14.0
                if (effKmL > 0.0) effectiveFuelKm / effKmL else 0.0
            }
            else -> 0.0
        }

    val electricCost: Double
        get() {
            if (vehicleType == VehicleType.HEV) return 0.0
            val fallbackHomePrice = if (homeEnergyPrice > 0.0) homeEnergyPrice else if (energyPriceKwh > 0.0) energyPriceKwh else 1.30
            val fallbackPublicPrice = if (publicEnergyPrice > 0.0) publicEnergyPrice else if (energyPriceKwh > 0.0) energyPriceKwh else 2.10

            val locations = effectiveRechargeLocations
            val finals = effectiveRechargeBatteryPercents
            val inits = effectiveRechargeInitialBatteryPercents
            val prices = effectiveRechargePrices

            val activeIndices = locations.indices.filter { locations[it] != ChargingLocation.NONE }
            if (activeIndices.isEmpty()) {
                return if (vehicleType == VehicleType.BEV) totalEnergyKwh * fallbackHomePrice else 0.0
            }

            val reservePercent = if (vehicleType == VehicleType.PHEV) 25.0 else 0.0
            var remainingKwh = totalEnergyKwh
            var cost = 0.0
            val batteryCap = if (batteryCapacityKwh > 0.0) batteryCapacityKwh else 18.3

            for (idx in activeIndices.indices) {
                if (remainingKwh <= 0.0) break
                val i = activeIndices[idx]
                val loc = locations[i]
                val pFinal = finals.getOrElse(i) { 100.0 }
                val usableP = if (idx == activeIndices.size - 1) {
                    (pFinal - reservePercent).coerceIn(0.0, 100.0)
                } else {
                    val nextI = activeIndices[idx + 1]
                    val nextInit = inits.getOrElse(nextI) { 0.0 }
                    val deltaToNext = (pFinal - nextInit).coerceAtLeast(0.0)
                    val leg0 = if (idx == 0) {
                        val pInit = inits.getOrElse(i) { 25.0 }
                        if (pFinal < 100.0) {
                            (100.0 - pFinal).coerceAtLeast(0.0)
                        } else if (pInit > reservePercent) {
                            (100.0 - pInit).coerceAtLeast(0.0)
                        } else {
                            0.0
                        }
                    } else {
                        0.0
                    }
                    leg0 + deltaToNext
                }
                val kwhPerRecharge = if (batteryCap > 0) batteryCap * (usableP / 100.0) else 0.0
                val kwh = if (kwhPerRecharge > 0) remainingKwh.coerceAtMost(kwhPerRecharge) else remainingKwh
                val price = prices.getOrElse(i) {
                    when (loc) {
                        ChargingLocation.HOME -> fallbackHomePrice
                        ChargingLocation.STATION -> fallbackPublicPrice
                        ChargingLocation.NONE -> 0.0
                    }
                }
                cost += kwh * price
                remainingKwh -= kwh
            }
            if (remainingKwh > 0.0) {
                cost += remainingKwh * fallbackHomePrice
            }
            return cost
        }

    val gasolineCost: Double
        get() {
            if (vehicleType == VehicleType.BEV) return 0.0
            val effPrice = if (gasPriceLiter > 0.0) gasPriceLiter else 6.50
            return totalGasolineLiters * effPrice
        }

    val totalCost: Double
        get() = electricCost + gasolineCost

    val costPerKm: Double
        get() = if (totalKm > 0) totalCost / totalKm else 0.0

    val costPer100Km: Double
        get() = costPerKm * 100.0

    // Equivalent km/L based on total money spent and current gas price
    val equivalentKmL: Double
        get() {
            val effPrice = if (gasPriceLiter > 0.0) gasPriceLiter else 6.50
            return if (totalCost > 0 && effPrice > 0) {
                totalKm / (totalCost / effPrice)
            } else {
                0.0
            }
        }

    // Benchmark comparison: what if the whole trip was 100% gasoline (combustion car or hybrid mode)?
    val cost100PercentGas: Double
        get() {
            val effKmL = when (vehicleType) {
                VehicleType.BEV -> 12.0
                VehicleType.HEV -> 10.0
                VehicleType.PHEV -> if (gasolineConsumptionKmL > 0) gasolineConsumptionKmL else 12.0
            }
            val effPrice = if (gasPriceLiter > 0.0) gasPriceLiter else 6.50
            return (totalKm / effKmL) * effPrice
        }

    val savingsVsGas: Double
        get() = (cost100PercentGas - totalCost).coerceAtLeast(0.0)

    val savingsPercent: Double
        get() = if (cost100PercentGas > 0) {
            ((savingsVsGas / cost100PercentGas) * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

    // CO2 avoided: ~2.31 kg of CO2 per liter of gasoline saved
    val co2SavedKg: Double
        get() {
            val gasSavedLiters = ((cost100PercentGas - totalCost) / gasPriceLiter.coerceAtLeast(1.0)).coerceAtLeast(0.0)
            return gasSavedLiters * 2.31
        }

    fun formattedDate(language: AppLanguage): String {
        val locale = if (language == AppLanguage.EN_US) Locale.US else Locale("pt", "BR")
        val pattern = if (language == AppLanguage.EN_US) "MMM d, yyyy • HH:mm" else "dd/MM/yyyy • HH:mm"
        val sdf = SimpleDateFormat(pattern, locale)
        return sdf.format(Date(timestamp))
    }

    fun matchesVehicle(vehicle: Vehicle): Boolean {
        if (vehicleId.isNotBlank() && vehicleId == vehicle.id) return true
        if (vehicleName.isNotBlank()) {
            if (vehicleName.equals(vehicle.name, ignoreCase = true)) return true
            val cleanEntry = vehicleName.lowercase().replace("-", " ").trim()
            val cleanVeh = vehicle.name.lowercase().replace("-", " ").trim()
            if (cleanEntry.isNotEmpty() && cleanVeh.isNotEmpty()) {
                if (cleanEntry.contains(cleanVeh) || cleanVeh.contains(cleanEntry)) return true
            }
        }
        return false
    }

    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("timestamp", timestamp)
        obj.put("title", title)
        obj.put("vehicleId", vehicleId)
        obj.put("vehicleName", vehicleName)
        obj.put("vehicleType", vehicleType.name)
        obj.put("totalKm", totalKm)
        obj.put("evKm", evKm)
        obj.put("hevKm", hevKm)
        obj.put("electricConsumptionKwh100km", electricConsumptionKwh100km)
        obj.put("gasolineConsumptionKmL", gasolineConsumptionKmL)
        obj.put("energyPriceKwh", energyPriceKwh)
        obj.put("gasPriceLiter", gasPriceLiter)
        obj.put("fuelLiters", fuelLiters)
        obj.put("averageHevKmL", averageHevKmL)
        obj.put("averageGlobalKmL", averageGlobalKmL)
        obj.put("totalStartKm", totalStartKm)
        obj.put("totalEndKm", totalEndKm)
        obj.put("hevStartKm", hevStartKm)
        obj.put("hevEndKm", hevEndKm)
        obj.put("chargingLocation", chargingLocation.name)
        obj.put("batteryCapacityKwh", batteryCapacityKwh)
        obj.put("batteryMaxPercent", usableBatteryPercent)
        obj.put("batteryStartPercent", batteryStartPercent)
        obj.put("rechargeCount", rechargeCount)
        val locArr = JSONArray()
        effectiveRechargeLocations.forEach { locArr.put(it.name) }
        obj.put("rechargeLocations", locArr)
        val percentArr = JSONArray()
        effectiveRechargeBatteryPercents.forEach { percentArr.put(it) }
        obj.put("rechargeBatteryPercents", percentArr)
        val initPercentArr = JSONArray()
        effectiveRechargeInitialBatteryPercents.forEach { initPercentArr.put(it) }
        obj.put("rechargeInitialBatteryPercents", initPercentArr)
        val priceArr = JSONArray()
        effectiveRechargePrices.forEach { priceArr.put(it) }
        obj.put("rechargePrices", priceArr)
        obj.put("homeEnergyPrice", homeEnergyPrice)
        obj.put("publicEnergyPrice", publicEnergyPrice)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): OdometerEntry {
            val locationStr = obj.optString("chargingLocation", "HOME")
            val parsedLocation = try {
                ChargingLocation.valueOf(locationStr)
            } catch (e: Exception) {
                ChargingLocation.HOME
            }

            val parsedStartPercent = if (obj.has("batteryStartPercent")) {
                obj.optDouble("batteryStartPercent", 100.0)
            } else {
                val oldMax = obj.optDouble("batteryMaxPercent", 75.0)
                if (oldMax <= 0.0) 25.0 else (oldMax + 25.0).coerceAtMost(100.0)
            }
            val parsedMaxPercent = (parsedStartPercent - 25.0).coerceAtLeast(0.0)
            val parsedRechargeCount = obj.optInt("rechargeCount", 1).coerceAtLeast(1)

            val parsedLocations = mutableListOf<ChargingLocation>()
            if (obj.has("rechargeLocations")) {
                val arr = obj.optJSONArray("rechargeLocations")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val loc = try {
                            ChargingLocation.valueOf(arr.getString(i))
                        } catch (e: Exception) {
                            ChargingLocation.HOME
                        }
                        parsedLocations.add(loc)
                    }
                }
            }
            val finalLocations = if (parsedLocations.isEmpty()) {
                List(parsedRechargeCount) { parsedLocation }
            } else {
                parsedLocations
            }

            val parsedPercents = mutableListOf<Double>()
            if (obj.has("rechargeBatteryPercents")) {
                val arr = obj.optJSONArray("rechargeBatteryPercents")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        parsedPercents.add(arr.optDouble(i, 100.0))
                    }
                }
            }
            val finalPercents = if (parsedPercents.isEmpty()) {
                List(parsedRechargeCount) { 100.0 }
            } else {
                parsedPercents
            }

            val parsedInitPercents = mutableListOf<Double>()
            if (obj.has("rechargeInitialBatteryPercents")) {
                val arr = obj.optJSONArray("rechargeInitialBatteryPercents")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        parsedInitPercents.add(arr.optDouble(i, 25.0))
                    }
                }
            }
            val finalInitPercents = if (parsedInitPercents.isEmpty()) {
                List(parsedRechargeCount) { 25.0 }
            } else {
                parsedInitPercents
            }

            val parsedPrices = mutableListOf<Double>()
            if (obj.has("rechargePrices")) {
                val arr = obj.optJSONArray("rechargePrices")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        parsedPrices.add(arr.optDouble(i, 0.0))
                    }
                }
            }

            val parsedHomePrice = obj.optDouble("homeEnergyPrice", 0.0)
            val parsedPublicPrice = obj.optDouble("publicEnergyPrice", 0.0)
            val parsedVehicleType = try {
                VehicleType.valueOf(obj.optString("vehicleType", "PHEV"))
            } catch (e: Exception) {
                VehicleType.PHEV
            }

            return OdometerEntry(
                id = obj.optString("id", UUID.randomUUID().toString()),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                title = obj.optString("title", "Viagem"),
                vehicleId = obj.optString("vehicleId", ""),
                vehicleName = obj.optString("vehicleName", ""),
                vehicleType = parsedVehicleType,
                totalKm = obj.optDouble("totalKm", 0.0),
                evKm = obj.optDouble("evKm", 0.0),
                hevKm = obj.optDouble("hevKm", 0.0),
                electricConsumptionKwh100km = obj.optDouble("electricConsumptionKwh100km", 18.0),
                gasolineConsumptionKmL = obj.optDouble("gasolineConsumptionKmL", 16.0),
                energyPriceKwh = obj.optDouble("energyPriceKwh", 1.30),
                gasPriceLiter = obj.optDouble("gasPriceLiter", 6.50),
                fuelLiters = obj.optDouble("fuelLiters", 0.0),
                averageHevKmL = obj.optDouble("averageHevKmL", 0.0),
                averageGlobalKmL = obj.optDouble("averageGlobalKmL", 0.0),
                totalStartKm = obj.optDouble("totalStartKm", 0.0),
                totalEndKm = obj.optDouble("totalEndKm", 0.0),
                hevStartKm = obj.optDouble("hevStartKm", 0.0),
                hevEndKm = obj.optDouble("hevEndKm", 0.0),
                chargingLocation = parsedLocation,
                batteryCapacityKwh = obj.optDouble("batteryCapacityKwh", 18.3),
                batteryMaxPercent = parsedMaxPercent,
                batteryStartPercent = parsedStartPercent,
                rechargeCount = parsedRechargeCount,
                rechargeLocations = finalLocations,
                rechargeBatteryPercents = finalPercents,
                rechargeInitialBatteryPercents = finalInitPercents,
                rechargePrices = parsedPrices,
                homeEnergyPrice = parsedHomePrice,
                publicEnergyPrice = parsedPublicPrice
            )
        }

        fun toJsonArray(list: List<OdometerEntry>): String {
            val array = JSONArray()
            for (entry in list) {
                array.put(entry.toJsonObject())
            }
            return array.toString()
        }

        fun fromJsonArray(jsonStr: String?): List<OdometerEntry> {
            if (jsonStr.isNullOrBlank()) return emptyList()
            return try {
                val array = JSONArray(jsonStr)
                val result = mutableListOf<OdometerEntry>()
                for (i in 0 until array.length()) {
                    result.add(fromJsonObject(array.getJSONObject(i)))
                }
                result
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
