package com.ivy.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Green

data class InstalledAppInfo(
    val appName: String,
    val packageName: String
)

fun isNotificationListenerPermissionGranted(context: Context): Boolean {
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat != null && flat.contains(context.packageName)
}

fun openNotificationListenerSettings(context: Context) {
    try {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getInstalledAppsList(context: Context): List<InstalledAppInfo> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(mainIntent, 0)
    }
    return resolveInfos.map { resolveInfo ->
        InstalledAppInfo(
            appName = resolveInfo.loadLabel(pm).toString(),
            packageName = resolveInfo.activityInfo.packageName
        )
    }.distinctBy { it.packageName }.sortedBy { it.appName }
}

fun parsePackageList(targetPackageString: String): List<String> {
    return targetPackageString.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

fun formatPackageList(packages: List<String>): String {
    return packages.joinToString(",")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationParserSection(
    enabled: Boolean,
    targetPackage: String,
    regexPattern: String,
    onSetEnabled: (Boolean) -> Unit,
    onSetTargetPackage: (String) -> Unit,
    onSetRegexPattern: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showAppPickerModal by remember { mutableStateOf(false) }
    var newPackageInput by remember { mutableStateOf("") }
    var permissionGranted by remember { mutableStateOf(isNotificationListenerPermissionGranted(context)) }

    val currentPackages = remember(targetPackage) { parsePackageList(targetPackage) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = isNotificationListenerPermissionGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        SettingsSectionDivider(text = stringResource(R.string.bank_notification_monitoring_title))

        Spacer(Modifier.height(16.dp))

        // Toggle Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.bank_notification_auto_process),
                    style = UI.typo.b1.style(
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.pureInverse
                    )
                )
                Text(
                    text = stringResource(R.string.bank_notification_auto_process_desc),
                    style = UI.typo.c.style(
                        color = UI.colors.gray
                    )
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onSetEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = UI.colors.pure,
                    checkedTrackColor = Green
                )
            )
        }

        if (enabled) {
            Spacer(Modifier.height(16.dp))

            // Permission status & button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (permissionGranted) UI.colors.medium.copy(alpha = 0.4f) else UI.colors.red.copy(alpha = 0.15f))
                    .clickable {
                        openNotificationListenerSettings(context)
                    }
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = if (permissionGranted) {
                            stringResource(R.string.bank_notification_permission_granted)
                        } else {
                            stringResource(R.string.bank_notification_permission_required)
                        },
                        style = UI.typo.b2.style(
                            fontWeight = FontWeight.Bold,
                            color = if (permissionGranted) Green else UI.colors.red
                        )
                    )
                    Text(
                        text = if (permissionGranted) {
                            stringResource(R.string.bank_notification_permission_granted_desc)
                        } else {
                            stringResource(R.string.bank_notification_permission_required_desc)
                        },
                        style = UI.typo.c.style(color = UI.colors.pureInverse)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Monitored Apps Chips List
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = stringResource(R.string.bank_notification_monitored_apps),
                    style = UI.typo.b2.style(fontWeight = FontWeight.Bold, color = UI.colors.pureInverse)
                )
                Spacer(Modifier.height(6.dp))

                if (currentPackages.isEmpty()) {
                    Text(
                        text = stringResource(R.string.bank_notification_all_apps_hint),
                        style = UI.typo.c.style(color = UI.colors.gray)
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentPackages.forEach { pkg ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(UI.colors.medium)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pkg,
                                    style = UI.typo.c.style(fontWeight = FontWeight.Bold, color = UI.colors.pureInverse)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "✕",
                                    style = UI.typo.c.style(fontWeight = FontWeight.Bold, color = UI.colors.red),
                                    modifier = Modifier.clickable {
                                        val updated = currentPackages.filter { it != pkg }
                                        onSetTargetPackage(formatPackageList(updated))
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Manual add text field + Add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPackageInput,
                        onValueChange = { newPackageInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.bank_notification_add_package_hint)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green,
                            unfocusedBorderColor = UI.colors.medium
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Green)
                            .clickable {
                                if (newPackageInput.isNotBlank()) {
                                    val updated = (currentPackages + newPackageInput.trim()).distinct()
                                    onSetTargetPackage(formatPackageList(updated))
                                    newPackageInput = ""
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.bank_notification_add),
                            style = UI.typo.c.style(fontWeight = FontWeight.Bold, color = UI.colors.pure)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(UI.colors.medium)
                        .clickable { showAppPickerModal = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.bank_notification_select_installed_app),
                        style = UI.typo.c.style(fontWeight = FontWeight.Bold, color = UI.colors.pureInverse)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Custom Regex Pattern
            val defaultPattern = "\\(?(-?\\d+(?:[\\.,]\\d+)?)\\s*([A-Za-z]{3})\\)?"
            var localRegex by remember(regexPattern) { mutableStateOf(regexPattern) }

            val isRegexValid = remember(localRegex) {
                if (localRegex.isBlank()) true else try {
                    java.util.regex.Pattern.compile(localRegex)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            val hasUnsavedChanges = localRegex != regexPattern

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = stringResource(R.string.bank_notification_regex_pattern),
                    style = UI.typo.b2.style(fontWeight = FontWeight.Bold, color = UI.colors.pureInverse)
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = localRegex,
                    onValueChange = { localRegex = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.bank_notification_regex_placeholder)) },
                    singleLine = true,
                    isError = !isRegexValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isRegexValid) Green else UI.colors.red,
                        unfocusedBorderColor = if (isRegexValid) UI.colors.medium else UI.colors.red
                    )
                )
                Spacer(Modifier.height(4.dp))
                if (!isRegexValid) {
                    Text(
                        text = stringResource(R.string.bank_notification_regex_invalid),
                        style = UI.typo.c.style(color = UI.colors.red)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.bank_notification_regex_hint),
                        style = UI.typo.c.style(color = UI.colors.gray)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRegexValid && hasUnsavedChanges) Green else UI.colors.medium.copy(alpha = 0.5f))
                            .clickable(enabled = isRegexValid && hasUnsavedChanges) {
                                onSetRegexPattern(localRegex)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.bank_notification_save_regex),
                            style = UI.typo.c.style(
                                fontWeight = FontWeight.Bold,
                                color = if (isRegexValid && hasUnsavedChanges) UI.colors.pure else UI.colors.gray
                            )
                        )
                    }

                    // Restore Default Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(UI.colors.medium)
                            .clickable {
                                localRegex = defaultPattern
                                onSetRegexPattern(defaultPattern)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.bank_notification_restore_default),
                            style = UI.typo.c.style(
                                fontWeight = FontWeight.Bold,
                                color = UI.colors.pureInverse
                            )
                        )
                    }
                }
            }
        }
    }

    if (showAppPickerModal) {
        AppPickerModal(
            currentSelectedPackages = currentPackages,
            onDismiss = { showAppPickerModal = false },
            onAppToggled = { selectedPkg ->
                val updated = if (currentPackages.contains(selectedPkg)) {
                    currentPackages.filter { it != selectedPkg }
                } else {
                    currentPackages + selectedPkg
                }
                onSetTargetPackage(formatPackageList(updated))
            }
        )
    }
}

@Composable
fun AppPickerModal(
    currentSelectedPackages: List<String>,
    onDismiss: () -> Unit,
    onAppToggled: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val allApps = remember { getInstalledAppsList(context) }
    val filteredApps = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            allApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(UI.colors.pure)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.bank_notification_select_app_dialog_title),
                    style = UI.typo.b1.style(fontWeight = FontWeight.Bold, color = UI.colors.pureInverse)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.bank_notification_search_placeholder)) },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isSelected = currentSelectedPackages.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppToggled(app.packageName) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    style = UI.typo.b1.style(fontWeight = FontWeight.Bold, color = UI.colors.pureInverse)
                                )
                                Text(
                                    text = app.packageName,
                                    style = UI.typo.c.style(color = UI.colors.gray)
                                )
                            }
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    style = UI.typo.b1.style(fontWeight = FontWeight.Bold, color = Green)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
