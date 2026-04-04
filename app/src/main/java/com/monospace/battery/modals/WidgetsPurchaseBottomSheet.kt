package com.monospace.battery.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.monospace.battery.R
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.purchase.PurchaseManager
import com.monospace.battery.ui.components.WidgetsPurchaseContent
import com.monospace.battery.ui.theme.BatteryTheme

class WidgetsPurchaseBottomSheet(
    private val onPurchaseSuccess: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val purchaseManager = PurchaseManager(
            requireContext(),
            PurchaseManager.WIDGETS,
            onPurchaseSuccess = {
                onPurchaseSuccess?.invoke()
                dismiss()
            }
        )

        return ComposeView(requireContext()).apply {
            setContent {
                BatteryTheme {
                    WidgetsPurchaseContent(
                        onBuyClick = {
                            purchaseManager.launchBuyBillingFlow(requireActivity())
                        },
                        onRestoreClick = {
                            purchaseManager.restorePurchases()

                            if (WidgetsUtils.isWidgetsPurchased(requireContext())) {
                                onPurchaseSuccess?.invoke()
                                dismiss()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.widget_purchase_no_purchases_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "WidgetsPurchaseBottomSheet"
    }
}