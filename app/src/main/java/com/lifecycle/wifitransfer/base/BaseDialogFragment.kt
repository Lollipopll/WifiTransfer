package com.lifecycle.wifitransfer.base

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.lifecycle.wifitransfer.eventbus.EventBusUtils
import com.lifecycle.wifitransfer.eventbus.EventMessage
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author wang
 * @date   2020/6/7
 * des
 */
open class BaseDialogFragment : DialogFragment() {

    /**
     * 经过处理的show方法，避免多次点击重复添加
     * https://blog.csdn.net/u010648159/article/details/82711876
     */
    override fun show(manager: FragmentManager, tag: String?) {
        if (this.isAdded) {
            return
        }

        if (null == manager) {
            return
        }
        if (Integer.valueOf(Build.VERSION.SDK_INT) > Build.VERSION_CODES.JELLY_BEAN) {
            if (manager.isDestroyed)
                return
        }

        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit()
            super.show(manager, tag)
        } catch (e: Exception) {
            //同一实例使用不同的tag会异常,这里捕获一下
            e.printStackTrace()

        }
    }


    /**
     * 点击外部取消弹窗
     */
    fun setOutsideClickCancel(isCancel: Boolean) {
        dialog?.setCancelable(isCancel)
        dialog?.setCanceledOnTouchOutside(isCancel)
    }


    /**
     * 不校验状态关闭弹窗
     */
    fun dismissSafe() {
        dismissAllowingStateLoss()
    }


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