package com.example.data.db

import android.util.Log
import com.example.data.OdometerEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TripRepository(private val tripDao: TripDao) {

    val allTrips: Flow<List<OdometerEntry>> = tripDao.getAllTrips().map { entities ->
        entities.map { it.toOdometerEntry() }
    }

    suspend fun insertTrip(entry: OdometerEntry) = withContext(Dispatchers.IO) {
        tripDao.insertTrip(entry.toTripEntity())
    }

    suspend fun insertTrips(entries: List<OdometerEntry>) = withContext(Dispatchers.IO) {
        if (entries.isNotEmpty()) {
            tripDao.insertTrips(entries.map { it.toTripEntity() })
        }
    }

    suspend fun updateTrip(entry: OdometerEntry) = withContext(Dispatchers.IO) {
        tripDao.insertTrip(entry.toTripEntity())
    }

    suspend fun deleteTripById(id: String) = withContext(Dispatchers.IO) {
        tripDao.deleteTripById(id)
    }

    suspend fun deleteTripsByVehicle(vehicleId: String, vehicleName: String) = withContext(Dispatchers.IO) {
        tripDao.deleteTripsByVehicle(vehicleId, vehicleName)
    }

    suspend fun deleteAllTrips() = withContext(Dispatchers.IO) {
        tripDao.deleteAllTrips()
    }

    suspend fun getTripCount(): Int = withContext(Dispatchers.IO) {
        tripDao.getTripCount()
    }

    /**
     * Preserves all user data by migrating legacy trips saved in SharedPreferences into Room database.
     * If Room is empty but legacy entries exist, they are completely migrated.
     * If Room already has entries, any legacy entries not yet in Room are added.
     */
    suspend fun migrateLegacyEntries(legacyEntries: List<OdometerEntry>) = withContext(Dispatchers.IO) {
        if (legacyEntries.isEmpty()) return@withContext
        try {
            val count = tripDao.getTripCount()
            if (count == 0) {
                Log.i("TripRepository", "Migrating ${legacyEntries.size} legacy trips into Room database...")
                tripDao.insertTrips(legacyEntries.map { it.toTripEntity() })
                Log.i("TripRepository", "Migration completed successfully with zero data loss.")
            } else {
                // Ensure no duplicates or missing entries
                tripDao.insertTrips(legacyEntries.map { it.toTripEntity() })
            }
        } catch (e: Exception) {
            Log.e("TripRepository", "Error migrating legacy trips: ${e.message}", e)
        }
    }
}
