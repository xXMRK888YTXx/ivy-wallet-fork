package com.ivy.legacy.utils


fun sendToCrashlytics(
    msg: String
) {
    DeveloperException(msg).sendToCrashlytics(msg)
}

fun Exception.sendToCrashlytics(
    clarification: String? = null
) {
    clarification?.let {
        logToCrashlytics("Log: $it")
    }
}

fun logToCrashlytics(msg: String) {
}

class DeveloperException(msg: String) : Exception(msg)
