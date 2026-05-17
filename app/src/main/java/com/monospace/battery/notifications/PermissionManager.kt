package com.monospace.battery.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    /**
     * Request permission. 
     * @param forceSettings If true, will open settings if the system dialog cannot be shown.
     */
    fun requestNotificationPermission(
        activity: ComponentActivity,
        launcher: ActivityResultLauncher<String>,
        forceSettings: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS

            when {
                hasNotificationPermission() -> {
                    // Already granted
                }

                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> {
                    // Show rationale (standard Android behavior)
                    launcher.launch(permission)
                }

                forceSettings -> {
                    // Only open settings if user explicitly clicked a button and rationale is false
                    // (which means they checked "don't ask again" or it's the second time)
                    runCatching {
                        openAppSettings()
                    }.onFailure {
                        launcher.launch(permission)
                    }
                }

                else -> {
                    // Standard launch (on app start)
                    launcher.launch(permission)
                }
            }
        }
    }

    fun openAppSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Directly to notification settings for this app (Android 8.0+)
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            // General app settings for older versions
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
