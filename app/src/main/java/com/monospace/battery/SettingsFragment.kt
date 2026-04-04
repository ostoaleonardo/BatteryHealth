package com.monospace.battery

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.monospace.battery.modals.WidgetsPurchaseBottomSheet
import com.monospace.battery.ui.screens.SettingsScreen
import com.monospace.battery.ui.theme.BatteryTheme

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                BatteryTheme {
                    SettingsScreen(
                        onUnlockClick = { showPurchaseBottomSheet() },
                        onUpdateClick = { openGooglePlay() },
                        onRateClick = { openGooglePlay() }
                    )
                }
            }
        }
    }

    private fun showPurchaseBottomSheet() {
        val bottomSheet = WidgetsPurchaseBottomSheet(
            onPurchaseSuccess = {
                // Refresh logic if needed
            }
        )
        bottomSheet.show(childFragmentManager, WidgetsPurchaseBottomSheet.TAG)
    }

    private fun openGooglePlay() {
        val appPackageName = requireContext().packageName
        val marketUri = "market://details?id=$appPackageName".toUri()
        val googlePlayUri = "https://play.google.com/store/apps/details?id=$appPackageName".toUri()

        try {
            requireContext().startActivity(
                Intent(Intent.ACTION_VIEW, marketUri)
            )
        } catch (_: ActivityNotFoundException) {
            requireContext().startActivity(
                Intent(Intent.ACTION_VIEW, googlePlayUri)
            )
        }
    }
}
