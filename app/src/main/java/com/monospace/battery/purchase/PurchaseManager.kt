package com.monospace.battery.purchase

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.monospace.battery.helpers.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PurchaseManager(
    private val context: Context,
    private val productId: String,
    private val onPurchaseSuccess: (() -> Unit)? = null
) {

    private var productDetails: ProductDetails? = null
    private var billingClient: BillingClient

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (purchases != null) {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                        }
                    }
                }

                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Log.d(TAG, "User cancelled the purchase flow")
                }

                else -> {
                    Log.e(TAG, "Billing Result Error: $billingResult")
                }
            }
        }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val sharedPrefs = SharedPreferences(context)

            // 1. Save locally
            if (purchase.products.contains(productId)) {
                sharedPrefs.setItem(productId, "purchased", "true")
            }

            // 2. Acknowledge the purchase
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                MainScope().launch {
                    val result = withContext(Dispatchers.IO) {
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams)
                    }
                    if (result.responseCode == BILLING_RESPONSE_OK) {
                        Log.d(TAG, "Purchase acknowledged successfully")
                        onPurchaseSuccess?.invoke()
                    }
                }
            } else {
                onPurchaseSuccess?.invoke()
            }
        }
    }

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()
        startBillingConnection()
    }

    private fun startBillingConnection() {
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    val responseCode = billingResult.responseCode
                    val debugMessage = billingResult.debugMessage

                    if (responseCode == BILLING_RESPONSE_OK) {
                        Log.d(TAG, "Billing Setup Success")
                        MainScope().launch {
                            getPurchases()
                            processProducts()
                        }
                    } else {
                        Log.e(TAG, "Billing Setup Error: $responseCode - $debugMessage")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Log.w(TAG, "Billing Service Disconnected. Retrying...")
                    startBillingConnection()
                }
            }
        )
    }

    private suspend fun processProducts(): ProductDetails? {
        if (!billingClient.isReady) {
            Log.e(TAG, "processProducts: BillingClient is not ready")
            return null
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val details =
                productDetailsResult.productDetailsList?.find { it.productId == productId }

            if (details != null) {
                productDetails = details
                Log.d(TAG, "Product found: $productId")
                return details
            }
        }

        Log.e(TAG, "Product not found: $productId")
        return null
    }

    private fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    fun getPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)

        billingClient.queryPurchasesAsync(
            params.build()
        ) { billingResult, purchaseList ->
            if (billingResult.responseCode == BILLING_RESPONSE_OK) {
                val sharedPrefs = SharedPreferences(context)

                if (purchaseList.isEmpty()) {
                    sharedPrefs.setItem(
                        productId,
                        "purchased",
                        false.toString()
                    )

                    return@queryPurchasesAsync
                }

                purchaseList.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
        }
    }

    fun restorePurchases() {
        getPurchases()
    }

    fun launchBuyBillingFlow(activity: Activity) {
        if (productDetails == null) {
            Log.d(TAG, "Product details not available, fetching...")

            MainScope().launch {
                val details = processProducts()

                if (details != null) {
                    launchPurchaseFlow(activity, details)
                } else {
                    Log.e(TAG, "Failed to fetch product details")
                }
            }
        } else {
            launchPurchaseFlow(activity, productDetails!!)
        }
    }

    companion object {
        private const val TAG = "PurchaseManager"
        const val WIDGETS = "widgets"
        const val BILLING_RESPONSE_OK = BillingClient.BillingResponseCode.OK
    }
}