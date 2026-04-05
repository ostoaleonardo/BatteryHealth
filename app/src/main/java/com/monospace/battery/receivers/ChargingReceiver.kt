package com.monospace.battery.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.monospace.battery.services.ChargingService

class ChargingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Action received: $action")

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                Log.d(TAG, "Charger connected - Starting ChargingService")
                val serviceIntent = Intent(context, ChargingService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
            
            Intent.ACTION_POWER_DISCONNECTED -> {
                Log.d(TAG, "Charger disconnected - Stopping ChargingService")
                val serviceIntent = Intent(context, ChargingService::class.java)
                context.stopService(serviceIntent)
            }
        }
    }

    companion object {
        private const val TAG = "ChargingReceiver"
    }
}