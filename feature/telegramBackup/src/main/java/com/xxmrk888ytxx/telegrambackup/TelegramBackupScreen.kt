package com.xxmrk888ytxx.telegrambackup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivy.navigation.navigation
import com.xxmrk888ytxx.telegrambackup.model.BackupRepeatTime
import com.xxmrk888ytxx.telegrambackup.model.TelegramBackupEvent
import com.xxmrk888ytxx.telegrambackup.model.TelegramBackupState
import kotlinx.collections.immutable.persistentListOf

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
        is TelegramBackupState.BackupConfiguration -> BackupConfigurationState(
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
    telegramBackupState: TelegramBackupState.BackupConfiguration,
    onEvent: (TelegramBackupEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SwitchParam(
                text = "Enable auto backup to telegram",
                isChecked = telegramBackupState.backupSettings.isEnabled,
                onStateChanged = { onEvent(TelegramBackupEvent.ChangeTelegramBackupState(it)) },
            )
        }


        item {
            Text(text = "Backup frequency", modifier = Modifier.padding(bottom = 10.dp))

            SelectTimeWidget(
                isEnabled = telegramBackupState.backupSettings.isEnabled,
                selectedTime = telegramBackupState.backupSettings.backupRepeatTime,
                onBackupTimeChanged = { onEvent(TelegramBackupEvent.ChangeBackupRepeatTimeEvent(it)) }
            )
        }

        item {
            ActionButton(
                text = "Create backup now",
                onClick = { onEvent(TelegramBackupEvent.CreateBackupNowEvent) }
            )
        }

        item {
            ActionButton(
                text = "Remove telegram data",
                onClick = { onEvent(TelegramBackupEvent.RemoveTelegramDataEvent) }
            )
        }
    }
}

@Composable
fun ActionButton(
    text:String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(id = com.ivy.ui.R.drawable.baseline_arrow_forward_ios_24),
            contentDescription = "",
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTimeWidget(
    isEnabled: Boolean,
    selectedTime:BackupRepeatTime,
    onBackupTimeChanged:(BackupRepeatTime) -> Unit
) {
    data class SelectTimeWidgetItem(
        val title:String,
        val linkedBackupRepeatTime:BackupRepeatTime
    )

    val context = LocalContext.current

    val items = remember {
        persistentListOf(
            SelectTimeWidgetItem(
                title = "6 hours",
                linkedBackupRepeatTime = BackupRepeatTime.HOURS_6
            ),
            SelectTimeWidgetItem(
                title = "12 hours",
                linkedBackupRepeatTime = BackupRepeatTime.HOURS_12
            ),
            SelectTimeWidgetItem(
                title = "1 day",
                linkedBackupRepeatTime = BackupRepeatTime.DAY_1
            ),
            SelectTimeWidgetItem(
                title = "1 week",
                linkedBackupRepeatTime = BackupRepeatTime.WEEK_1
            ),
        )
    }


    LazyRow(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) {
            FilterChip(
                selected = selectedTime == it.linkedBackupRepeatTime,
                onClick = { onBackupTimeChanged(it.linkedBackupRepeatTime) },
                label = {
                    Text(text = it.title)
                },
                enabled = isEnabled
            )
        }
    }
}

@Composable
private fun SwitchParam(
    text:String,
    isChecked:Boolean,
    onStateChanged:(Boolean) -> Unit,
    isEnabled:Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = isChecked,
            onCheckedChange = onStateChanged,
            enabled = isEnabled
        )
    }
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