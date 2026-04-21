package com.gtu.employeeperformancetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gtu.employeeperformancetracker.data.local.entity.AuthUser
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AuthUser): Long

    @Update
    suspend fun updateUser(user: AuthUser)

    @Query("SELECT * FROM auth_users ORDER BY role ASC, display_name ASC")
    fun observeUsers(): Flow<List<AuthUser>>

    @Query("SELECT * FROM auth_users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): AuthUser?

    @Query("SELECT * FROM auth_users WHERE email = :email AND password = :password AND is_active = 1 LIMIT 1")
    suspend fun authenticate(email: String, password: String): AuthUser?

    @Query("SELECT * FROM auth_users WHERE id = :id LIMIT 1")
    fun observeUserById(id: Int): Flow<AuthUser?>

    @Query("SELECT * FROM auth_users WHERE employee_id = :employeeId LIMIT 1")
    suspend fun getUserByEmployeeId(employeeId: Int): AuthUser?

    @Query("DELETE FROM auth_users WHERE employee_id = :employeeId")
    suspend fun deleteByEmployeeId(employeeId: Int)
}
