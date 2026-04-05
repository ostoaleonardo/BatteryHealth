package com.monospace.battery

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.BatteryUtils
import com.monospace.battery.helpers.NetworkUtils
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.modals.CompleteWidgetsPurchaseBottomSheet
import com.monospace.battery.modals.WidgetsPurchaseBottomSheet
import com.monospace.battery.purchase.PurchaseManager
import com.monospace.battery.receivers.ChargingReceiver
import com.monospace.battery.ui.components.BatteryState
import com.monospace.battery.ui.screens.MainScreen
import com.monospace.battery.ui.screens.SettingsScreen
import com.monospace.battery.ui.theme.BatteryTheme

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
}

class MainActivity : FragmentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Si no se concede, la notificación no aparecerá en Android 13+
    }

    private var batteryState by mutableStateOf(BatteryState())

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryState(intent)
        }
    }

    private val chargingReceiver = ChargingReceiver()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (NetworkUtils.isInternetAvailable(this)) {
            checkWidgetsPurchase()
        }

        setContent {
            BatteryTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = if (currentRoute == Screen.Settings.route) stringResource(R.string.action_settings)
                                    else stringResource(R.string.app_name)
                                )
                            },
                            actions = {
                                if (currentRoute == Screen.Home.route) {
                                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                                        Icon(
                                            imageVector = Icons.Sharp.Settings,
                                            contentDescription = stringResource(R.string.action_settings)
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                if (currentRoute == Screen.Settings.route) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                                            contentDescription = stringResource(R.string.action_settings),
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                            }
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
                                onUnlockClick = { showPurchaseBottomSheet() }
                            )
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = registerReceiver(batteryReceiver, intentFilter)
        updateBatteryState(batteryIntent)

        // Registro dinámico del receptor de carga para asegurar que reciba eventos
        val chargingFilter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(chargingReceiver, chargingFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        try {
            unregisterReceiver(chargingReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateBatteryState(intent: Intent?) {
        val batteryInfo = BatteryInfo(intent)
        val batteryUtils = BatteryUtils(this)
        
        batteryState = BatteryState(
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
            chargeSpeed = batteryUtils.getChargeSpeed(intent ?: Intent(), batteryInfo.isCharging)
        )
    }

    private fun checkWidgetsPurchase() {
        try {
            PurchaseManager(this, PurchaseManager.WIDGETS, null)

            if (!WidgetsUtils.isWidgetsPurchased(this) && !isFinishing && !isDestroyed) {
                showPurchaseBottomSheet()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showPurchaseBottomSheet() {
        WidgetsPurchaseBottomSheet(
            onPurchaseSuccess = {
                CompleteWidgetsPurchaseBottomSheet().show(
                    supportFragmentManager,
                    CompleteWidgetsPurchaseBottomSheet.TAG
                )
            }
        ).show(
            supportFragmentManager,
            WidgetsPurchaseBottomSheet.TAG
        )
    }
}
