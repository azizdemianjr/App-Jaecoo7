package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("phev_calc_user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "AppPreferences"
        private const val KEY_HOME_ENERGY_PRICE = "home_energy_price"
        private const val KEY_HOME_ENERGY_PRICE_BITS = "home_energy_price_bits"
        private const val KEY_PUBLIC_ENERGY_PRICE = "public_energy_price"
        private const val KEY_PUBLIC_ENERGY_PRICE_BITS = "public_energy_price_bits"
        private const val KEY_GASOLINE_PRICE = "gasoline_price"
        private const val KEY_GASOLINE_PRICE_BITS = "gasoline_price_bits"
        private const val KEY_CUSTOM_COMPARISON_GAS_KM_L = "custom_comparison_gas_km_l"
        private const val KEY_CUSTOM_COMPARISON_GAS_KM_L_BITS = "custom_comparison_gas_km_l_bits"
        private const val KEY_MONTHLY_KM = "monthly_km"
        private const val KEY_COST_SOC_INITIAL = "cost_soc_initial"
        private const val KEY_COST_SOC_FINAL = "cost_soc_final"
        private const val KEY_THERMAL_LOSS_PERCENT = "thermal_loss_percent"
        private const val KEY_THERMAL_LOSS_PERCENT_BITS = "thermal_loss_percent_bits"
        private const val KEY_TIME_VOLTAGE = "time_voltage"
        private const val KEY_TIME_CURRENT = "time_current"
        private const val KEY_TIME_SOC_INITIAL = "time_soc_initial"
        private const val KEY_TIME_SOC_FINAL = "time_soc_final"
        private const val KEY_SELECTED_VEHICLE_ID = "selected_vehicle_id"
        private const val KEY_VEHICLES_JSON = "vehicles_json_list"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
        private const val KEY_ODOMETER_ENTRIES_JSON = "odometer_entries_json"
        private const val KEY_ODOMETER_TOTAL_KM = "odometer_total_km"
        private const val KEY_ODOMETER_EV_KM = "odometer_ev_km"
        private const val KEY_ODOMETER_HEV_KM = "odometer_hev_km"
        private const val KEY_ODOMETER_USE_HOME_TARIFF = "odometer_use_home_tariff"
        private const val KEY_ODOMETER_TOTAL_START_KM = "odometer_total_start_km"
        private const val KEY_ODOMETER_TOTAL_END_KM = "odometer_total_end_km"
        private const val KEY_ODOMETER_HEV_START_KM = "odometer_hev_start_km"
        private const val KEY_ODOMETER_HEV_END_KM = "odometer_hev_end_km"
        private const val KEY_ODOMETER_FUEL_LITERS = "odometer_fuel_liters"
        private const val KEY_ODOMETER_TRIP_NOTE = "odometer_trip_note"
    }

    fun saveLastSyncTimestamp(timestamp: Long) {
        val success = prefs.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, timestamp).commit()
        if (!success) Log.e(TAG, "Failed to commit last sync timestamp")
    }

    fun getLastSyncTimestamp(): Long {
        return prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
    }

    fun saveLanguage(languageCode: String) {
        val success = prefs.edit().putString(KEY_LANGUAGE, languageCode).commit()
        if (!success) Log.e(TAG, "Failed to commit language: $languageCode")
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "pt-BR") ?: "pt-BR"
    }

    fun saveHomeEnergyPrice(price: Double) {
        val success = prefs.edit()
            .putLong(KEY_HOME_ENERGY_PRICE_BITS, java.lang.Double.doubleToRawLongBits(price))
            .putFloat(KEY_HOME_ENERGY_PRICE, price.toFloat())
            .commit()
        if (!success) Log.e(TAG, "Failed to commit home energy price: $price")
    }

    fun getHomeEnergyPrice(): Double {
        return try {
            if (prefs.contains(KEY_HOME_ENERGY_PRICE_BITS)) {
                java.lang.Double.longBitsToDouble(prefs.getLong(KEY_HOME_ENERGY_PRICE_BITS, 0L))
            } else if (prefs.contains(KEY_HOME_ENERGY_PRICE)) {
                prefs.getFloat(KEY_HOME_ENERGY_PRICE, 1.30f).toDouble()
            } else {
                1.30
            }
        } catch (e: Exception) {
            1.30
        }
    }

    fun savePublicEnergyPrice(price: Double) {
        val success = prefs.edit()
            .putLong(KEY_PUBLIC_ENERGY_PRICE_BITS, java.lang.Double.doubleToRawLongBits(price))
            .putFloat(KEY_PUBLIC_ENERGY_PRICE, price.toFloat())
            .commit()
        if (!success) Log.e(TAG, "Failed to commit public energy price: $price")
    }

    fun getPublicEnergyPrice(): Double {
        return try {
            if (prefs.contains(KEY_PUBLIC_ENERGY_PRICE_BITS)) {
                java.lang.Double.longBitsToDouble(prefs.getLong(KEY_PUBLIC_ENERGY_PRICE_BITS, 0L))
            } else if (prefs.contains(KEY_PUBLIC_ENERGY_PRICE)) {
                prefs.getFloat(KEY_PUBLIC_ENERGY_PRICE, 1.79f).toDouble()
            } else {
                1.79
            }
        } catch (e: Exception) {
            1.79
        }
    }

    fun saveGasolinePrice(price: Double) {
        val success = prefs.edit()
            .putLong(KEY_GASOLINE_PRICE_BITS, java.lang.Double.doubleToRawLongBits(price))
            .putFloat(KEY_GASOLINE_PRICE, price.toFloat())
            .commit()
        if (!success) Log.e(TAG, "Failed to commit gasoline price: $price")
    }

    fun getGasolinePrice(): Double {
        return try {
            if (prefs.contains(KEY_GASOLINE_PRICE_BITS)) {
                java.lang.Double.longBitsToDouble(prefs.getLong(KEY_GASOLINE_PRICE_BITS, 0L))
            } else if (prefs.contains(KEY_GASOLINE_PRICE)) {
                prefs.getFloat(KEY_GASOLINE_PRICE, 6.50f).toDouble()
            } else {
                6.50
            }
        } catch (e: Exception) {
            6.50
        }
    }

    fun saveCustomComparisonGasKmL(kmL: Double) {
        val success = prefs.edit()
            .putLong(KEY_CUSTOM_COMPARISON_GAS_KM_L_BITS, java.lang.Double.doubleToRawLongBits(kmL))
            .putFloat(KEY_CUSTOM_COMPARISON_GAS_KM_L, kmL.toFloat())
            .commit()
        if (!success) Log.e(TAG, "Failed to commit comparison gas km/L: $kmL")
    }

    fun getCustomComparisonGasKmL(): Double {
        return try {
            if (prefs.contains(KEY_CUSTOM_COMPARISON_GAS_KM_L_BITS)) {
                java.lang.Double.longBitsToDouble(prefs.getLong(KEY_CUSTOM_COMPARISON_GAS_KM_L_BITS, 0L))
            } else if (prefs.contains(KEY_CUSTOM_COMPARISON_GAS_KM_L)) {
                prefs.getFloat(KEY_CUSTOM_COMPARISON_GAS_KM_L, 12.0f).toDouble()
            } else {
                12.0
            }
        } catch (e: Exception) {
            12.0
        }
    }

    fun saveMonthlyKm(km: Float) {
        val success = prefs.edit().putFloat(KEY_MONTHLY_KM, km).commit()
        if (!success) Log.e(TAG, "Failed to commit monthly km: $km")
    }

    fun getMonthlyKm(): Float {
        return prefs.getFloat(KEY_MONTHLY_KM, 1000f)
    }

    fun saveCostSoc(initial: Int, final: Int) {
        val success = prefs.edit()
            .putInt(KEY_COST_SOC_INITIAL, initial)
            .putInt(KEY_COST_SOC_FINAL, final)
            .commit()
        if (!success) Log.e(TAG, "Failed to commit cost SoC")
    }

    fun getCostSocInitial(): Int = prefs.getInt(KEY_COST_SOC_INITIAL, 20)
    fun getCostSocFinal(): Int = prefs.getInt(KEY_COST_SOC_FINAL, 100)

    fun saveThermalLossPercent(percent: Double) {
        val success = prefs.edit()
            .putLong(KEY_THERMAL_LOSS_PERCENT_BITS, java.lang.Double.doubleToRawLongBits(percent))
            .putFloat(KEY_THERMAL_LOSS_PERCENT, percent.toFloat())
            .commit()
        if (!success) Log.e(TAG, "Failed to commit thermal loss: $percent")
    }

    fun getThermalLossPercent(): Double {
        return try {
            if (prefs.contains(KEY_THERMAL_LOSS_PERCENT_BITS)) {
                java.lang.Double.longBitsToDouble(prefs.getLong(KEY_THERMAL_LOSS_PERCENT_BITS, 0L))
            } else if (prefs.contains(KEY_THERMAL_LOSS_PERCENT)) {
                prefs.getFloat(KEY_THERMAL_LOSS_PERCENT, 10.0f).toDouble()
            } else {
                10.0
            }
        } catch (e: Exception) {
            10.0
        }
    }

    fun saveTimeParameters(voltage: Int, current: Int, initial: Int, final: Int) {
        val success = prefs.edit()
            .putInt(KEY_TIME_VOLTAGE, voltage)
            .putInt(KEY_TIME_CURRENT, current)
            .putInt(KEY_TIME_SOC_INITIAL, initial)
            .putInt(KEY_TIME_SOC_FINAL, final)
            .commit()
        if (!success) Log.e(TAG, "Failed to commit time parameters")
    }

    fun getTimeVoltage(): Int = prefs.getInt(KEY_TIME_VOLTAGE, 220)
    fun getTimeCurrent(): Int = prefs.getInt(KEY_TIME_CURRENT, 10)
    fun getTimeSocInitial(): Int = prefs.getInt(KEY_TIME_SOC_INITIAL, 20)
    fun getTimeSocFinal(): Int = prefs.getInt(KEY_TIME_SOC_FINAL, 100)

    fun saveSelectedVehicleId(id: String) {
        val success = prefs.edit().putString(KEY_SELECTED_VEHICLE_ID, id).commit()
        if (!success) Log.e(TAG, "Failed to commit selected vehicle id: $id")
    }

    fun getSelectedVehicleId(): String? = prefs.getString(KEY_SELECTED_VEHICLE_ID, null)

    fun saveVehiclesList(list: List<Vehicle>) {
        val array = JSONArray()
        for (v in list) {
            val obj = JSONObject()
            obj.put("id", v.id)
            obj.put("name", v.name)
            obj.put("brand", v.brand)
            obj.put("type", v.type.name)
            obj.put("tag", v.tag)
            obj.put("batteryCapacityKwh", v.batteryCapacityKwh)
            obj.put("electricConsumptionKwh100km", v.electricConsumptionKwh100km)
            obj.put("gasolineConsumptionKmL", v.gasolineConsumptionKmL)
            obj.put("maxAcChargeKw", v.maxAcChargeKw)
            obj.put("imageUrl", v.imageUrl)
            obj.put("isCustom", v.isCustom)
            array.put(obj)
        }
        val success = prefs.edit().putString(KEY_VEHICLES_JSON, array.toString()).commit()
        if (!success) Log.e(TAG, "Failed to commit vehicles list (${list.size} items)")
    }

    fun getVehiclesList(): List<Vehicle>? {
        val jsonStr = prefs.getString(KEY_VEHICLES_JSON, null) ?: return null
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<Vehicle>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeName = obj.optString("type", VehicleType.PHEV.name)
                val type = try {
                    VehicleType.valueOf(typeName)
                } catch (e: Exception) {
                    VehicleType.PHEV
                }
                list.add(
                    Vehicle(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "Veículo"),
                        brand = obj.optString("brand", ""),
                        type = type,
                        tag = obj.optString("tag", type.label),
                        batteryCapacityKwh = obj.optDouble("batteryCapacityKwh", 18.0),
                        electricConsumptionKwh100km = obj.optDouble("electricConsumptionKwh100km", 20.0),
                        gasolineConsumptionKmL = obj.optDouble("gasolineConsumptionKmL", 16.0),
                        maxAcChargeKw = obj.optDouble("maxAcChargeKw", 7.0),
                        imageUrl = obj.optString("imageUrl", ""),
                        isCustom = obj.optBoolean("isCustom", false)
                    )
                )
            }
            if (list.isNotEmpty()) list else null
        } catch (e: Exception) {
            null
        }
    }

    fun saveOdometerEntries(list: List<OdometerEntry>) {
        val jsonStr = OdometerEntry.toJsonArray(list)
        val success = prefs.edit().putString(KEY_ODOMETER_ENTRIES_JSON, jsonStr).commit()
        if (!success) Log.e(TAG, "Failed to commit odometer entries")
    }

    fun getOdometerEntries(): List<OdometerEntry> {
        val jsonStr = prefs.getString(KEY_ODOMETER_ENTRIES_JSON, null)
        return OdometerEntry.fromJsonArray(jsonStr)
    }

    fun saveOdometerDraft(totalKm: Double, hevKm: Double, useHomeTariff: Boolean) {
        prefs.edit()
            .putFloat(KEY_ODOMETER_TOTAL_KM, totalKm.toFloat())
            .putFloat(KEY_ODOMETER_HEV_KM, hevKm.toFloat())
            .putBoolean(KEY_ODOMETER_USE_HOME_TARIFF, useHomeTariff)
            .commit()
    }

    fun getOdometerDraftTotalKm(): Double {
        return if (prefs.contains(KEY_ODOMETER_TOTAL_KM)) {
            prefs.getFloat(KEY_ODOMETER_TOTAL_KM, 100.0f).toDouble()
        } else {
            val ev = prefs.getFloat(KEY_ODOMETER_EV_KM, 60.0f).toDouble()
            val hev = prefs.getFloat(KEY_ODOMETER_HEV_KM, 40.0f).toDouble()
            ev + hev
        }
    }

    fun getOdometerDraftHevKm(): Double = prefs.getFloat(KEY_ODOMETER_HEV_KM, 40.0f).toDouble()
    fun getOdometerDraftUseHomeTariff(): Boolean = prefs.getBoolean(KEY_ODOMETER_USE_HOME_TARIFF, true)

    fun saveOdometerDraft(
        totalStartKm: Double,
        totalEndKm: Double,
        hevStartKm: Double,
        hevEndKm: Double,
        fuelLiters: Double = 0.0,
        tripNote: String = ""
    ) {
        prefs.edit()
            .putFloat(KEY_ODOMETER_TOTAL_START_KM, totalStartKm.toFloat())
            .putFloat(KEY_ODOMETER_TOTAL_END_KM, totalEndKm.toFloat())
            .putFloat(KEY_ODOMETER_HEV_START_KM, hevStartKm.toFloat())
            .putFloat(KEY_ODOMETER_HEV_END_KM, hevEndKm.toFloat())
            .putFloat(KEY_ODOMETER_FUEL_LITERS, fuelLiters.toFloat())
            .putString(KEY_ODOMETER_TRIP_NOTE, tripNote)
            .commit()
    }

    fun getOdometerDraftTotalStartKm(): Double = prefs.getFloat(KEY_ODOMETER_TOTAL_START_KM, 0.0f).toDouble()

    fun getOdometerDraftTotalEndKm(): Double = prefs.getFloat(KEY_ODOMETER_TOTAL_END_KM, 1000.0f).toDouble()

    fun getOdometerDraftHevStartKm(): Double = prefs.getFloat(KEY_ODOMETER_HEV_START_KM, 0.0f).toDouble()

    fun getOdometerDraftHevEndKm(): Double = prefs.getFloat(KEY_ODOMETER_HEV_END_KM, 100.0f).toDouble()

    fun getOdometerDraftFuelLiters(): Double = prefs.getFloat(KEY_ODOMETER_FUEL_LITERS, 6.25f).toDouble()

    fun getOdometerDraftTripNote(): String = prefs.getString(KEY_ODOMETER_TRIP_NOTE, "") ?: ""

    fun clearAll() {
        prefs.edit().clear().commit()
    }
}
