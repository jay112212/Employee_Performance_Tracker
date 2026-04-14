package com.gtu.employeeperformancetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gtu.employeeperformancetracker.data.local.entity.AppSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: AppSession)

    @Query("SELECT * FROM app_session WHERE id = 1 LIMIT 1")
    fun observeSession(): Flow<AppSession?>
}
