package com.monospace.battery.purchase

import android.app.Activity
import android.content.pm.ApplicationInfo
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
import com.monospace.battery.data.local.WidgetsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PurchaseManager(
    private val context: Context,
    private val productId: String
) {
    private val isDebuggable =
        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    private fun debug(message: String) {
        if (isDebuggable) Log.d(TAG, message)
    }

    private val scope = MainScope()
    private var productDetails: ProductDetails? = null
    private var billingClient: BillingClient
    private var connecting = false

    private val _isPurchased = MutableStateFlow(WidgetsUtils.isWidgetsPurchased(context))
    val isPurchased: StateFlow<Boolean> = _isPurchased.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseEvent>()
    val purchaseEvents: SharedFlow<PurchaseEvent> = _purchaseEvents.asSharedFlow()

    sealed class PurchaseEvent {
        object Success : PurchaseEvent()
        sealed class Error : PurchaseEvent() {
            object ServiceUnavailable : Error()
            object BillingUnavailable : Error()
            object ItemAlreadyOwned : Error()
            object NetworkError : Error()
            object DeveloperError : Error()
            object ProductUnavailable : Error()
            object NoRestorablePurchases : Error()
            object NoPurchasesFound : Error()
            data class Unknown(val code: Int) : Error()
        }
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage

            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchases?.forEach { handlePurchase(it) }
                }

                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    debug("User cancelled the purchase flow")
                }

                else -> {
                    Log.e(TAG, "Purchases Update Error: Code $responseCode - $debugMessage")
                    emitError(responseCode)
                }
            }
        }

    private fun emitError(responseCode: Int) {
        val errorEvent = when (responseCode) {
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> PurchaseEvent.Error.ServiceUnavailable
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> PurchaseEvent.Error.BillingUnavailable
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> PurchaseEvent.Error.ItemAlreadyOwned
            BillingClient.BillingResponseCode.NETWORK_ERROR -> PurchaseEvent.Error.NetworkError
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> PurchaseEvent.Error.DeveloperError
            else -> PurchaseEvent.Error.Unknown(responseCode)
        }

        scope.launch { _purchaseEvents.emit(errorEvent) }
    }

    private fun handlePurchase(purchase: Purchase, silent: Boolean = false) {
        debug("Handling purchase: State=${purchase.purchaseState}")
        debug("Handling purchase: Products=${purchase.products}")

        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED
            || !purchase.products.contains(productId)
        ) {
            debug("Purchase ignored: State is not PURCHASED or ID mismatch")
            return
        }

        val wasPurchasedLocally = WidgetsUtils.isWidgetsPurchased(context)

        // Update state and persistence
        WidgetsUtils.setWidgetsPurchased(context, true)
        _isPurchased.value = true

        if (!purchase.isAcknowledged) {
            debug("Acknowledging new purchase...")

            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(params)
                }

                if (result.responseCode == BILLING_RESPONSE_OK) {
                    debug("Purchase acknowledged successfully")

                    if (!silent) _purchaseEvents.emit(PurchaseEvent.Success)
                } else {
                    Log.e(TAG, "Acknowledge error: ${result.responseCode}")
                    Log.e(TAG, "Acknowledge error: ${result.debugMessage}")
                }
            }
        } else {
            debug("Purchase already acknowledged")

            if (!silent && !wasPurchasedLocally) {
                scope.launch { _purchaseEvents.emit(PurchaseEvent.Success) }
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

    private fun startBillingConnection(
        onReady: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (connecting) {
            onFailed?.invoke()
            return
        }

        debug("Starting billing connection...")
        connecting = true

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                connecting = false

                if (billingResult.responseCode == BILLING_RESPONSE_OK) {
                    debug("Billing Connection Successful")

                    if (onReady != null) {
                        onReady()
                    } else {
                        scope.launch {
                            getPurchases(silent = true)
                            processProducts()
                        }
                    }
                } else {
                    Log.e(
                        TAG,
                        "Billing Setup Failed: ${billingResult.responseCode} - ${billingResult.debugMessage}"
                    )
                    onFailed?.invoke()
                }
            }

            override fun onBillingServiceDisconnected() {
                connecting = false
                Log.w(TAG, "Billing Service Disconnected. Will reconnect on next interaction.")
            }
        })
    }

    private suspend fun processProducts(): ProductDetails? {
        if (!billingClient.isReady) {
            Log.w(TAG, "processProducts: BillingClient not ready")
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

        val result = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            productDetails = result.productDetailsList?.find { it.productId == productId }
            debug("ProductDetails loaded: ${productDetails?.name ?: "Null"}")
            return productDetails
        } else {
            Log.e(TAG, "queryProductDetails Error: ${result.billingResult.responseCode}")
            Log.e(TAG, "queryProductDetails Error: ${result.billingResult.debugMessage}")
        }

        return null
    }

    fun getPurchases(silent: Boolean = false) {
        if (!billingClient.isReady) {
            Log.w(TAG, "getPurchases: BillingClient not ready, reconnecting")
            startBillingConnection(onReady = { getPurchases(silent) })
            return
        }

        val params =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
        billingClient.queryPurchasesAsync(params.build()) { billingResult, purchaseList ->
            scope.launch {
                val responseCode = billingResult.responseCode
                if (responseCode == BILLING_RESPONSE_OK) {
                    val hasProduct = purchaseList.any {
                        it.products.contains(productId) && it.purchaseState == Purchase.PurchaseState.PURCHASED
                    }

                    debug("Syncing purchases: found active product=$hasProduct")

                    if (hasProduct) {
                        purchaseList.filter {
                            it.products.contains(productId)
                        }.forEach {
                            handlePurchase(it, silent)
                        }
                    } else {
                        if (WidgetsUtils.isWidgetsPurchased(context)) {
                            debug("Revoking access: No active purchase found in Google Play")
                            WidgetsUtils.setWidgetsPurchased(context, false)
                        }

                        _isPurchased.value = false

                        if (!silent) {
                            val errorEvent = if (purchaseList.isEmpty()) {
                                PurchaseEvent.Error.NoRestorablePurchases
                            } else {
                                PurchaseEvent.Error.NoPurchasesFound
                            }
                            _purchaseEvents.emit(errorEvent)
                        }
                    }
                } else {
                    Log.e(TAG, "queryPurchasesAsync failed: $responseCode")
                    Log.e(TAG, "queryPurchasesAsync failed: ${billingResult.debugMessage}")
                }
            }
        }
    }

    fun close() {
        scope.cancel()
        if (billingClient.isReady) billingClient.endConnection()
    }

    fun launchBuyBillingFlow(activity: Activity) {
        if (!billingClient.isReady) {
            startBillingConnection(
                onReady = { launchBuyBillingFlow(activity) },
                onFailed = { emitError(BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE) }
            )
            return
        }

        scope.launch {
            val details = productDetails ?: processProducts()
            details?.let {
                debug("Launching billing flow for: ${it.productId}")
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(it).build()
                        )
                    )
                    .build()
                billingClient.launchBillingFlow(activity, flowParams)
            } ?: run {
                Log.e(TAG, "Cannot launch flow: ProductDetails is null")
                _purchaseEvents.emit(PurchaseEvent.Error.ProductUnavailable)
            }
        }
    }

    companion object {
        private const val TAG = "PurchaseManager"
        const val WIDGETS = "widgets"
        const val BILLING_RESPONSE_OK = BillingClient.BillingResponseCode.OK
    }
}
