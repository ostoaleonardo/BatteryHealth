package com.monospace.battery

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.constants.WidgetConstants
import com.monospace.battery.core.utils.AppUtils
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.notifications.PermissionManager
import com.monospace.battery.purchase.PurchaseManager
import com.monospace.battery.purchase.PurchaseViewModel
import com.monospace.battery.service.alerts.BatteryAlertService
import com.monospace.battery.ui.components.navigation.AppNavHost
import com.monospace.battery.ui.components.navigation.BottomNavigation
import com.monospace.battery.ui.components.navigation.Screen
import com.monospace.battery.ui.components.navigation.TopAppBar
import com.monospace.battery.ui.theme.BatteryTheme
import com.monospace.battery.ui.viewmodels.MainViewModel
import com.monospace.battery.ui.viewmodels.SettingsViewModel
import com.monospace.battery.widgets.WidgetsUpdater
import com.monospace.battery.widgets.charging.BatteryLevelWidgetReceiver
import com.monospace.battery.widgets.cycles.ChargeCyclesWidgetReceiver
import com.monospace.battery.widgets.pager.BatteryPagerWidgetReceiver
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val permissionManager by lazy { PermissionManager(this) }
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val purchaseViewModel: PurchaseViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        updatePermissionStates()
        if (isGranted) updateMonitoringService()
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updatePermissionStates()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStates()
    }

    private fun updatePermissionStates() {
        settingsViewModel.updatePermissionState(
            notifications = permissionManager.hasNotificationPermission(),
            overlay = Settings.canDrawOverlays(this)
        )
    }

    private fun alertToggle(toggle: (Boolean) -> Unit): (Boolean) -> Unit = { enabled ->
        toggle(enabled)
        ensureNotificationPermission(enabled)
        updateMonitoringService()
    }

    private fun ensureNotificationPermission(enabled: Boolean) {
        if (enabled && !permissionManager.hasNotificationPermission()) {
            permissionManager.requestNotificationPermission(this, requestPermissionLauncher)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupWidgetPreviews()
        settingsViewModel.updateVersionName(AppUtils.getVersionName(this))

        setContent {
            BatteryTheme(headingFont = settingsViewModel.uiState.value.headingFont) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val context = LocalContext.current

                val purchaseManager = purchaseViewModel.manager
                val isPurchased by purchaseManager.isPurchased.collectAsState()
                val settingsState by settingsViewModel.uiState
                val batteryState by mainViewModel.batteryState

                var purchaseError by remember {
                    mutableStateOf<PurchaseManager.PurchaseEvent.Error?>(null)
                }

                val settingsActions = remember {
                    SettingsUiActions(
                        onHealthyChargeChange = alertToggle(settingsViewModel::toggleHealthyCharge),
                        onTempAlertChange = alertToggle(settingsViewModel::toggleTempAlert),
                        onLowBatteryChange = alertToggle(settingsViewModel::toggleLowBattery),
                        onFastDischargeChange = alertToggle(settingsViewModel::toggleFastDischarge),
                        onSlowChargeChange = alertToggle(settingsViewModel::toggleSlowCharge),
                        onHealthyChargeLevelChange = settingsViewModel::setHealthyChargeLevel,
                        onLowBatteryLevelChange = settingsViewModel::setLowBatteryLevel,
                        onAlwaysOnDisplayChange = alertToggle(settingsViewModel::toggleAod),
                        onActiveMonitoringChange = alertToggle(settingsViewModel::toggleActiveMonitoring),
                        onAodMeterStyleChange = settingsViewModel::setAodMeterStyle,
                        onAodColorChange = settingsViewModel::setAodColor,
                        onAodShowDateChange = settingsViewModel::toggleAodShowDate,
                        onAodShowClockChange = settingsViewModel::toggleAodShowClock,
                        onAod24hFormatChange = settingsViewModel::toggleAod24h,
                        onAodFontSizeClockChange = settingsViewModel::setAodFontSizeClock,
                        onAodFontSizeDateChange = settingsViewModel::setAodFontSizeDate,
                        onAodDimAmountChange = settingsViewModel::setAodDimAmount,
                        onAodShowShortcutsChange = settingsViewModel::toggleAodShowShortcuts,
                        onHeadingFontChange = settingsViewModel::setHeadingFont,
                        onWidgetGlassChange = settingsViewModel::toggleWidgetGlass,
                        onWidgetGlassAlphaChange = settingsViewModel::setWidgetGlassAlpha,
                        onOverlayPermissionRequest = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                "package:$packageName".toUri()
                            )

                            overlayPermissionLauncher.launch(intent)
                        },
                        onUnlockClick = { purchaseManager.launchBuyBillingFlow(this@MainActivity) },
                        onNotificationPermissionRequest = {
                            permissionManager.requestNotificationPermission(
                                this@MainActivity,
                                requestPermissionLauncher,
                                forceSettings = true
                            )
                        },
                        onUpdateClick = { AppUtils.openPlayStore(context) },
                        onRateClick = { AppUtils.openPlayStore(context) },
                        onDeleteHistoryClick = settingsViewModel::clearAllData
                    )
                }

                LaunchedEffect(isPurchased) {
                    settingsViewModel.updatePurchaseState(isPurchased)
                    updateMonitoringService()
                }

                LaunchedEffect(Unit) {
                    purchaseManager.purchaseEvents.collect { event ->
                        if (event is PurchaseManager.PurchaseEvent.Error) purchaseError = event
                    }
                }

                purchaseError?.let { error ->
                    val message = getPurchaseErrorMessage(error)

                    LaunchedEffect(error) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        purchaseError = null
                    }
                }

                CompositionLocalProvider(
                    LocalSettingsState provides settingsState,
                    LocalSettingsActions provides settingsActions,
                    LocalBatteryState provides batteryState
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                currentRoute,
                                { navController.navigate(Screen.Settings.route) },
                                { navController.popBackStack() }
                            )
                        },
                        bottomBar = {
                            if (currentRoute != Screen.Settings.route) {
                                BottomNavigation(currentRoute, navController)
                            }
                        }
                    ) { innerPadding ->
                        AppNavHost(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        updateAllWidgets()
    }

    private fun getPurchaseErrorMessage(event: PurchaseManager.PurchaseEvent.Error): String {
        return when (event) {
            is PurchaseManager.PurchaseEvent.Error.ServiceUnavailable -> getString(R.string.widget_purchase_error_service_unavailable)
            is PurchaseManager.PurchaseEvent.Error.BillingUnavailable -> getString(R.string.widget_purchase_error_billing_unavailable)
            is PurchaseManager.PurchaseEvent.Error.ItemAlreadyOwned -> getString(R.string.widget_purchase_error_item_already_owned)
            is PurchaseManager.PurchaseEvent.Error.NetworkError -> getString(R.string.widget_purchase_error_network)
            is PurchaseManager.PurchaseEvent.Error.DeveloperError -> getString(R.string.widget_purchase_error_developer)
            is PurchaseManager.PurchaseEvent.Error.ProductUnavailable -> getString(R.string.widget_purchase_error_product_unavailable)
            is PurchaseManager.PurchaseEvent.Error.NoRestorablePurchases -> getString(R.string.widget_purchase_no_restorable_purchases)
            is PurchaseManager.PurchaseEvent.Error.NoPurchasesFound -> getString(R.string.widget_purchase_no_purchases_found)
            is PurchaseManager.PurchaseEvent.Error.Unknown -> getString(
                R.string.widget_purchase_error_unknown, event.code
            )
        }
    }

    private fun updateMonitoringService() {
        val state = settingsViewModel.uiState.value
        val shouldRun = (state.anyAlertEnabled && state.isWidgetsPurchased) || state.activeMonitoringEnabled

        runCatching {
            if (shouldRun) BatteryAlertService.start(this) else BatteryAlertService.stop(this)
        }.onFailure {
            Log.w("MainActivity", "Could not update monitoring service", it)
            runCatching { FirebaseCrashlytics.getInstance().recordException(it) }
        }
    }

    private fun setupWidgetPreviews() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM || !shouldRefreshPreviews()) return

        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@MainActivity)

            listOf(
                BatteryLevelWidgetReceiver::class,
                ChargeCyclesWidgetReceiver::class,
                BatteryPagerWidgetReceiver::class
            ).forEach {
                runCatching { manager.setWidgetPreviews(it) }
                    .onFailure { e -> Log.w("MainActivity", "Widget preview failed", e) }
            }
        }
    }

    private fun shouldRefreshPreviews(): Boolean {
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val prefs = PreferenceManager(this)
        val version = AppUtils.getVersionName(this)
        val now = System.currentTimeMillis()

        val lastVersion = prefs.get(Constants.PREFS_ALERTS, Constants.KEY_PREVIEW_VERSION, "")
        val lastTime = prefs.get(Constants.PREFS_ALERTS, Constants.KEY_PREVIEW_TIME, 0L)
        val expired = now - lastTime >= WidgetConstants.PREVIEW_REFRESH_INTERVAL.inWholeMilliseconds

        if (!isDebuggable && lastVersion == version && !expired) return false

        prefs.set(Constants.PREFS_ALERTS, Constants.KEY_PREVIEW_VERSION, version)
        prefs.set(Constants.PREFS_ALERTS, Constants.KEY_PREVIEW_TIME, now)
        return true
    }

    private fun updateAllWidgets() {
        lifecycleScope.launch {
            WidgetsUpdater.updateAll(applicationContext)
        }
    }
}
