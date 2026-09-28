package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY timestamp DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    fun getTripsByVehicle(vehicleId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTripById(id: String): TripEntity?

    @Query("SELECT COUNT(*) FROM trips")
    suspend fun getTripCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTripById(id: String)

    @Query("DELETE FROM trips WHERE vehicleId = :vehicleId OR vehicleName = :vehicleName")
    suspend fun deleteTripsByVehicle(vehicleId: String, vehicleName: String)

    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()
}
