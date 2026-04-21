package com.gtu.employeeperformancetracker.utils

object FirebaseErrorFormatter {

    fun format(message: String?, fallback: String): String {
        val normalized = message?.trim().orEmpty()
        if (normalized.isBlank()) return fallback

        val lower = normalized.lowercase()

        return when {
            "configuration_not_found" in lower -> {
                "Firebase Email/Password sign-in is not configured yet. Open Firebase Console > Authentication > Sign-in method, enable Email/Password, then retry."
            }

            "client is offline" in lower -> {
                "The app could not reach Firebase. Check the device internet connection, make sure Firestore Database is created in Firebase Console, then retry."
            }

            "cloud firestore api has not been used" in lower || "firestore.googleapis.com" in lower -> {
                "Cloud Firestore is not enabled for this Firebase project yet. Open Firebase Console > Firestore Database and create the database, then open the Google Cloud API page for this project, enable Cloud Firestore API, wait a few minutes, and retry."
            }

            "permission_denied" in lower -> {
                "Firebase denied this request. Check that Firestore Database exists, the Firestore API is enabled, and your Firestore rules allow this operation."
            }

            "network error" in lower -> {
                "A network error occurred while talking to Firebase. Check internet access and retry."
            }

            else -> normalized
        }
    }
}
