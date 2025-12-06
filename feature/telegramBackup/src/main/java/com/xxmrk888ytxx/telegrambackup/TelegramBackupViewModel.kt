package com.xxmrk888ytxx.telegrambackup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewModelScope
import com.ivy.base.Toaster
import com.ivy.domain.exception.InvalidTelegramDataException
import com.ivy.domain.exception.NetworkException
import com.ivy.domain.model.TelegramBackupRepeatTime
import com.ivy.domain.model.TelegramBackupSettings
import com.ivy.domain.model.TelegramData
import com.ivy.domain.usecase.telegram.ManageTelegramBackupSettingsUseCase
import com.ivy.domain.usecase.telegram.ManageTelegramDataUseCase
import com.ivy.domain.usecase.telegram.TelegramUseCase
import com.ivy.ui.ComposeViewModel
import com.xxmrk888ytxx.telegrambackup.model.BackupRepeatTime
import com.xxmrk888ytxx.telegrambackup.model.BackupSettings
import com.xxmrk888ytxx.telegrambackup.model.BackupRepeatTime.Companion.toBackupTime
import com.xxmrk888ytxx.telegrambackup.model.TelegramBackupEvent
import com.xxmrk888ytxx.telegrambackup.model.TelegramBackupState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@Stable
@HiltViewModel
class TelegramBackupViewModel @Inject constructor(
    private val manageTelegramDataUseCase: ManageTelegramDataUseCase,
    private val telegramUseCase: TelegramUseCase,
    private val toaster: Toaster,
    private val manageTelegramBackupSettingsUseCase: ManageTelegramBackupSettingsUseCase
) : ComposeViewModel<TelegramBackupState, TelegramBackupEvent>() {

    private val _uiState = MutableStateFlow<TelegramBackupState>(TelegramBackupState.Loading)

    private val uiState = combine(
        _uiState,
        manageTelegramBackupSettingsUseCase.backupSettings
    ) { uiState, backupSettings ->
        when (uiState) {
            is TelegramBackupState.BackupConfiguration -> TelegramBackupState.BackupConfiguration(
                backupSettings.toBackupSettings()
            )

            else -> uiState
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TelegramBackupState.Loading)

    @Composable
    override fun uiState(): TelegramBackupState {
        val uiState by uiState.collectAsState()
        return uiState
    }

    override fun onEvent(event: TelegramBackupEvent) {
        when (event) {
            is TelegramBackupEvent.UserIdTextFieldChanged -> {
                if (!isUserIdInputValid(event.text)) return
                updateEnterTelegramDataState { it.copy(userId = event.text) }
            }

            is TelegramBackupEvent.BotTokenTextFieldChanged -> updateEnterTelegramDataState {
                it.copy(botToken = event.text)
            }

            is TelegramBackupEvent.SaveNewTelegramData -> saveTelegramData()

            is TelegramBackupEvent.ChangeTelegramBackupState -> changeTelegramBackupState(event.isEnabled)

            is TelegramBackupEvent.ChangeBackupRepeatTimeEvent -> changeTelegramBackupRepeatTime(event.backupRepeatTime)

            TelegramBackupEvent.RemoveTelegramDataEvent -> removeTelegramData()

            TelegramBackupEvent.CreateBackupNowEvent -> createBackupNow()
        }
    }

    private fun createBackupNow() = viewModelScope.launch { manageTelegramBackupSettingsUseCase.makeBackupNow() }

    private fun removeTelegramData() {
        viewModelScope.launch {
            toLoadingState()
            changeTelegramBackupState(false)
            manageTelegramDataUseCase.removeTelegramData()
            toEnterTelegramDataState()
            toaster.show("Telegram data removed successfully.")
        }
    }

    private fun changeTelegramBackupRepeatTime(backupRepeatTime: BackupRepeatTime) =
        viewModelScope.launch {
            manageTelegramBackupSettingsUseCase.setBackupTime(backupRepeatTime.toTelegramBackupRepeatTime())
        }

    private fun BackupRepeatTime.toTelegramBackupRepeatTime(): TelegramBackupRepeatTime {
        return when(this) {
            BackupRepeatTime.HOURS_6 -> TelegramBackupRepeatTime.HOURS_6
            BackupRepeatTime.HOURS_12 -> TelegramBackupRepeatTime.HOURS_12
            BackupRepeatTime.DAY_1 -> TelegramBackupRepeatTime.DAY_1
            BackupRepeatTime.WEEK_1 -> TelegramBackupRepeatTime.WEEK_1
        }
    }

    private fun changeTelegramBackupState(isEnabled: Boolean) = viewModelScope.launch {
        manageTelegramBackupSettingsUseCase.setEnableState(isEnabled)
    }

    private fun saveTelegramData() = viewModelScope.launch {
        val enterTelegramDataState =
            _uiState.value as? TelegramBackupState.EnterTelegramData ?: return@launch
        val telegramData =
            TelegramData(enterTelegramDataState.userId, enterTelegramDataState.botToken)
        toLoadingState()
        telegramUseCase.validateTelegramData(telegramData)
            .onSuccess {
                manageTelegramDataUseCase.setupTelegramData(telegramData)
                toaster.show("Telegram data saved successfully")
                toBackupConfigurationState()
            }
            .onFailure { exception ->
                _uiState.value = enterTelegramDataState

                val toastMessage = when (exception) {
                    is NetworkException -> "Telegram is unavailable. Check your Internet connection"
                    is InvalidTelegramDataException -> "Invalid telegram data"
                    else -> "Unknown error"
                }
                toaster.show(toastMessage)
            }


    }

    private fun updateEnterTelegramDataState(onUpdate: (TelegramBackupState.EnterTelegramData) -> TelegramBackupState.EnterTelegramData) {
        val currentState = _uiState.value as? TelegramBackupState.EnterTelegramData ?: return
        val newState = onUpdate(currentState)
        _uiState.update { newState.copy(isSaveButtonEnabled = newState.isInputValid) }
    }

    private fun isUserIdInputValid(input: String): Boolean = input.isDigitsOnly()

    private val TelegramBackupState.EnterTelegramData.isInputValid: Boolean
        get() = listOf(userId, botToken).map { it.trim() }
            .all { it.isNotBlank() && it.isNotEmpty() }

    init {
        viewModelScope.launch { initScreen() }
    }

    private suspend fun initScreen() {
        val isTelegramDataSetup = manageTelegramDataUseCase.telegramData.first() != null

        when (isTelegramDataSetup) {
            true -> toBackupConfigurationState()
            false -> toEnterTelegramDataState()
        }
    }

    fun toEnterTelegramDataState() {
        _uiState.update { TelegramBackupState.EnterTelegramData() }
    }

    fun toBackupConfigurationState() {
        _uiState.update { TelegramBackupState.BackupConfiguration() }
    }

    fun toLoadingState() {
        _uiState.update { TelegramBackupState.Loading }
    }

    private fun TelegramBackupSettings.toBackupSettings(): BackupSettings {
        return BackupSettings(isEnabled, telegramBackupRepeatTime.toBackupTime())
    }
}