package com.monospace.battery.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.monospace.battery.databinding.WidgetsPurchaseBottomSheetBinding
import com.monospace.battery.purchase.PurchaseManager

class WidgetsPurchaseBottomSheet(
    private val onPurchaseSuccess: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var _binding: WidgetsPurchaseBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var purchaseManager: PurchaseManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WidgetsPurchaseBottomSheetBinding.inflate(inflater, container, false)

        purchaseManager = PurchaseManager(
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
            purchaseManager.launchBuyBillingFlow(requireActivity())
        }

        return binding.root
    }

    companion object {
        const val TAG = "WidgetsPurchaseBottomSheet"
    }
}