package com.monospace.battery.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.monospace.battery.R
import com.monospace.battery.databinding.WidgetsPurchaseBottomSheetBinding
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.purchase.PurchaseManager

class WidgetsPurchaseBottomSheet(
    private val onPurchaseSuccess: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var _binding: WidgetsPurchaseBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WidgetsPurchaseBottomSheetBinding.inflate(inflater, container, false)

        val purchaseManager = PurchaseManager(
            requireContext(),
            PurchaseManager.WIDGETS,
            onPurchaseSuccess = {
                onPurchaseSuccess?.invoke()
                dismiss()
            }
        )

        binding.buyButton.setOnClickListener {
            purchaseManager.launchBuyBillingFlow(requireActivity())
        }

        binding.restoreButton.setOnClickListener {
            purchaseManager.restorePurchases()

            if (WidgetsUtils.isWidgetsPurchased(requireContext())) {
                onPurchaseSuccess?.invoke()
                dismiss()
            } else {
                binding.restoreButton.isEnabled = false
                Toast.makeText(
                    requireContext(),
                    getString(R.string.widget_purchase_no_purchases_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    companion object {
        const val TAG = "WidgetsPurchaseBottomSheet"
    }
}