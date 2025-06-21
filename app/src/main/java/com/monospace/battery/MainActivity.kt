package com.monospace.battery

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.monospace.battery.databinding.ActivityMainBinding
import com.monospace.battery.helpers.NetworkUtils
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.modals.CompleteWidgetsPurchaseBottomSheet
import com.monospace.battery.modals.WidgetsPurchaseBottomSheet
import com.monospace.battery.purchase.PurchaseManager

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var settingsItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (settingsItem != null) {
                settingsItem?.isVisible = destination.id == R.id.FirstFragment
            }
        }

        if (NetworkUtils.isInternetAvailable(this)) {
            checkWidgetsPurchase()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        settingsItem = menu.findItem(R.id.action_settings)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController(R.id.nav_host_fragment_content_main)
                    .navigate(R.id.action_home_to_settings)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun checkWidgetsPurchase() {
        try {
            PurchaseManager(this, PurchaseManager.WIDGETS, null)
            if (!WidgetsUtils.isWidgetsPurchased(this)) {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}