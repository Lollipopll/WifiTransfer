package com.lifecycle.wifitransfer.eventbus

/**
 * @author wang
 * @date   2020/6/7
 * des
 */
data class EventMessage<T>(var from: String, var code: Int, var data: T)