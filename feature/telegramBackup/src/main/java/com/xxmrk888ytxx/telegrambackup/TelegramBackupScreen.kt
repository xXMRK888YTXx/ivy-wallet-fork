package com.xxmrk888ytxx.telegrambackup

import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivy.navigation.navigation
import com.xxmrk888ytxx.telegrambackup.model.TelegramBackupEvent
import com.xxmrk888ytxx.telegrambackup.model.TelegramBackupState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramBackupScreen() {
    val telegramBackupViewModel = viewModel<TelegramBackupViewModel>()


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Title()
                },
                navigationIcon = {
                    BackButton()
                },
            )
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Content(
                telegramBackupState = telegramBackupViewModel.uiState(),
                onEvent = telegramBackupViewModel::onEvent,
            )
        }
    }
}

@Composable
private fun Content(
    telegramBackupState: TelegramBackupState,
    onEvent: (TelegramBackupEvent) -> Unit
) {
    when (telegramBackupState) {
        TelegramBackupState.BackupConfiguration -> BackupConfigurationState(
            telegramBackupState,
            onEvent
        )

        TelegramBackupState.Loading -> LoadingState()
        is TelegramBackupState.EnterTelegramData -> EnterTelegramDataState(
            telegramBackupState,
            onEvent
        )
    }
}

@Composable
private fun EnterTelegramDataState(
    telegramBackupState: TelegramBackupState.EnterTelegramData,
    onEvent: (TelegramBackupEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputTelegramDataTextField(
            text = telegramBackupState.userId,
            onChange = { onEvent(TelegramBackupEvent.UserIdTextFieldChanged(it)) },
            label = "UserId"
        )

        InputTelegramDataTextField(
            text = telegramBackupState.botToken,
            onChange = { onEvent(TelegramBackupEvent.BotTokenTextFieldChanged(it)) },
            label = "Bot key"
        )

        SaveTelegramDataButton(
            onClick = { onEvent(TelegramBackupEvent.SaveNewTelegramData) },
            enabled = telegramBackupState.isSaveButtonEnabled
        )
    }
}

@Composable
private fun SaveTelegramDataButton(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        enabled = enabled
    ) {
        Text(text = "Save")
    }
}

@Composable
private fun InputTelegramDataTextField(
    text: String,
    onChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        singleLine = true,
        label = { Text(text = label, style = MaterialTheme.typography.titleMedium) })
}

@Composable
private fun BackupConfigurationState(
    telegramBackupState: TelegramBackupState,
    onEvent: (TelegramBackupEvent) -> Unit
) {

}


@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}


@Composable
private fun Title(
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = "Telegram backup",
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun BackButton(
    modifier: Modifier = Modifier,
) {
    val nav = navigation()
    IconButton(
        modifier = modifier,
        onClick = {
            nav.back()
        }
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Back"
        )
    }
}