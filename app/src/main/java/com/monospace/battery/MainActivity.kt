package com.monospace.battery

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.monospace.battery.core.utils.AppUtils
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.notifications.PermissionManager
import com.monospace.battery.purchase.PurchaseManager
import com.monospace.battery.service.alerts.BatteryAlertService
import com.monospace.battery.ui.components.AppNavHost
import com.monospace.battery.ui.components.BottomNavigation
import com.monospace.battery.ui.components.Screen
import com.monospace.battery.ui.components.TopAppBar
import com.monospace.battery.ui.theme.BatteryTheme
import com.monospace.battery.ui.viewmodels.MainViewModel
import com.monospace.battery.ui.viewmodels.SettingsViewModel
import com.monospace.battery.widgets.charging.BatteryLevelWidget
import com.monospace.battery.widgets.charging.BatteryLevelWidgetReceiver
import com.monospace.battery.widgets.charging.ChargingInfoWidget
import com.monospace.battery.widgets.charging.ChargingInfoWidgetReceiver
import com.monospace.battery.widgets.cycles.ChargeCyclesWidget
import com.monospace.battery.widgets.cycles.ChargeCyclesWidgetReceiver
import com.monospace.battery.widgets.health.HealthStatusWidget
import com.monospace.battery.widgets.health.HealthStatusWidgetReceiver
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val permissionManager by lazy { PermissionManager(this) }
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

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

    private fun ensureNotificationPermission(enabled: Boolean) {
        if (enabled && !permissionManager.hasNotificationPermission()) {
            permissionManager.requestNotificationPermission(this, requestPermissionLauncher)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupWidgetPreviews()
        settingsViewModel.updateVersionName(AppUtils.getVersionName(this))

        setContent {
            BatteryTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val context = LocalContext.current

                val purchaseManager = remember { PurchaseManager(context, PurchaseManager.WIDGETS) }
                val isPurchased by purchaseManager.isPurchased.collectAsState()
                val settingsState by settingsViewModel.uiState
                val batteryState by mainViewModel.batteryState

                var purchaseError by remember {
                    mutableStateOf<PurchaseManager.PurchaseEvent.Error?>(null)
                }

                val settingsActions = remember {
                    SettingsUiActions(
                        onHealthyChargeChange = {
                            settingsViewModel.toggleHealthyCharge(it)
                            ensureNotificationPermission(it)
                            updateMonitoringService()
                        },
                        onTempAlertChange = {
                            settingsViewModel.toggleTempAlert(it)
                            ensureNotificationPermission(it)
                            updateMonitoringService()
                        },
                        onLowBatteryChange = {
                            settingsViewModel.toggleLowBattery(it)
                            ensureNotificationPermission(it)
                            updateMonitoringService()
                        },
                        onFastDischargeChange = {
                            settingsViewModel.toggleFastDischarge(it)
                            ensureNotificationPermission(it)
                            updateMonitoringService()
                        },
                        onSlowChargeChange = {
                            settingsViewModel.toggleSlowCharge(it)
                            ensureNotificationPermission(it)
                            updateMonitoringService()
                        },
                        onHealthyChargeLevelChange = settingsViewModel::setHealthyChargeLevel,
                        onLowBatteryLevelChange = settingsViewModel::setLowBatteryLevel,
                        onAlwaysOnDisplayChange = {
                            settingsViewModel.toggleAod(it)
                            ensureNotificationPermission(it)
                            updateMonitoringService()
                        },
                        onActiveMonitoringChange = {
                            settingsViewModel.toggleActiveMonitoring(it)
                            ensureNotificationPermission(it)
                            updateMonitoringService()
                        },
                        onAodClockStyleChange = settingsViewModel::setAodClockStyle,
                        onAodMeterStyleChange = settingsViewModel::setAodMeterStyle,
                        onAodColorChange = settingsViewModel::setAodColor,
                        onAodShowDateChange = settingsViewModel::toggleAodShowDate,
                        onAodShowClockChange = settingsViewModel::toggleAodShowClock,
                        onAod24hFormatChange = settingsViewModel::toggleAod24h,
                        onAodFontSizeClockChange = settingsViewModel::setAodFontSizeClock,
                        onAodFontSizeDateChange = settingsViewModel::setAodFontSizeDate,
                        onAodDimAmountChange = settingsViewModel::setAodDimAmount,
                        onAodShowShortcutsChange = settingsViewModel::toggleAodShowShortcuts,
                        onOverlayPermissionRequest = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
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
        val shouldRun = state.anyAlertEnabled || state.activeMonitoringEnabled
        val intent = Intent(this, BatteryAlertService::class.java)

        if (shouldRun) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
        } else {
            stopService(intent)
        }
    }

    private fun setupWidgetPreviews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            lifecycleScope.launch {
                val manager = GlanceAppWidgetManager(this@MainActivity)

                listOf(
                    ChargingInfoWidgetReceiver::class,
                    BatteryLevelWidgetReceiver::class,
                    HealthStatusWidgetReceiver::class,
                    ChargeCyclesWidgetReceiver::class
                ).forEach {
                    runCatching { manager.setWidgetPreviews(it) }
                }
            }
        }
    }

    private fun updateAllWidgets() {
        lifecycleScope.launch {
            runCatching {
                ChargingInfoWidget().updateAll(applicationContext)
                ChargeCyclesWidget().updateAll(applicationContext)
                HealthStatusWidget().updateAll(applicationContext)
                BatteryLevelWidget().updateAll(applicationContext)
            }
        }
    }
}
