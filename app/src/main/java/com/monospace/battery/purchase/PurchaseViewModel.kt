package com.monospace.battery.purchase

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class PurchaseViewModel(application: Application) : AndroidViewModel(application) {
    val manager = PurchaseManager(application, PurchaseManager.WIDGETS)

    override fun onCleared() {
        manager.close()
    }
}
