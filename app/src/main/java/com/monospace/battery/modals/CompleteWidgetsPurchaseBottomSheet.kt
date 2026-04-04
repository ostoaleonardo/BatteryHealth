package com.monospace.battery.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.monospace.battery.ui.components.CompleteWidgetsPurchaseContent
import com.monospace.battery.ui.theme.BatteryTheme

class CompleteWidgetsPurchaseBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                BatteryTheme {
                    CompleteWidgetsPurchaseContent()
                }
            }
        }
    }

    companion object {
        const val TAG = "CompleteWidgetsPurchaseBottomSheet"
    }
}