package com.ivy.domain.exception

import com.ivy.data.exception.ApiDataException
import com.ivy.data.exception.DataIvyException
import com.ivy.data.exception.NoConnectionExceptionData

sealed class DomainIvyException(m: String? = null) : Exception(m)

fun DataIvyException.toDomainException() : DomainIvyException = when(this) {
    is ApiDataException -> InvalidTelegramDataException(message)
    is NoConnectionExceptionData -> NetworkException(message)
    else -> UnknownException(message)
}