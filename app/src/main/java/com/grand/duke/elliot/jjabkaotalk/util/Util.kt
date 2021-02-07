package com.grand.duke.elliot.jjabkaotalk.util

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import java.security.MessageDigest
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateFormat(pattern: String): String = SimpleDateFormat(pattern, Locale.getDefault()).format(this)
fun Long.toLocalTimeString(): String {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
        .apply {
            timeZone = TimeZone.getDefault()
        }
    val timeFormat = SimpleDateFormat("a hh:mm", Locale.getDefault())
        .apply {
            timeZone = TimeZone.getDefault()
        }

    // val localDate = dateFormat.format(Date(this))
    val localTime = timeFormat.format(Date(this))
    // return "$localDate $localTime"
    return "$localTime "
}

fun hashString(input: String, algorithm: String = "SHA-256"): String {
    return MessageDigest.getInstance(algorithm)
            .digest(input.toByteArray())
            .fold("", { string, it -> string + "%02x".format(it) })
}

fun FirebaseFirestoreException?.isNotNull() = this != null
fun FirebaseFirestoreException?.isNull() = this == null

fun ListenerRegistration?.isNull() = this == null