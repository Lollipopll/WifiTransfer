package com.lifecycle.wifitransfer.ui.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lifecycle.wifitransfer.utils.ApkUtils
import com.lifecycle.wifitransfer.R
import com.lifecycle.wifitransfer.model.AppInfoModel

/**
 * @author wang
 * @date   2020/6/7
 * des
 */
class AppListAdapter(layoutResId: Int, data: MutableList<AppInfoModel>?) :
    BaseQuickAdapter<AppInfoModel, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: AppInfoModel) {

        holder.setText(R.id.tvName, item.name)
        holder.setText(R.id.tvSize, item.size)
        holder.setText(R.id.tvPath, item.path)
        holder.setImageDrawable(R.id.ivIcon, item.icon)
        holder.setGone(R.id.btnUninstall, !item.installed)

        holder.getView<TextView>(R.id.btnInstall).setOnClickListener {
            ApkUtils.installApkFile(context,item.path)
        }

        holder.getView<TextView>(R.id.btnUninstall).setOnClickListener {
            ApkUtils.uninstallApk(context,item.packageName)
        }
    }
}