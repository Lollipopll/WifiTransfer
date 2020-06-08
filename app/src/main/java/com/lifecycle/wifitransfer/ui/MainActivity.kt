package com.lifecycle.wifitransfer.ui

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.animation.AccelerateInterpolator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lifecycle.wifitransfer.R
import com.lifecycle.wifitransfer.base.BaseActivity
import com.lifecycle.wifitransfer.constant.Constants
import com.lifecycle.wifitransfer.databinding.ActivityMainBinding
import com.lifecycle.wifitransfer.eventbus.EventCode
import com.lifecycle.wifitransfer.eventbus.EventMessage
import com.lifecycle.wifitransfer.model.AppInfoModel
import com.lifecycle.wifitransfer.ui.adapter.AppListAdapter
import com.lifecycle.wifitransfer.utils.ApkUtils
import com.lifecycle.wifitransfer.utils.toast
import java.io.File

class MainActivity : BaseActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private val appList = arrayListOf<AppInfoModel>()
    private val appListAdapter by lazy {
        AppListAdapter(R.layout.layout_item_app, appList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(mainBinding.root)
        setSupportActionBar(mainBinding.toolbar)
        initViewListener()
        intiRecycleView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar_item, menu)
        return super.onCreateOptionsMenu(menu)
    }


    private fun intiRecycleView() {

        mainBinding.rvMain.layoutManager = LinearLayoutManager(this)
        mainBinding.rvMain.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL
            )
        )

        mainBinding.rvMain.adapter = appListAdapter
        appListAdapter.setEmptyView(R.layout.empty_view)
        refreshAppList()
    }

    /**
     *初始化View监听
     */
    private fun initViewListener() {
        mainBinding.fab.setOnClickListener {
            showFabOutAnim()
            WebServerConnectDialog()
                .addDismissListener {
                    showFabInAnim()
                }
                .show(supportFragmentManager, "WebServerConnectDialog")
        }

        mainBinding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.btnDeleteAll -> {
                    if (appList.isEmpty()) {
                        toast("暂无可删除内容")
                    } else {
                        showDeleteAllDialog()
                    }
                }
            }
            false
        }
    }

    private fun showFabInAnim() {
        val anim: ObjectAnimator =
            ObjectAnimator.ofFloat(
                mainBinding.fab,
                "translationY",
                mainBinding.fab.height * 2.0f,
                0f
            )
                .setDuration(200L)
        anim.interpolator = AccelerateInterpolator()
        anim.start()
    }

    private fun showFabOutAnim() {
        val anim: ObjectAnimator =
            ObjectAnimator.ofFloat(
                mainBinding.fab,
                "translationY",
                0f,
                mainBinding.fab.height * 2.0f
            )
                .setDuration(200L)
        anim.interpolator = AccelerateInterpolator()
        anim.start()
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this).setTitle("温馨提示:")
            .setMessage("确定全部删除吗？")
            .setPositiveButton("确定",
                DialogInterface.OnClickListener { _, _ ->
                    ApkUtils.deleteAll {
                        refreshAppList()
                    }
                })
            .setNegativeButton("取消",
                DialogInterface.OnClickListener { _, _ -> })
            .show()
    }

    override fun isRegisteredEventBus(): Boolean {
        return true
    }

    override fun onReceiveEvent(event: EventMessage<*>?) {
        super.onReceiveEvent(event)
        event?.let {
            when (it.code) {
                EventCode.EVENT_CODE_REFRESH_MAIN_APP_LIST -> {
                    refreshAppList()
                }
            }
        }
    }

    /**
     * 刷新主页列表
     */
    private fun refreshAppList() {
        val listArr = mutableListOf<AppInfoModel>()
        val dir: File = Constants.APK_DIR
        if (dir.exists() && dir.isDirectory) {
            val fileNames = dir.listFiles()
            if (fileNames != null) {
                for (fileName in fileNames) {
                    ApkUtils.handleApk(
                        fileName.absolutePath,
                        fileName.length(),
                        listArr
                    )
                }
            }
        }
        appList.clear()
        appList.addAll(listArr)
        appListAdapter.notifyDataSetChanged()
    }
}