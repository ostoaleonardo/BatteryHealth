package com.monospace.battery.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.monospace.battery.databinding.CompleteWidgetsPurchaseBottomSheetBinding
import com.monospace.battery.purchase.PurchaseManager

class CompleteWidgetsPurchaseBottomSheet : BottomSheetDialogFragment() {

    private var _binding: CompleteWidgetsPurchaseBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var purchaseManager: PurchaseManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CompleteWidgetsPurchaseBottomSheetBinding.inflate(inflater, container, false)

        purchaseManager = PurchaseManager(requireContext(), PurchaseManager.WIDGETS)

        return binding.root
    }

    companion object {
        const val TAG = "CompleteWidgetsPurchaseBottomSheet"
    }
}