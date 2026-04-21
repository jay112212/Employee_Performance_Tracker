package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.entity.EmailLog
import com.gtu.employeeperformancetracker.data.remote.FirebaseStore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class EmailRepository(
    private val store: FirebaseStore = FirebaseStore()
) {

    private val emailLogs = store.firestore().collection(EMAIL_LOGS_COLLECTION)
    private val mailQueue = store.firestore().collection(MAIL_QUEUE_COLLECTION)

    fun observeEmails(): Flow<List<EmailLog>> = store.observeCollection(emailLogs) { it.toEmailLog() }

    fun observeEmailsForRecipient(recipientEmail: String): Flow<List<EmailLog>> = observeEmails()
        .map { items -> items.filter { it.recipientEmail.equals(recipientEmail, ignoreCase = true) } }

    suspend fun insert(emailLog: EmailLog) {
        val emailId = store.nextId(EMAIL_COUNTER_KEY)
        val savedEmail = emailLog.copy(id = emailId)
        emailLogs.document(emailId.toString())
            .set(savedEmail.toMap())
            .await()

        // If the Firebase Trigger Email extension is installed, this queue can send real emails.
        mailQueue.document("mail-$emailId")
            .set(
                mapOf(
                    "to" to listOf(savedEmail.recipientEmail),
                    "message" to mapOf(
                        "subject" to savedEmail.subject,
                        "text" to savedEmail.body
                    ),
                    "meta" to mapOf(
                        "recipientName" to savedEmail.recipientName,
                        "createdAt" to savedEmail.sentAt
                    )
                )
            )
            .await()
    }

    private fun EmailLog.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "recipientEmail" to recipientEmail,
        "recipientName" to recipientName,
        "subject" to subject,
        "body" to body,
        "sentAt" to sentAt
    )

    private fun DocumentSnapshot.toEmailLog(): EmailLog? {
        val recipientEmail = getString("recipientEmail") ?: return null
        return EmailLog(
            id = (getLong("id") ?: id.toLongOrNull() ?: 0L).toInt(),
            recipientEmail = recipientEmail,
            recipientName = getString("recipientName").orEmpty(),
            subject = getString("subject").orEmpty(),
            body = getString("body").orEmpty(),
            sentAt = getString("sentAt").orEmpty()
        )
    }

    companion object {
        private const val EMAIL_LOGS_COLLECTION = "email_logs"
        private const val MAIL_QUEUE_COLLECTION = "mail"
        private const val EMAIL_COUNTER_KEY = "email_logs"
    }
}
