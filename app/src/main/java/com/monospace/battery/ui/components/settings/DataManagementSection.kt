package com.monospace.battery.ui.components.settings

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.ui.components.SettingsSection

@Composable
fun DataManagementSection() {
    val actions = LocalSettingsActions.current
    val context = LocalContext.current
    val showDeleteDialog = remember { mutableStateOf(false) }
    val successMessage = stringResource(R.string.settings_delete_success)

    if (showDeleteDialog.value) {
        DeleteHistoryDialog(
            onDismiss = { showDeleteDialog.value = false },
            onConfirm = {
                actions.onDeleteHistoryClick()
                showDeleteDialog.value = false
                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    SettingsSection(stringResource(R.string.settings_data_management)) {
        item(
            title = stringResource(R.string.settings_delete_history),
            description = stringResource(R.string.settings_delete_history_desc),
            iconRes = R.drawable.delete,
            onClick = { showDeleteDialog.value = true }
        )
    }
}

@Composable
private fun DeleteHistoryDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_delete_confirm_title)) },
        text = { Text(stringResource(R.string.settings_delete_confirm_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
