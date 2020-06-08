package com.lifecycle.wifitransfer.constant

import com.lifecycle.wifitransfer.MyApp
import java.io.File

/**
 * @author wang
 * @date   2020/6/7
 * des
 */
class Constants {
    companion object {
        const val HTTP_SERVER_PORT = 12345
        private const val APK_DIR_NAME = "apk"

        // 使用app分配的空间，不需要存储权限
        val APK_DIR by lazy {
            File(
                MyApp.context.getExternalFilesDir(
                    APK_DIR_NAME
                )?.absolutePath
                    ?: MyApp.context.filesDir.absolutePath
            )
        }

    }
}