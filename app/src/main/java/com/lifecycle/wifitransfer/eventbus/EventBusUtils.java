package com.lifecycle.wifitransfer.eventbus;

import android.net.NetworkInfo;

import org.greenrobot.eventbus.EventBus;

/**
 * @author Wang
 * @date 2019/1/24
 * des
 */
public class EventBusUtils {

    private EventBusUtils() {
    }

    /**
     * 注册 EventBus
     *
     * @param subscriber
     */
    public static void register(Object subscriber) {
        EventBus eventBus = EventBus.getDefault();
        if (!eventBus.isRegistered(subscriber)) {
            eventBus.register(subscriber);
        }
    }

    /**
     * 解除注册 EventBus
     *
     * @param subscriber
     */
    public static void unregister(Object subscriber) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.isRegistered(subscriber)) {
            eventBus.unregister(subscriber);
        }
    }

    /**
     * 发送事件消息
     *
     * @param event
     */
    public static void post(EventMessage event) {
        EventBus.getDefault().post(event);
    }

    /**
     * 发送粘性事件消息
     *
     * @param event
     */
    public static void postSticky(EventMessage event) {
        EventBus.getDefault().postSticky(event);
    }


    /**
     * 刷新主页列表消息
     **/
    public static void sendRefreshAppListMessage() {
        EventBusUtils.post(
                new EventMessage<>(
                        "",
                        EventCode.EVENT_CODE_REFRESH_MAIN_APP_LIST,
                        null)

        );
    }

    /**
     * wifi状态改变消息
     *
     * @param networkInfoState
     */
    public static void sendWifiConnectChangedMessage(NetworkInfo.State networkInfoState) {
        EventBusUtils.post(
                new EventMessage<>(
                        "",
                        EventCode.EVENT_CODE_WIFI_CONNECT_CHANGE,
                        networkInfoState)

        );
    }

}


