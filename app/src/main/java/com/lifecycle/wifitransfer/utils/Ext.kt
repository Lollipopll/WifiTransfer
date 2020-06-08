package com.lifecycle.wifitransfer.utils

import android.content.Context
import android.widget.Toast

/**
 * @author wang
 * @date   2020/6/7
 * des
 */


fun Context.toast(str: String) {
    Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
}