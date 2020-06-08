package com.lifecycle.wifitransfer.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources.NotFoundException
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import androidx.core.content.FileProvider
import com.lifecycle.wifitransfer.MyApp
import com.lifecycle.wifitransfer.constant.Constants
import com.lifecycle.wifitransfer.model.AppInfoModel
import java.io.File
import java.text.DecimalFormat

/**
 * @author wang
 * @date   2020/6/7
 * des
 */
object ApkUtils {


    /**
     * 安装apk
     */
    fun installApkFile(context: Context, path: String) {
        if (TextUtils.isEmpty(path)) return
        val apkFile = File(path)
        val installAllowed: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            installAllowed = context.packageManager.canRequestPackageInstalls()
            if (installAllowed) {
                installApkFile(
                    context,
                    apkFile
                )
            } else {
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + context.packageName)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                installApkFile(
                    context,
                    apkFile
                )
            }
        } else {
            installApkFile(
                context,
                apkFile
            )
        }
    }

    private fun installApkFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        //兼容7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(
                Uri.fromFile(file),
                "application/vnd.android.package-archive"
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (context.packageManager.queryIntentActivities(intent, 0).size > 0) {
            context.startActivity(intent)
        }
    }

    /**
     * 卸载apk
     */
    fun uninstallApk(context: Context, packageName: String) {
        if (TextUtils.isEmpty(packageName)) return
        val uri = Uri.fromParts("package", packageName, null)
        val intent = Intent(Intent.ACTION_DELETE, uri)
        context.startActivity(intent)
    }

    /**
     * 获取apkSize
     */
    fun getFileSize(length: Long): String? {
        val df = DecimalFormat("######0.00")
        val d1 = 3.23456
        val d2 = 0.0
        val d3 = 2.0
        df.format(d1)
        df.format(d2)
        df.format(d3)
        val l = length / 1000 //KB
        if (l < 1024) {
            return df.format(l) + "KB"
        } else if (l < 1024 * 1024f) {
            return df.format((l / 1024f).toDouble()) + "MB"
        }
        return df.format(l / 1024f / 1024f.toDouble()) + "GB"
    }

    @Synchronized
    fun getIconFromPackageName(
        packageName: String?,
        context: Context
    ): Drawable? {
        val pm = context.packageManager
        try {
            val pi = pm.getPackageInfo(packageName, 0)
            val otherAppCtx = context.createPackageContext(
                packageName,
                Context.CONTEXT_IGNORE_SECURITY
            )
            val displayMetrics = intArrayOf(
                DisplayMetrics.DENSITY_XXXHIGH,
                DisplayMetrics.DENSITY_XXHIGH,
                DisplayMetrics.DENSITY_XHIGH,
                DisplayMetrics.DENSITY_HIGH,
                DisplayMetrics.DENSITY_TV
            )
            for (displayMetric in displayMetrics) {
                try {
                    val d = otherAppCtx.resources
                        .getDrawableForDensity(pi.applicationInfo.icon, displayMetric)
                    if (d != null) {
                        return d
                    }
                } catch (e: NotFoundException) {
                    continue
                }
            }
        } catch (e: Exception) {
            // Handle Error here
        }
        var appInfo: ApplicationInfo? = null
        appInfo = try {
            pm.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }
        return appInfo.loadIcon(pm)
    }


    fun getApplicationName(packageName: String?): String? {
        var packageManager: PackageManager? = null
        var applicationInfo: ApplicationInfo? = null
        try {
            packageManager = MyApp.context.packageManager
            applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            applicationInfo = null
        }
        return if (packageManager != null && applicationInfo != null) {
            packageManager.getApplicationLabel(applicationInfo) as String
        } else packageName
    }

    /**
     * 判断相对应的APP是否存在
     *
     * @param context
     * @param packageName(包名)(若想判断QQ，则改为com.tencent.mobileqq，若想判断微信，则改为com.tencent.mm)
     * @return
     */
    fun isAvilible(
        context: Context,
        packageName: String?
    ): Boolean {
        val packageManager = context.packageManager

        //获取手机系统的所有APP包名，然后进行一一比较
        val pinfo =
            packageManager.getInstalledPackages(0)
        for (i in pinfo.indices) {
            if ((pinfo[i] as PackageInfo).packageName
                    .equals(packageName, ignoreCase = true)
            ) return true
        }
        return false
    }


    //获取apk信息
    fun handleApk(
        path: String,
        length: Long,
        list: MutableList<AppInfoModel>
    ) {

        var archiveFilePath = ""
        archiveFilePath = path
        val pm: PackageManager = MyApp.context.packageManager
        val info = pm.getPackageArchiveInfo(archiveFilePath, 0)
        if (info != null) {
            val appInfo = info.applicationInfo
            appInfo.sourceDir = archiveFilePath
            appInfo.publicSourceDir = archiveFilePath
            val packageName = appInfo.packageName //得到安装包名称
            val version = info.versionName //得到版本信息
            var icon = pm.getApplicationIcon(appInfo)
            var appName: String = pm.getApplicationLabel(appInfo).toString()
            if (TextUtils.isEmpty(appName)) {
                appName = getApplicationName(packageName ?: "") ?: ""
            }
            if (icon == null) {
                icon = getIconFromPackageName(packageName, MyApp.context) // 获得应用程序图标
            }

            val infoModel = AppInfoModel(
                path,
                version,
                getFileSize(length) ?: "",
                appName,
                packageName,
                isAvilible(MyApp.context, packageName),
                icon
            )
            list.add(infoModel)
        }
    }


    //删除所有文件
    fun deleteAll(block: () -> Unit) {
        val dir: File = Constants.APK_DIR
        if (dir.exists() && dir.isDirectory) {
            val fileNames = dir.listFiles()
            if (fileNames != null) {
                for (fileName in fileNames) {
                    fileName.delete()
                }
            }
        }
        block.invoke()
    }

}