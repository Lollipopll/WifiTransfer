package com.lifecycle.wifitransfer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Parcelable
import com.lifecycle.wifitransfer.eventbus.EventBusUtils

/**
 * @author wang
 * @date   2020/6/7
 * des
 */

class WifiConnectChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION == intent!!.action) {
            val parcelableExtra = intent
                .getParcelableExtra<Parcelable>(WifiManager.EXTRA_NETWORK_INFO)
            if (null != parcelableExtra) {
                val networkInfo = parcelableExtra as NetworkInfo
                EventBusUtils.sendWifiConnectChangedMessage(networkInfo.state)
            }
        }
    }

}