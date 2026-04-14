package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.dao.EmailLogDao
import com.gtu.employeeperformancetracker.data.local.entity.EmailLog
import kotlinx.coroutines.flow.Flow

class EmailRepository(private val dao: EmailLogDao) {

    suspend fun insert(emailLog: EmailLog) = dao.insertEmail(emailLog)

    fun observeEmails(): Flow<List<EmailLog>> = dao.observeEmails()

    fun observeEmailsForRecipient(recipientEmail: String): Flow<List<EmailLog>> =
        dao.observeEmailsForRecipient(recipientEmail)
}
