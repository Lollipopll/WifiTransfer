package com.lifecycle.wifitransfer

import android.app.Application
import android.content.Context
import kotlin.properties.Delegates

/**
 * @author wang
 * @date   2020/6/7
 * des
 */
class MyApp : Application() {

    companion object {
        var context: Context by Delegates.notNull()
            private set

    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

}