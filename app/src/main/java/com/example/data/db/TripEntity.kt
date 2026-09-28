package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.ChargingLocation
import com.example.data.OdometerEntry
import com.example.data.VehicleType
import org.json.JSONArray

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val title: String,
    val vehicleId: String,
    val vehicleName: String,
    val vehicleType: String,
    val totalKm: Double,
    val evKm: Double,
    val hevKm: Double,
    val electricConsumptionKwh100km: Double,
    val gasolineConsumptionKmL: Double,
    val energyPriceKwh: Double,
    val gasPriceLiter: Double,
    val fuelLiters: Double,
    val averageHevKmL: Double,
    val averageGlobalKmL: Double,
    val totalStartKm: Double,
    val totalEndKm: Double,
    val hevStartKm: Double,
    val hevEndKm: Double,
    val chargingLocation: String,
    val batteryCapacityKwh: Double,
    val batteryMaxPercent: Double,
    val batteryStartPercent: Double,
    val rechargeCount: Int,
    val rechargeLocationsJson: String,
    val rechargeBatteryPercentsJson: String,
    val rechargeInitialBatteryPercentsJson: String,
    val rechargePricesJson: String = "[]",
    val homeEnergyPrice: Double,
    val publicEnergyPrice: Double
)

fun TripEntity.toOdometerEntry(): OdometerEntry {
    val parsedType = try {
        VehicleType.valueOf(vehicleType)
    } catch (e: Exception) {
        VehicleType.PHEV
    }
    val parsedLoc = try {
        ChargingLocation.valueOf(chargingLocation)
    } catch (e: Exception) {
        ChargingLocation.HOME
    }

    val parsedRechargeLocations = try {
        if (rechargeLocationsJson.isNotBlank()) {
            val arr = JSONArray(rechargeLocationsJson)
            (0 until arr.length()).map { ChargingLocation.valueOf(arr.getString(it)) }
        } else emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val parsedBatteryPercents = try {
        if (rechargeBatteryPercentsJson.isNotBlank()) {
            val arr = JSONArray(rechargeBatteryPercentsJson)
            (0 until arr.length()).map { arr.getDouble(it) }
        } else emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val parsedInitialPercents = try {
        if (rechargeInitialBatteryPercentsJson.isNotBlank()) {
            val arr = JSONArray(rechargeInitialBatteryPercentsJson)
            (0 until arr.length()).map { arr.getDouble(it) }
        } else emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val parsedPrices = try {
        if (rechargePricesJson.isNotBlank()) {
            val arr = JSONArray(rechargePricesJson)
            (0 until arr.length()).map { arr.getDouble(it) }
        } else emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    return OdometerEntry(
        id = id,
        timestamp = timestamp,
        title = title,
        vehicleId = vehicleId,
        vehicleName = vehicleName,
        vehicleType = parsedType,
        totalKm = totalKm,
        evKm = evKm,
        hevKm = hevKm,
        electricConsumptionKwh100km = electricConsumptionKwh100km,
        gasolineConsumptionKmL = gasolineConsumptionKmL,
        energyPriceKwh = energyPriceKwh,
        gasPriceLiter = gasPriceLiter,
        fuelLiters = fuelLiters,
        averageHevKmL = averageHevKmL,
        averageGlobalKmL = averageGlobalKmL,
        totalStartKm = totalStartKm,
        totalEndKm = totalEndKm,
        hevStartKm = hevStartKm,
        hevEndKm = hevEndKm,
        chargingLocation = parsedLoc,
        batteryCapacityKwh = batteryCapacityKwh,
        batteryMaxPercent = batteryMaxPercent,
        batteryStartPercent = batteryStartPercent,
        rechargeCount = rechargeCount,
        rechargeLocations = parsedRechargeLocations,
        rechargeBatteryPercents = parsedBatteryPercents,
        rechargeInitialBatteryPercents = parsedInitialPercents,
        rechargePrices = parsedPrices,
        homeEnergyPrice = homeEnergyPrice,
        publicEnergyPrice = publicEnergyPrice
    )
}

fun OdometerEntry.toTripEntity(): TripEntity {
    val locsJson = JSONArray().apply {
        rechargeLocations.forEach { put(it.name) }
    }.toString()

    val batteryPercentsJson = JSONArray().apply {
        rechargeBatteryPercents.forEach { put(it) }
    }.toString()

    val initialPercentsJson = JSONArray().apply {
        rechargeInitialBatteryPercents.forEach { put(it) }
    }.toString()

    val pricesJson = JSONArray().apply {
        rechargePrices.forEach { put(it) }
    }.toString()

    return TripEntity(
        id = id,
        timestamp = timestamp,
        title = title,
        vehicleId = vehicleId,
        vehicleName = vehicleName,
        vehicleType = vehicleType.name,
        totalKm = totalKm,
        evKm = evKm,
        hevKm = hevKm,
        electricConsumptionKwh100km = electricConsumptionKwh100km,
        gasolineConsumptionKmL = gasolineConsumptionKmL,
        energyPriceKwh = energyPriceKwh,
        gasPriceLiter = gasPriceLiter,
        fuelLiters = fuelLiters,
        averageHevKmL = averageHevKmL,
        averageGlobalKmL = averageGlobalKmL,
        totalStartKm = totalStartKm,
        totalEndKm = totalEndKm,
        hevStartKm = hevStartKm,
        hevEndKm = hevEndKm,
        chargingLocation = chargingLocation.name,
        batteryCapacityKwh = batteryCapacityKwh,
        batteryMaxPercent = batteryMaxPercent,
        batteryStartPercent = batteryStartPercent,
        rechargeCount = rechargeCount,
        rechargeLocationsJson = locsJson,
        rechargeBatteryPercentsJson = batteryPercentsJson,
        rechargeInitialBatteryPercentsJson = initialPercentsJson,
        rechargePricesJson = pricesJson,
        homeEnergyPrice = homeEnergyPrice,
        publicEnergyPrice = publicEnergyPrice
    )
}
