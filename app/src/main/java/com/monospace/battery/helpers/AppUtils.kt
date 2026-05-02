package com.monospace.battery.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.net.toUri

object AppUtils {

    fun getVersionName(context: Context): String {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }.versionName
        }.getOrNull() ?: "1.0.0"
    }

    fun openPlayStore(context: Context) {
        val appPackageName = context.packageName
        val marketUri = "market://details?id=$appPackageName".toUri()
        val webUri = "https://play.google.com/store/apps/details?id=$appPackageName".toUri()

        val intent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        }

        runCatching {
            context.startActivity(intent)
        }.onFailure {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
