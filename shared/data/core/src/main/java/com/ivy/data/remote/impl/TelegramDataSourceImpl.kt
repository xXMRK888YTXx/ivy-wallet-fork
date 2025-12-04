package com.ivy.data.remote.impl

import com.ivy.data.exception.NoConnectionExceptionData
import com.ivy.data.exception.DataUnknownException
import com.ivy.data.remote.TelegramDataSource
import com.ivy.data.exception.ApiExceptionData
import com.ivy.data.exception.DataIvyException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TelegramDataSourceImpl(
    private val userId: String,
    private val botKey: String,
    private val ktorClient: HttpClient
) : TelegramDataSource {

    private val sendMessageUrl: String
        get() = "https://api.telegram.org/bot$botKey/sendMessage"

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