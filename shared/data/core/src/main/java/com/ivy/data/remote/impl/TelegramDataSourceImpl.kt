package com.ivy.data.remote.impl

import android.content.Context
import com.ivy.data.exception.ApiExceptionData
import com.ivy.data.exception.DataIvyException
import com.ivy.data.exception.DataUnknownException
import com.ivy.data.exception.NoConnectionExceptionData
import com.ivy.data.remote.TelegramDataSource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TelegramDataSourceImpl(
    private val userId: String,
    private val botKey: String,
    private val ktorClient: HttpClient,
    private val context: Context
) : TelegramDataSource {

    private val sendMessageUrl: String
        get() = "https://api.telegram.org/bot$botKey/sendMessage"

    private val sendDocumentUrl:String
        get() = "https://api.telegram.org/bot$botKey/sendDocument"

    override suspend fun sendMessage(text: String): Result<Unit> = call {
        val result = ktorClient.get(sendMessageUrl) {
            parameter("chat_id", userId)
            parameter("text", text)
        }

        when (result.status.value) {
            in 200..299 -> Result.success(Unit)
            in 400..499 -> throw ApiExceptionData()
            else -> throw DataUnknownException()
        }
    }

    override suspend fun uploadFile(file: File): Result<Unit> = call {
        val fileBytes = file.inputStream().buffered().use { it.readBytes() }


        val formData = formData {
            append(
                "document",
                fileBytes,
                Headers.build {
                    append(
                        HttpHeaders.ContentDisposition,
                        "${ContentDisposition.Parameters.FileName}=\"${file.name}\" "
                    )
                }
            )
        }

        val result = ktorClient.post(sendDocumentUrl) {
            parameter("chat_id",userId)

            setBody(
                MultiPartFormDataContent(parts = formData)
            )

            method = HttpMethod.Post
        }



        when(result.status.value) {
            in 200..299 -> Result.success(Unit)
            in 400..499 -> throw ApiExceptionData()
            else -> throw DataUnknownException()
        }
    }


    private suspend fun call(block: suspend () -> Unit): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                try {
                    block()
                } catch (e: ClientRequestException) {
                    throw ApiExceptionData(e.stackTraceToString())
                } catch (e: IOException) {
                    throw NoConnectionExceptionData(e.stackTraceToString())
                } catch (e: DataIvyException) {
                    throw e
                } catch (e: Exception) {
                    throw DataUnknownException(e.stackTraceToString())
                }
            }
        }
}