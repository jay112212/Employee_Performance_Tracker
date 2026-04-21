package com.gtu.employeeperformancetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gtu.employeeperformancetracker.data.local.entity.EmailLog
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(emailLog: EmailLog): Long

    @Query("SELECT * FROM email_logs ORDER BY sent_at DESC")
    fun observeEmails(): Flow<List<EmailLog>>

    @Query("SELECT * FROM email_logs WHERE recipient_email = :recipientEmail ORDER BY sent_at DESC")
    fun observeEmailsForRecipient(recipientEmail: String): Flow<List<EmailLog>>
}
