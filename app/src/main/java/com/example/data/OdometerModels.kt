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
    STATION;

    fun label(language: AppLanguage): String {
        return when (this) {
            HOME -> if (language == AppLanguage.EN_US) "Home" else "Em Casa"
            STATION -> if (language == AppLanguage.EN_US) "Charging Station" else "Posto de Recarga"
        }
    }
}

data class OdometerEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val vehicleName: String,
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
    val batteryCapacityKwh: Double = 18.3
) {
    // Percentages
    val evPercent: Double
        get() = if (totalKm > 0) ((evKm / totalKm) * 100.0).coerceIn(0.0, 100.0) else 0.0

    val hevPercent: Double
        get() = if (totalKm > 0) ((hevKm / totalKm) * 100.0).coerceIn(0.0, 100.0) else 0.0

    // Effective real consumption from fuel liters if available, else theoretical
    val realHevKmL: Double
        get() = if (averageHevKmL > 0) averageHevKmL else if (fuelLiters > 0 && hevKm > 0) hevKm / fuelLiters else gasolineConsumptionKmL

    val realHevL100km: Double
        get() = if (realHevKmL > 0) 100.0 / realHevKmL else 0.0

    val realGlobalKmL: Double
        get() = if (averageGlobalKmL > 0) averageGlobalKmL else if (fuelLiters > 0 && totalKm > 0) totalKm / fuelLiters else 0.0

    // Consumptions and costs: gasto elétrico limitado a 75% da capacidade de bateria do veículo
    val totalEnergyKwh: Double
        get() {
            val rawKwh = (evKm * electricConsumptionKwh100km / 100.0)
            val maxKwh = if (batteryCapacityKwh > 0) batteryCapacityKwh * 0.75 else 0.0
            return if (maxKwh > 0) rawKwh.coerceAtMost(maxKwh) else rawKwh
        }

    val totalGasolineLiters: Double
        get() = if (fuelLiters > 0) fuelLiters else if (gasolineConsumptionKmL > 0) hevKm / gasolineConsumptionKmL else 0.0

    val electricCost: Double
        get() = totalEnergyKwh * energyPriceKwh

    val gasolineCost: Double
        get() = totalGasolineLiters * gasPriceLiter

    val totalCost: Double
        get() = electricCost + gasolineCost

    val costPerKm: Double
        get() = if (totalKm > 0) totalCost / totalKm else 0.0

    val costPer100Km: Double
        get() = costPerKm * 100.0

    // Equivalent km/L based on total money spent and current gas price
    val equivalentKmL: Double
        get() = if (totalCost > 0 && gasPriceLiter > 0) {
            totalKm / (totalCost / gasPriceLiter)
        } else {
            0.0
        }

    // Benchmark comparison: what if the whole trip was 100% gasoline (combustion car or hybrid mode)?
    val cost100PercentGas: Double
        get() {
            val effKmL = if (gasolineConsumptionKmL > 0) gasolineConsumptionKmL else 12.0
            return (totalKm / effKmL) * gasPriceLiter
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

    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("timestamp", timestamp)
        obj.put("title", title)
        obj.put("vehicleName", vehicleName)
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

            return OdometerEntry(
                id = obj.optString("id", UUID.randomUUID().toString()),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                title = obj.optString("title", "Viagem"),
                vehicleName = obj.optString("vehicleName", ""),
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
                batteryCapacityKwh = obj.optDouble("batteryCapacityKwh", 18.3)
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
