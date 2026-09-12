package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VehicleSyncResult(
    val newModelsCount: Int,
    val updatedModelsCount: Int,
    val totalCount: Int,
    val newlyDiscoveredVehicles: List<Vehicle>,
    val updatedVehicles: List<Vehicle>,
    val syncTimestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
            return sdf.format(Date(syncTimestamp))
        }
}

object VehicleSyncEngine {

    /**
     * Performs a smart merge of local vehicles with the latest official database.
     * Preserves custom vehicles created by the user and updates factory specs for standard models.
     */
    fun smartSync(
        currentList: List<Vehicle>,
        latestCatalog: List<Vehicle> = VehicleCatalog.defaultVehicles
    ): Pair<List<Vehicle>, VehicleSyncResult> {
        val catalogMap = latestCatalog.associateBy { it.id }
        val currentIds = currentList.map { it.id }.toSet()

        val newlyDiscovered = latestCatalog.filter { it.id !in currentIds }
        val updatedVehicles = mutableListOf<Vehicle>()

        val mergedExisting = currentList.map { v ->
            if (v.isCustom) {
                // User-created custom vehicles are always 100% preserved
                v
            } else {
                val official = catalogMap[v.id]
                if (official != null) {
                    // Check if specifications differ from official catalog
                    val hasChanged = official.batteryCapacityKwh != v.batteryCapacityKwh ||
                            official.electricConsumptionKwh100km != v.electricConsumptionKwh100km ||
                            official.gasolineConsumptionKmL != v.gasolineConsumptionKmL ||
                            official.maxAcChargeKw != v.maxAcChargeKw ||
                            official.name != v.name ||
                            official.imageUrl != v.imageUrl

                    if (hasChanged) {
                        updatedVehicles.add(official)
                    }
                    official
                } else {
                    v
                }
            }
        }

        // Complete merged list: updated existing + newly discovered models from catalog
        val finalList = mergedExisting + newlyDiscovered

        val result = VehicleSyncResult(
            newModelsCount = newlyDiscovered.size,
            updatedModelsCount = updatedVehicles.size,
            totalCount = finalList.size,
            newlyDiscoveredVehicles = newlyDiscovered,
            updatedVehicles = updatedVehicles,
            syncTimestamp = System.currentTimeMillis()
        )

        return Pair(finalList, result)
    }

    /**
     * Resets a single standard vehicle to its official factory specifications.
     */
    fun resetSingleToFactory(vehicleId: String, currentList: List<Vehicle>): List<Vehicle> {
        val official = VehicleCatalog.defaultVehicles.find { it.id == vehicleId } ?: return currentList
        return currentList.map { if (it.id == vehicleId) official else it }
    }
}
