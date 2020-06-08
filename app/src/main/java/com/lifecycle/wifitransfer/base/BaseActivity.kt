package com.lifecycle.wifitransfer.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lifecycle.wifitransfer.eventbus.EventBusUtils
import com.lifecycle.wifitransfer.eventbus.EventMessage
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author wang
 * @date   2020/6/7
 * des
 */
open class BaseActivity : AppCompatActivity() {


//    private fun checkAppNeedPermission() {
//        PermissionX.init(this)
//            .permissions(
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            )
//            .onExplainRequestReason { deniedList ->
//                showRequestReasonDialog(deniedList, "即将重新申请的权限是程序必须依赖的权限", "我已明白", "取消")
//            }
//            .onForwardToSettings { deniedList ->
//                showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白", "取消")
//            }
//            .request { _, _, _ ->
//
//            }
//
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isRegisteredEventBus()) {
            EventBusUtils.register(this)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (isRegisteredEventBus()) {
            EventBusUtils.unregister(this)
        }
    }


    /**
     * 是否注册事件分发
     *
     * @return true 注册；false 不注册，默认不注册
     */
    protected open fun isRegisteredEventBus(): Boolean {
        return false
    }

    /**
     * 接收到分发的事件
     *
     * @param event 事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onReceiveEvent(event: EventMessage<*>?) {
    }

    /**
     * 接受到分发的粘性事件
     *
     * @param event 粘性事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    open fun onReceiveStickyEvent(event: EventMessage<*>?) {
    }


}