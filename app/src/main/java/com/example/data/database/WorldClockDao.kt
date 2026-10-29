package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WorldClockCity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldClockDao {
    @Query("SELECT * FROM world_cities ORDER BY displayOrder ASC, cityName ASC")
    fun getAllCities(): Flow<List<WorldClockCity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: WorldClockCity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<WorldClockCity>)

    @Update
    suspend fun updateCity(city: WorldClockCity)

    @Delete
    suspend fun deleteCity(city: WorldClockCity)

    @Query("DELETE FROM world_cities WHERE id = :id")
    suspend fun deleteCityById(id: String)
}
