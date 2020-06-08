package com.lifecycle.wifitransfer.model

import android.graphics.drawable.Drawable

/**
 * @author wang
 * @date   2020/6/7
 * des
 */
data class AppInfoModel(
    val path: String,
    val version: String,
    val size: String,
    val name: String,
    val packageName: String,
    val installed: Boolean,
    val icon: Drawable
)