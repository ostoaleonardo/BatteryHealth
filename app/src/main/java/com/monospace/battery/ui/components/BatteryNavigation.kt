package com.monospace.battery.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.monospace.battery.R

sealed class Screen(val route: String, val labelRes: Int = 0, val iconRes: Int = 0) {
    object Home : Screen("home", R.string.nav_home, R.drawable.bolt)
    object History : Screen("history", R.string.nav_history, R.drawable.schedule)
    object Alerts : Screen("alerts", R.string.nav_alerts, R.drawable.battery_alert)
    object Settings : Screen("settings")
}

@Composable
fun BottomNavigation(
    currentRoute: String?,
    navController: NavHostController
) {
    val items = listOf(
        Screen.Home,
        Screen.History,
        Screen.Alerts
    )

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconRes),
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = screen.labelRes),
                        fontFamily = FontFamily(Font(R.font.azeret_mono_light))
                    )
                },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    currentRoute: String?,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = when (currentRoute) {
                    Screen.Settings.route -> stringResource(R.string.action_settings)
                    Screen.History.route -> stringResource(R.string.nav_history)
                    Screen.Alerts.route -> stringResource(R.string.nav_alerts)
                    else -> stringResource(R.string.app_name)
                }
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
