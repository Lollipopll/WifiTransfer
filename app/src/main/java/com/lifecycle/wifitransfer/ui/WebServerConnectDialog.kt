package com.lifecycle.wifitransfer.ui

import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lifecycle.wifitransfer.R
import com.lifecycle.wifitransfer.base.BaseDialogFragment
import com.lifecycle.wifitransfer.constant.Constants
import com.lifecycle.wifitransfer.databinding.FragmentWebserverConnectBinding
import com.lifecycle.wifitransfer.eventbus.EventCode
import com.lifecycle.wifitransfer.eventbus.EventMessage
import com.lifecycle.wifitransfer.receiver.WifiConnectChangedReceiver
import com.lifecycle.wifitransfer.service.WebService
import com.lifecycle.wifitransfer.utils.WifiUtils


/**
 * @author wang
 * @date   2020/6/7
 * des
 */
class WebServerConnectDialog : BaseDialogFragment() {

    private var wifiConnectChangedReceiver: WifiConnectChangedReceiver =
        WifiConnectChangedReceiver()

    private lateinit var uiBinding: FragmentWebserverConnectBinding

    private lateinit var dismissListener: () -> Unit

    fun addDismissListener(dismissListener: () -> Unit): WebServerConnectDialog {
        this.dismissListener = dismissListener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.WebServerConnectDialogStyle
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uiBinding = FragmentWebserverConnectBinding.inflate(inflater, container, false)
        initViewListener()
        val dm = resources.displayMetrics
        uiBinding.root.minimumWidth = dm.widthPixels
        return uiBinding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.let {
            it.setCanceledOnTouchOutside(false)
            it.setCancelable(false)
            val dialogWindow = it.window
            dialogWindow!!.setGravity(Gravity.BOTTOM)
        }
        WebService.start(requireContext())
        registerWifiConnectChangedReceiver()

    }

    private fun initViewListener() {
        uiBinding.sharedWifiCancel.setOnClickListener {
            dismissSafe()
        }

        uiBinding.sharedWifiSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }


    override fun isRegisteredEventBus(): Boolean {
        return true
    }

    override fun onReceiveEvent(event: EventMessage<*>?) {
        super.onReceiveEvent(event)
        if (null != event && event.code == EventCode.EVENT_CODE_WIFI_CONNECT_CHANGE) {
            val state = event.data as NetworkInfo.State
            if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                if (state == NetworkInfo.State.CONNECTED) {
                    val ip: String = WifiUtils.getWifiIp(requireContext())
                    if (!TextUtils.isEmpty(ip)) {
                        onWifiConnected(ip)
                        return
                    }
                }
                onWifiConnecting()
                return
            }
            onWifiDisconnected()
        }
    }

    private fun onWifiDisconnected() {
        uiBinding.popupMenuTitle.setText(R.string.wlan_disabled)
        uiBinding.popupMenuTitle.setTextColor(requireContext().resources.getColor(android.R.color.black))
        uiBinding.popupMenuSubtitle.visibility = View.VISIBLE
        uiBinding.sharedWifiState.setImageResource(R.mipmap.shared_wifi_shut_down)
        uiBinding.sharedWifiStateHint.setText(R.string.fail_to_start_http_service)
        uiBinding.sharedWifiAddress.visibility = View.GONE
        uiBinding.sharedWifiButtonSplitLine.visibility = View.VISIBLE
        uiBinding.sharedWifiSettings.visibility = View.VISIBLE
    }

    private fun onWifiConnecting() {
        uiBinding.popupMenuTitle.setText(R.string.wlan_enabled)
        uiBinding.popupMenuTitle.setTextColor(requireContext().resources.getColor(R.color.colorWifiConnected))
        uiBinding.popupMenuSubtitle.visibility = View.GONE
        uiBinding.sharedWifiState.setImageResource(R.mipmap.shared_wifi_enable)
        uiBinding.sharedWifiStateHint.setText(R.string.retrofit_wlan_address)
        uiBinding.sharedWifiAddress.visibility = View.GONE
        uiBinding.sharedWifiButtonSplitLine.visibility = View.GONE
        uiBinding.sharedWifiSettings.visibility = View.GONE
    }

    private fun onWifiConnected(ipAddress: String?) {
        uiBinding.popupMenuTitle.setText(R.string.wlan_enabled)
        uiBinding.popupMenuTitle.setTextColor(requireContext().resources.getColor(R.color.colorWifiConnected))
        uiBinding.popupMenuSubtitle.visibility = View.GONE
        uiBinding.sharedWifiState.setImageResource(R.mipmap.shared_wifi_enable)
        uiBinding.sharedWifiStateHint.setText(R.string.pls_input_the_following_address_in_pc_browser)
        uiBinding.sharedWifiAddress.visibility = View.VISIBLE
        uiBinding.sharedWifiAddress.text = String.format(
            requireContext().getString(R.string.http_address),
            ipAddress,
            Constants.HTTP_SERVER_PORT
        )
        uiBinding.sharedWifiButtonSplitLine.visibility = View.GONE
        uiBinding.sharedWifiSettings.visibility = View.GONE
    }


    private fun registerWifiConnectChangedReceiver() {
        val intentFilter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        context?.registerReceiver(wifiConnectChangedReceiver, intentFilter)
    }

    private fun unRegisterWifiConnectChangedReceiver() {
        context?.unregisterReceiver(wifiConnectChangedReceiver)
    }

    override fun onDestroy() {
        WebService.stop(requireContext())
        unRegisterWifiConnectChangedReceiver()
        super.onDestroy()
        dismissListener.invoke()
    }

}

