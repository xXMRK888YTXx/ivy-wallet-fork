package com.ivy.data.remote

import android.net.Uri
import java.io.File
import java.util.zip.ZipFile

interface TelegramDataSource {
    suspend fun sendMessage(text: String) : Result<Unit>
    suspend fun uploadFile(file: File): Result<Unit>
}