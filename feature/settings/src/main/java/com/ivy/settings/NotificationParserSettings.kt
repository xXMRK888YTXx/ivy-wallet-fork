package com.ivy.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.ivy.design.l1_buildingBlocks.IconScale
import com.ivy.design.l1_buildingBlocks.IvyIconScaled
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.GradientGreen
import com.ivy.wallet.ui.theme.GradientRed
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.IvyButton
import com.ivy.wallet.ui.theme.components.IvyOutlinedButton
import com.ivy.wallet.ui.theme.components.IvySwitch

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

        // Native Ivy Wallet Toggle Switch Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(UI.shapes.r4)
                .border(2.dp, UI.colors.medium, UI.shapes.r4)
                .background(UI.colors.medium)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GradientGreen.asHorizontalBrush()),
                    contentAlignment = Alignment.Center
                ) {
                    IvyIconScaled(
                        icon = R.drawable.ic_notification,
                        iconScale = IconScale.S,
                        tint = White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
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
            }

            Spacer(Modifier.width(8.dp))

            IvySwitch(enabled = enabled) {
                onSetEnabled(it)
            }
        }

        if (enabled) {
            Spacer(Modifier.height(12.dp))

            // Permission Status Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(UI.shapes.r4)
                    .border(
                        2.dp,
                        if (permissionGranted) Green.copy(alpha = 0.5f) else Red.copy(alpha = 0.5f),
                        UI.shapes.r4
                    )
                    .background(if (permissionGranted) Green.copy(alpha = 0.08f) else Red.copy(alpha = 0.08f))
                    .clickable { openNotificationListenerSettings(context) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background((if (permissionGranted) GradientGreen else GradientRed).asHorizontalBrush()),
                    contentAlignment = Alignment.Center
                ) {
                    IvyIconScaled(
                        icon = if (permissionGranted) R.drawable.ic_secure else R.drawable.ic_buffer_exceeded,
                        iconScale = IconScale.S,
                        tint = White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (permissionGranted) {
                            stringResource(R.string.bank_notification_permission_granted)
                        } else {
                            stringResource(R.string.bank_notification_permission_required)
                        },
                        style = UI.typo.b2.style(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (permissionGranted) Green else Red
                        )
                    )
                    Spacer(Modifier.height(2.dp))
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
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.bank_notification_monitored_apps),
                    style = UI.typo.b2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = UI.colors.pureInverse
                    )
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
                                    .clip(UI.shapes.r2)
                                    .border(1.dp, UI.colors.medium, UI.shapes.r2)
                                    .background(UI.colors.medium)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pkg,
                                    style = UI.typo.c.style(fontWeight = FontWeight.Bold, color = UI.colors.pureInverse)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "✕",
                                    style = UI.typo.c.style(
                                        fontWeight = FontWeight.Bold,
                                        color = Red
                                    ),
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

                // Manual Add Text Field + Add Button
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
                        shape = UI.shapes.r2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green,
                            unfocusedBorderColor = UI.colors.medium
                        )
                    )
                    Spacer(Modifier.width(8.dp))

                    IvyButton(
                        text = stringResource(R.string.bank_notification_add),
                        backgroundGradient = GradientGreen,
                        enabled = newPackageInput.isNotBlank()
                    ) {
                        if (newPackageInput.isNotBlank()) {
                            val updated = (currentPackages + newPackageInput.trim()).distinct()
                            onSetTargetPackage(formatPackageList(updated))
                            newPackageInput = ""
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                IvyOutlinedButton(
                    text = stringResource(R.string.bank_notification_select_installed_app),
                    iconStart = R.drawable.ic_custom_account_s
                ) {
                    showAppPickerModal = true
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

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.bank_notification_regex_pattern),
                    style = UI.typo.b2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = UI.colors.pureInverse
                    )
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = localRegex,
                    onValueChange = { localRegex = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.bank_notification_regex_placeholder)) },
                    singleLine = true,
                    isError = !isRegexValid,
                    shape = UI.shapes.r2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isRegexValid) Green else Red,
                        unfocusedBorderColor = if (isRegexValid) UI.colors.medium else Red
                    )
                )
                Spacer(Modifier.height(4.dp))
                if (!isRegexValid) {
                    Text(
                        text = stringResource(R.string.bank_notification_regex_invalid),
                        style = UI.typo.c.style(color = Red)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.bank_notification_regex_hint),
                        style = UI.typo.c.style(color = UI.colors.gray)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        IvyButton(
                            text = stringResource(R.string.bank_notification_save_regex),
                            backgroundGradient = GradientGreen,
                            enabled = isRegexValid && hasUnsavedChanges
                        ) {
                            onSetRegexPattern(localRegex)
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        IvyOutlinedButton(
                            text = stringResource(R.string.bank_notification_restore_default),
                            iconStart = R.drawable.ic_refresh
                        ) {
                            localRegex = defaultPattern
                            onSetRegexPattern(defaultPattern)
                        }
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

@OptIn(ExperimentalFoundationApi::class)
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(UI.colors.pure)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { /* consume click inside card */ }
                    .padding(20.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(UI.colors.gray.copy(alpha = 0.4f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.bank_notification_select_app_dialog_title),
                            style = UI.typo.b1.style(
                                fontWeight = FontWeight.ExtraBold,
                                color = UI.colors.pureInverse
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(UI.colors.medium)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                style = UI.typo.b2.style(
                                    fontWeight = FontWeight.Bold,
                                    color = UI.colors.pureInverse
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.bank_notification_search_placeholder)) },
                        singleLine = true,
                        shape = UI.shapes.r2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green,
                            unfocusedBorderColor = UI.colors.medium
                        )
                    )
                    Spacer(Modifier.height(12.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isSelected = currentSelectedPackages.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .fillMaxWidth()
                                    .clip(UI.shapes.r2)
                                    .clickable { onAppToggled(app.packageName) }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(UI.colors.medium),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IvyIconScaled(
                                            icon = R.drawable.ic_custom_account_s,
                                            iconScale = IconScale.S,
                                            tint = UI.colors.pureInverse
                                        )
                                    }

                                    Spacer(Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = app.appName,
                                            style = UI.typo.b1.style(
                                                fontWeight = FontWeight.Bold,
                                                color = UI.colors.pureInverse
                                            )
                                        )
                                        Text(
                                            text = app.packageName,
                                            style = UI.typo.c.style(color = UI.colors.gray)
                                        )
                                    }
                                }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(GradientGreen.asHorizontalBrush()),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        style = UI.typo.b2.style(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
