package com.grand.duke.elliot.jjabkaotalk.util

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateFormat(pattern: String): String = SimpleDateFormat(pattern, Locale.getDefault()).format(this)

fun hashString(input: String, algorithm: String = "SHA-256"): String {
    return MessageDigest.getInstance(algorithm)
            .digest(input.toByteArray())
            .fold("", { string, it -> string + "%02x".format(it) })
}

