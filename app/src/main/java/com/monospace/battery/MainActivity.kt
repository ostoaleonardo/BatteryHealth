package com.monospace.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.BatteryUtils
import com.monospace.battery.purchase.PurchaseManager
import com.monospace.battery.ui.components.BatteryState
import com.monospace.battery.ui.components.CompleteWidgetsPurchaseContent
import com.monospace.battery.ui.components.WidgetsPurchaseContent
import com.monospace.battery.ui.screens.MainScreen
import com.monospace.battery.ui.screens.SettingsScreen
import com.monospace.battery.ui.theme.BatteryTheme
import com.monospace.battery.widgets.charging.BatteryLevelWidgetReceiver
import com.monospace.battery.widgets.charging.ChargingInfoWidgetReceiver
import com.monospace.battery.widgets.cycles.ChargeCyclesWidgetReceiver
import com.monospace.battery.widgets.health.HealthStatusWidgetReceiver
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
}

class MainActivity : FragmentActivity() {

    private var batteryState by mutableStateOf(BatteryState())
    private val batteryUtils by lazy { BatteryUtils(this) }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryState(intent)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

                var showPurchaseSheet by remember { mutableStateOf(false) }
                var showSuccessSheet by remember { mutableStateOf(false) }

                LaunchedEffect(isPurchased) {
                    showPurchaseSheet = !isPurchased
                }

                LaunchedEffect(Unit) {
                    purchaseManager.purchaseEvents.collect { event ->
                        when (event) {
                            is PurchaseManager.PurchaseEvent.Success -> {
                                showPurchaseSheet = false
                                showSuccessSheet = true
                            }

                            is PurchaseManager.PurchaseEvent.Error -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                if (showPurchaseSheet) {
                    ModalBottomSheet(
                        sheetState = rememberModalBottomSheetState(),
                        onDismissRequest = {
                            showPurchaseSheet = false
                        }
                    ) {
                        WidgetsPurchaseContent(
                            onBuyClick = {
                                Log.d("MainActivity", "User clicked buy")
                                purchaseManager.launchBuyBillingFlow(this@MainActivity)
                            },
                            onRestoreClick = {
                                Log.d("MainActivity", "User clicked restore")
                                purchaseManager.restorePurchases()
                            }
                        )
                    }
                }

                if (showSuccessSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSuccessSheet = false }
                    ) {
                        CompleteWidgetsPurchaseContent()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        BatteryTopAppBar(
                            currentRoute = currentRoute,
                            onSettingsClick = { navController.navigate(Screen.Settings.route) },
                            onBackClick = { navController.popBackStack() }
                        )
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
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onUnlockClick = {
                                    showPurchaseSheet = true
                                }
                            )
                        }
                    }
                }
            }
        }

        try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val stickyIntent = registerReceiver(batteryReceiver, intentFilter)
            updateBatteryState(stickyIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering battery receiver", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering battery receiver", e)
        }
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BatteryTopAppBar(
        currentRoute: String?,
        onSettingsClick: () -> Unit,
        onBackClick: () -> Unit
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (currentRoute == Screen.Settings.route) stringResource(R.string.action_settings)
                    else stringResource(R.string.app_name)
                )
            },
            actions = {
                if (currentRoute == Screen.Home.route) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = stringResource(R.string.action_settings)
                        )
                    }
                }
            },
            navigationIcon = {
                if (currentRoute == Screen.Settings.route) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
        )
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

    companion object {
        private const val TAG = "MainActivity"
    }
}
