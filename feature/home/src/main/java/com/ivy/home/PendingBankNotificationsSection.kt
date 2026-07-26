package com.ivy.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ivy.base.model.TransactionType
import com.ivy.data.db.entity.ParsedNotificationEntity
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.l1_buildingBlocks.IconScale
import com.ivy.design.l1_buildingBlocks.IvyIconScaled
import com.ivy.navigation.EditTransactionScreen
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.GradientGreen
import com.ivy.wallet.ui.theme.GradientRed
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.IvyCircleButton
import com.ivy.wallet.ui.theme.components.IvyOutlinedButton
import kotlinx.collections.immutable.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun PendingBankNotificationsSection(
    pendingNotifications: ImmutableList<ParsedNotificationEntity>,
    onNotificationSelected: (ParsedNotificationEntity) -> Unit,
    onNotificationDeleted: (ParsedNotificationEntity) -> Unit,
    onClearAllNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (pendingNotifications.isEmpty()) return

    val nav = navigation()
    var showModal by remember { mutableStateOf(false) }

    // Ivy Wallet Native styled card on Home screen
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(UI.shapes.r4)
            .border(2.dp, UI.colors.medium, UI.shapes.r4)
            .background(UI.colors.pure)
            .clickable { showModal = true }
            .padding(16.dp),
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
                    text = stringResource(
                        R.string.bank_notification_pending_title,
                        pendingNotifications.size
                    ),
                    style = UI.typo.b1.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = UI.colors.pureInverse
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.bank_notification_card_desc),
                    style = UI.typo.c.style(
                        color = UI.colors.gray
                    )
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(UI.shapes.r2)
                .background(GradientGreen.asHorizontalBrush())
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${pendingNotifications.size} >",
                style = UI.typo.b2.style(
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            )
        }
    }

    if (showModal) {
        PendingBankNotificationsModal(
            pendingNotifications = pendingNotifications,
            onDismiss = { showModal = false },
            onClearAll = {
                showModal = false
                onClearAllNotifications()
            },
            onItemDeleted = { item ->
                onNotificationDeleted(item)
            },
            onItemSelected = { item, chosenAmount ->
                showModal = false
                val isExpense = chosenAmount < 0
                val trnType = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME
                nav.navigateTo(
                    EditTransactionScreen(
                        initialTransactionId = null,
                        type = trnType,
                        initialAmount = abs(chosenAmount),
                        initialTitle = null,
                        initialNote = null,
                        initialNotificationId = item.id
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun PendingBankNotificationsModal(
    pendingNotifications: ImmutableList<ParsedNotificationEntity>,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit,
    onItemDeleted: (ParsedNotificationEntity) -> Unit,
    onItemSelected: (ParsedNotificationEntity, Double) -> Unit
) {
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
                    // Top handle bar
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
                            text = stringResource(
                                R.string.bank_notification_pending_title,
                                pendingNotifications.size
                            ),
                            style = UI.typo.b1.style(
                                fontWeight = FontWeight.ExtraBold,
                                color = UI.colors.pureInverse
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IvyOutlinedButton(
                                text = stringResource(R.string.bank_notification_clear_all),
                                iconStart = R.drawable.ic_delete,
                                solidBackground = false
                            ) {
                                onClearAll()
                            }

                            Spacer(Modifier.width(8.dp))

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
                    }

                    Spacer(Modifier.height(16.dp))

                    val dateFormatter =
                        remember { SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()) }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(pendingNotifications, key = { it.id }) { item ->
                            val mainIsExpense = item.amount < 0
                            val formattedDate = dateFormatter.format(Date(item.timestamp))
                            val altAmountsList = remember(item.alternativeAmounts) {
                                item.alternativeAmounts?.split(",")
                                    ?.mapNotNull { it.toDoubleOrNull() } ?: emptyList()
                            }

                            Column(
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(UI.shapes.r2)
                                    .border(1.dp, UI.colors.medium, UI.shapes.r2)
                                    .background(UI.colors.medium.copy(alpha = 0.25f))
                                    .padding(12.dp)
                            ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onItemSelected(item, item.amount) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background((if (mainIsExpense) GradientRed else GradientGreen).asHorizontalBrush()),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IvyIconScaled(
                                            icon = if (mainIsExpense) R.drawable.ic_expense else R.drawable.ic_income,
                                            iconScale = IconScale.S,
                                            tint = White
                                        )
                                    }

                                    Spacer(Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = item.title ?: item.packageName,
                                            style = UI.typo.b2.style(
                                                fontWeight = FontWeight.Bold,
                                                color = UI.colors.pureInverse
                                            )
                                        )
                                        item.text?.let { noteText ->
                                            Text(
                                                text = noteText,
                                                style = UI.typo.c.style(color = UI.colors.gray),
                                                maxLines = 2
                                            )
                                        }
                                        Text(
                                            text = formattedDate,
                                            style = UI.typo.c.style(color = UI.colors.gray)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Primary Amount Pill Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(UI.shapes.r2)
                                            .background((if (mainIsExpense) GradientRed else GradientGreen).asHorizontalBrush())
                                            .clickable { onItemSelected(item, item.amount) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${if (mainIsExpense) "-" else "+"}${abs(item.amount)} ${item.currency ?: ""}".trim(),
                                            style = UI.typo.b2.style(
                                                fontWeight = FontWeight.Bold,
                                                color = White
                                            )
                                        )
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    IvyCircleButton(
                                        modifier = Modifier.size(36.dp),
                                        icon = R.drawable.ic_delete,
                                        backgroundGradient = GradientRed,
                                        tint = White,
                                        onClick = { onItemDeleted(item) }
                                    )
                                }
                            }

                                // Alternative Amount Chips
                            if (altAmountsList.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Alt:",
                                        style = UI.typo.c.style(color = UI.colors.gray)
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        altAmountsList.forEach { altVal ->
                                            val altIsExpense = altVal < 0
                                            Box(
                                                modifier = Modifier
                                                    .clip(UI.shapes.r2)
                                                    .border(1.dp, UI.colors.medium, UI.shapes.r2)
                                                    .background(UI.colors.pure)
                                                    .clickable { onItemSelected(item, altVal) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${if (altIsExpense) "-" else ""}${abs(altVal)} ${item.currency ?: ""}".trim(),
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
                        }
                    }
                }
            }
        }
    }
}
}
