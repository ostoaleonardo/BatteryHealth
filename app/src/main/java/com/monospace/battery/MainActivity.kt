package com.monospace.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.AppUtils
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.models.BatteryInfo
import com.monospace.battery.data.models.BatteryState
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.data.models.SettingsUiState
import com.monospace.battery.notifications.PermissionManager
import com.monospace.battery.purchase.PurchaseManager
import com.monospace.battery.service.alerts.BatteryAlertService
import com.monospace.battery.ui.components.BottomNavigation
import com.monospace.battery.ui.components.Screen
import com.monospace.battery.ui.components.TopAppBar
import com.monospace.battery.ui.screens.AlertsScreen
import com.monospace.battery.ui.screens.AodScreen
import com.monospace.battery.ui.screens.HistoryScreen
import com.monospace.battery.ui.screens.MainScreen
import com.monospace.battery.ui.screens.SettingsScreen
import com.monospace.battery.ui.theme.BatteryTheme
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

    private var batteryState by mutableStateOf(BatteryState())
    private var hasNotificationPermission by mutableStateOf(false)
    private var hasOverlayPermission by mutableStateOf(false)
    private val batteryUtils by lazy { BatteryUtils(this) }
    private val permissionManager by lazy { PermissionManager(this) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasNotificationPermission = isGranted

        if (isGranted) {
            updateMonitoringService()
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updatePermissionState()
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryState(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionState()
    }

    private fun updatePermissionState() {
        hasNotificationPermission = permissionManager.hasNotificationPermission()
        hasOverlayPermission = Settings.canDrawOverlays(this)
    }

    private fun ensureNotificationPermission(enabled: Boolean) {
        if (enabled && !hasNotificationPermission) {
            permissionManager.requestNotificationPermission(
                this,
                requestPermissionLauncher
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        updatePermissionState()
        setupWidgetPreviews()

        setContent {
            BatteryTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val context = LocalContext.current

                val purchaseManager = remember {
                    PurchaseManager(context, PurchaseManager.WIDGETS)
                }

                val isPurchased by purchaseManager.isPurchased.collectAsState()

                val prefs = remember { PreferenceManager(context) }

                var purchaseError by remember {
                    mutableStateOf<PurchaseManager.PurchaseEvent.Error?>(null)
                }

                val healthyChargeEnabledState = remember {
                    mutableStateOf(
                        prefs.getBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_HEALTHY_CHARGE_ENABLED
                        )
                    )
                }
                val tempAlertEnabledState = remember {
                    mutableStateOf(
                        prefs.getBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_TEMP_ALERT_ENABLED
                        )
                    )
                }
                val healthyChargeLevelState = remember {
                    mutableIntStateOf(
                        prefs.getInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_HEALTHY_CHARGE_LEVEL,
                            80
                        )
                    )
                }
                val lowBatteryEnabledState = remember {
                    mutableStateOf(
                        prefs.getBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_LOW_BATTERY_ENABLED
                        )
                    )
                }
                val lowBatteryLevelState = remember {
                    mutableIntStateOf(
                        prefs.getInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_LOW_BATTERY_LEVEL,
                            20
                        )
                    )
                }
                val fastDischargeEnabledState = remember {
                    mutableStateOf(
                        prefs.getBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_FAST_DISCHARGE_ENABLED
                        )
                    )
                }
                val slowChargeEnabledState = remember {
                    mutableStateOf(
                        prefs.getBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_SLOW_CHARGE_ENABLED
                        )
                    )
                }
                val aodEnabledState = remember {
                    mutableStateOf(
                        prefs.getBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_ENABLED
                        )
                    )
                }
                val aodClockStyleState = remember {
                    mutableIntStateOf(
                        prefs.getInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_CLOCK_STYLE,
                            0
                        )
                    )
                }
                val aodMeterStyleState = remember {
                    mutableIntStateOf(
                        prefs.getInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_METER_STYLE,
                            0
                        )
                    )
                }
                val aodColorState = remember {
                    mutableLongStateOf(
                        prefs.getLong(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_COLOR,
                            0xFF00A25B
                        )
                    )
                }
                val aodShowDateState = remember {
                    mutableStateOf(
                        prefs.getBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_SHOW_DATE,
                            true
                        )
                    )
                }
                val aodShowClockState = remember {
                    mutableStateOf(
                        prefs.getBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_SHOW_CLOCK,
                            true
                        )
                    )
                }
                val aod24hFormatState = remember {
                    mutableStateOf(
                        prefs.getBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_24H_FORMAT,
                            true
                        )
                    )
                }
                val aodFontSizeClockState = remember {
                    mutableIntStateOf(
                        prefs.getInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_FONT_SIZE_CLOCK,
                            80
                        )
                    )
                }
                val aodFontSizeDateState = remember {
                    mutableIntStateOf(
                        prefs.getInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_FONT_SIZE_DATE,
                            14
                        )
                    )
                }
                val aodShowShortcutsState = remember {
                    mutableStateOf(
                        prefs.getBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_SHOW_SHORTCUTS,
                            false
                        )
                    )
                }
                val aodDimAmountState = remember {
                    mutableIntStateOf(
                        prefs.getInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_DIM_AMOUNT,
                            0
                        )
                    )
                }

                val settingsState = SettingsUiState(
                    isWidgetsPurchased = isPurchased,
                    hasNotificationPermission = hasNotificationPermission,
                    versionName = remember { AppUtils.getVersionName(context) },
                    healthyChargeEnabled = healthyChargeEnabledState.value,
                    tempAlertEnabled = tempAlertEnabledState.value,
                    lowBatteryEnabled = lowBatteryEnabledState.value,
                    fastDischargeEnabled = fastDischargeEnabledState.value,
                    slowChargeEnabled = slowChargeEnabledState.value,
                    healthyChargeLevel = healthyChargeLevelState.intValue,
                    lowBatteryLevel = lowBatteryLevelState.intValue,
                    alwaysOnDisplayEnabled = aodEnabledState.value,
                    hasOverlayPermission = hasOverlayPermission,
                    aodClockStyle = aodClockStyleState.intValue,
                    aodMeterStyle = aodMeterStyleState.intValue,
                    aodColor = aodColorState.longValue,
                    aodShowDate = aodShowDateState.value,
                    aodShowClock = aodShowClockState.value,
                    aod24hFormat = aod24hFormatState.value,
                    aodFontSizeClock = aodFontSizeClockState.intValue,
                    aodFontSizeDate = aodFontSizeDateState.intValue,
                    aodDimAmount = aodDimAmountState.intValue,
                    aodShowShortcuts = aodShowShortcutsState.value
                )

                val settingsActions = SettingsUiActions(
                    onHealthyChargeChange = { enabled ->
                        ensureNotificationPermission(enabled)
                        healthyChargeEnabledState.value = enabled
                        prefs.setBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_HEALTHY_CHARGE_ENABLED,
                            enabled
                        )
                        updateMonitoringService()
                    },
                    onTempAlertChange = { enabled ->
                        ensureNotificationPermission(enabled)
                        tempAlertEnabledState.value = enabled
                        prefs.setBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_TEMP_ALERT_ENABLED,
                            enabled
                        )
                        updateMonitoringService()
                    },
                    onLowBatteryChange = { enabled ->
                        ensureNotificationPermission(enabled)
                        lowBatteryEnabledState.value = enabled
                        prefs.setBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_LOW_BATTERY_ENABLED,
                            enabled
                        )
                        updateMonitoringService()
                    },
                    onFastDischargeChange = { enabled ->
                        ensureNotificationPermission(enabled)
                        fastDischargeEnabledState.value = enabled
                        prefs.setBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_FAST_DISCHARGE_ENABLED,
                            enabled
                        )
                        updateMonitoringService()
                    },
                    onSlowChargeChange = { enabled ->
                        ensureNotificationPermission(enabled)
                        slowChargeEnabledState.value = enabled
                        prefs.setBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_SLOW_CHARGE_ENABLED,
                            enabled
                        )
                        updateMonitoringService()
                    },
                    onHealthyChargeLevelChange = {
                        healthyChargeLevelState.intValue = it
                        prefs.setInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_HEALTHY_CHARGE_LEVEL,
                            it
                        )
                    },
                    onLowBatteryLevelChange = {
                        lowBatteryLevelState.intValue = it
                        prefs.setInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_LOW_BATTERY_LEVEL,
                            it
                        )
                    },
                    onAlwaysOnDisplayChange = { enabled ->
                        ensureNotificationPermission(enabled)
                        aodEnabledState.value = enabled
                        prefs.setBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_ENABLED,
                            enabled
                        )
                        updateMonitoringService()
                    },
                    onAodClockStyleChange = {
                        aodClockStyleState.intValue = it
                        prefs.setInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_CLOCK_STYLE,
                            it
                        )
                    },
                    onAodMeterStyleChange = {
                        aodMeterStyleState.intValue = it
                        prefs.setInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_METER_STYLE,
                            it
                        )
                    },
                    onAodColorChange = {
                        aodColorState.longValue = it
                        prefs.setLong(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_COLOR, it
                        )
                    },
                    onAodShowDateChange = {
                        aodShowDateState.value = it
                        prefs.setBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_SHOW_DATE,
                            it
                        )
                    },
                    onAodShowClockChange = {
                        aodShowClockState.value = it
                        prefs.setBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_SHOW_CLOCK,
                            it
                        )
                    },
                    onAod24hFormatChange = {
                        aod24hFormatState.value = it
                        prefs.setBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_24H_FORMAT,
                            it
                        )
                    },
                    onAodFontSizeClockChange = {
                        aodFontSizeClockState.intValue = it
                        prefs.setInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_FONT_SIZE_CLOCK,
                            it
                        )
                    },
                    onAodFontSizeDateChange = {
                        aodFontSizeDateState.intValue = it
                        prefs.setInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_FONT_SIZE_DATE,
                            it
                        )
                    },
                    onAodDimAmountChange = {
                        aodDimAmountState.intValue = it
                        prefs.setInt(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_DIM_AMOUNT,
                            it
                        )
                    },
                    onAodShowShortcutsChange = {
                        aodShowShortcutsState.value = it
                        prefs.setBoolean(
                            Constants.PREFS_ALERTS,
                            Constants.KEY_AOD_SHOW_SHORTCUTS,
                            it
                        )
                    },
                    onOverlayPermissionRequest = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:$packageName".toUri()
                        )
                        overlayPermissionLauncher.launch(intent)
                    },
                    onUnlockClick = {
                        purchaseManager.launchBuyBillingFlow(this@MainActivity)
                    },
                    onNotificationPermissionRequest = {
                        permissionManager.requestNotificationPermission(
                            this@MainActivity,
                            requestPermissionLauncher,
                            forceSettings = true // Manual click, can go to settings
                        )
                    },
                    onUpdateClick = { AppUtils.openPlayStore(context) },
                    onRateClick = { AppUtils.openPlayStore(context) }
                )

                LaunchedEffect(isPurchased) {
                    updateMonitoringService()
                }

                LaunchedEffect(Unit) {
                    purchaseManager.purchaseEvents.collect { event ->
                        when (event) {
                            is PurchaseManager.PurchaseEvent.Success -> {}

                            is PurchaseManager.PurchaseEvent.Error -> {
                                purchaseError = event
                            }
                        }
                    }
                }

                purchaseError?.let { error ->
                    val message = getPurchaseErrorMessage(error)

                    LaunchedEffect(error) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        purchaseError = null
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            currentRoute = currentRoute,
                            onSettingsClick = { navController.navigate(Screen.Settings.route) },
                            onBackClick = { navController.popBackStack() }
                        )
                    },
                    bottomBar = {
                        if (currentRoute != Screen.Settings.route) {
                            BottomNavigation(
                                currentRoute = currentRoute,
                                navController = navController
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            MainScreen(state = batteryState)
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(
                                isPremium = isPurchased,
                                currentLevel = batteryState.level,
                                onUpgradeClick = {
                                    purchaseManager.launchBuyBillingFlow(this@MainActivity)
                                }
                            )
                        }
                        composable(Screen.Alerts.route) {
                            AlertsScreen(state = settingsState, actions = settingsActions)
                        }
                        composable(Screen.Aod.route) {
                            AodScreen(
                                state = settingsState,
                                actions = settingsActions,
                                currentBatteryLevel = batteryState.level
                            )
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onUnlockClick = {
                                    purchaseManager.launchBuyBillingFlow(this@MainActivity)
                                }
                            )
                        }
                    }
                }
            }
        }

        runCatching {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val stickyIntent = registerReceiver(batteryReceiver, intentFilter)
            updateBatteryState(stickyIntent)
        }.onFailure { e ->
            Log.e(TAG, "Error registering battery receiver", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        runCatching {
            unregisterReceiver(batteryReceiver)
        }.onFailure { e ->
            Log.e(TAG, "Error unregistering battery receiver", e)
        }
    }

    override fun onStop() {
        super.onStop()
        updateAllWidgets()
    }

    private fun updateBatteryState(intent: Intent?) {
        runCatching {
            val batteryInfo = BatteryInfo(intent)

            batteryState = batteryState.copy(
                health = batteryInfo.health,
                level = batteryInfo.level,
                isCharging = batteryInfo.isCharging,
                chargeSource = batteryInfo.chargeSource,
                chargeCycles = batteryInfo.chargeCycles,
                technology = batteryInfo.technology,
                temperature = batteryInfo.temperature,
                voltage = batteryInfo.voltage,
                capacity = batteryUtils.getBatteryCapacity(),
                capacityRemaining = batteryUtils.getCapacityRemaining(),
                currentNow = batteryUtils.getCurrentNow(),
                timeRemaining = batteryUtils.getChargeTimeRemaining(batteryInfo.isCharging),
                chargeSpeed = batteryUtils.getChargeSpeed(
                    batteryInfo.voltage,
                    batteryInfo.isCharging
                )
            )
        }.onFailure { e ->
            Log.e(TAG, "Error updating battery state", e)
        }
    }


    @Composable
    private fun getPurchaseErrorMessage(event: PurchaseManager.PurchaseEvent.Error): String {
        return when (event) {
            is PurchaseManager.PurchaseEvent.Error.ServiceUnavailable ->
                stringResource(R.string.widget_purchase_error_service_unavailable)

            is PurchaseManager.PurchaseEvent.Error.BillingUnavailable ->
                stringResource(R.string.widget_purchase_error_billing_unavailable)

            is PurchaseManager.PurchaseEvent.Error.ItemAlreadyOwned ->
                stringResource(R.string.widget_purchase_error_item_already_owned)

            is PurchaseManager.PurchaseEvent.Error.NetworkError ->
                stringResource(R.string.widget_purchase_error_network)

            is PurchaseManager.PurchaseEvent.Error.DeveloperError ->
                stringResource(R.string.widget_purchase_error_developer)

            is PurchaseManager.PurchaseEvent.Error.ProductUnavailable ->
                stringResource(R.string.widget_purchase_error_product_unavailable)

            is PurchaseManager.PurchaseEvent.Error.NoRestorablePurchases ->
                stringResource(R.string.widget_purchase_no_restorable_purchases)

            is PurchaseManager.PurchaseEvent.Error.NoPurchasesFound ->
                stringResource(R.string.widget_purchase_no_purchases_found)

            is PurchaseManager.PurchaseEvent.Error.Unknown ->
                stringResource(R.string.widget_purchase_error_unknown, event.code)
        }
    }

    private fun updateMonitoringService() {
        val prefs = PreferenceManager(this)
        val isAodEnabled = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_AOD_ENABLED)
        val isHealthyChargeEnabled =
            prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_HEALTHY_CHARGE_ENABLED)
        val isLowBatteryEnabled =
            prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_LOW_BATTERY_ENABLED)
        val isTempAlertEnabled =
            prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_TEMP_ALERT_ENABLED)
        val isFastDischargeEnabled =
            prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_FAST_DISCHARGE_ENABLED)
        val isSlowChargeEnabled =
            prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_SLOW_CHARGE_ENABLED)

        val shouldRun = isAodEnabled || isHealthyChargeEnabled || isLowBatteryEnabled ||
                isTempAlertEnabled || isFastDischargeEnabled || isSlowChargeEnabled

        if (shouldRun) {
            startBatteryAlertService()
        } else {
            stopBatteryAlertService()
        }
    }

    private fun startBatteryAlertService() {
        runCatching {
            val intent = Intent(this, BatteryAlertService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }.onFailure { e ->
            Log.e(TAG, "Failed to start BatteryAlertService", e)
        }
    }

    private fun stopBatteryAlertService() {
        runCatching {
            val intent = Intent(this, BatteryAlertService::class.java)
            stopService(intent)
        }.onFailure { e ->
            Log.e(TAG, "Failed to stop BatteryAlertService", e)
        }
    }

    private fun setupWidgetPreviews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            lifecycleScope.launch {
                val glanceManager = GlanceAppWidgetManager(this@MainActivity)

                listOf(
                    ChargingInfoWidgetReceiver::class,
                    BatteryLevelWidgetReceiver::class,
                    HealthStatusWidgetReceiver::class,
                    ChargeCyclesWidgetReceiver::class
                ).forEach { receiver ->
                    runCatching {
                        glanceManager.setWidgetPreviews(receiver)
                    }
                }
            }
        }
    }

    private fun updateAllWidgets() {
        val context = applicationContext

        lifecycleScope.launch {
            runCatching {
                ChargingInfoWidget().updateAll(context)
                ChargeCyclesWidget().updateAll(context)
                HealthStatusWidget().updateAll(context)
                BatteryLevelWidget().updateAll(context)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
