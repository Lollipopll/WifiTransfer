package com.lifecycle.wifitransfer.eventbus

/**
 * @author wang
 * @date   2020/6/7
 * des
 */
class EventCode {

    companion object {
        /**
         * 刷新主页面appList列表
         */
        const val EVENT_CODE_REFRESH_MAIN_APP_LIST = 1000

        /**
         * wifi状态变化
         */
        const val EVENT_CODE_WIFI_CONNECT_CHANGE = 1001
    }
}