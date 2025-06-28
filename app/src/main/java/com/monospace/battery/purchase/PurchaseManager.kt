package com.monospace.battery.purchase

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
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
            if (billingResult.responseCode == BILLING_RESPONSE_OK) {
                purchases?.forEach { purchase ->
                    Log.d("PurchaseManager", "Purchase found: ${purchase.orderId}")

                    val orderId = purchase.orderId ?: return@forEach
                    val sharedPrefs = SharedPreferences(context)

                    if (purchase.products.contains(productId)) {
                        sharedPrefs.setItem(
                            productId,
                            "purchased",
                            true.toString()
                        )
                    }

                    if (sharedPrefs.getItem(WIDGETS, orderId) != null) {
                        Log.d("PurchaseManager", "Purchase already exists: $orderId")
                        return@forEach
                    }

                    sharedPrefs.setItem(
                        WIDGETS,
                        orderId,
                        purchase.purchaseToken
                    )
                }

                // Call the success callback
                onPurchaseSuccess?.invoke()
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
                    if (billingResult.responseCode == BILLING_RESPONSE_OK) {
                        MainScope().launch {
                            getPurchases()
                            processProducts()
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    startBillingConnection()
                }
            }
        )
    }

    private suspend fun processProducts(): ProductDetails? {
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

        if (productDetailsResult.billingResult.responseCode == BILLING_RESPONSE_OK) {
            productDetails = productDetailsResult.productDetailsList?.find {
                it.productId == productId
            }

            return productDetailsResult.productDetailsList?.find {
                it.productId == productId
            }
        }

        return null
    }

    private fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build(),
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        // Launch the billing flow
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
                    // Save if purchases contain the productId
                    if (purchase.products.contains(productId)) {
                        sharedPrefs.setItem(
                            productId,
                            "purchased",
                            true.toString()
                        )
                    }

                    Log.d("PurchaseManager", "Purchase found: ${purchase.orderId}")

                    val orderId = purchase.orderId ?: return@forEach

                    if (sharedPrefs.getItem(WIDGETS, orderId) != null) {
                        Log.d("PurchaseManager", "Purchase already exists: $orderId")
                        return@forEach
                    }

                    // Save purchase details to preferences
                    sharedPrefs.setItem(
                        WIDGETS,
                        orderId,
                        purchase.purchaseToken
                    )
                }
            }
        }
    }

    fun restorePurchases() {
        getPurchases()
    }

    fun launchBuyBillingFlow(activity: Activity) {
        if (productDetails == null) {
            Log.d("PurchaseManager", "Product details not available, fetching...")

            MainScope().launch {
                val details = processProducts()

                if (details != null) {
                    launchPurchaseFlow(activity, details)
                } else {
                    Log.e("PurchaseManager", "Failed to fetch product details")
                }
            }
        } else {
            launchPurchaseFlow(activity, productDetails!!)
        }
    }

    companion object {
        const val WIDGETS = "widgets"
        const val BILLING_RESPONSE_OK = BillingClient.BillingResponseCode.OK
    }
}