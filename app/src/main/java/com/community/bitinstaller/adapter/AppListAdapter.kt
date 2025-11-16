package com.community.bitinstaller.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.community.bitinstaller.R
import com.community.bitinstaller.viewmodel.AppItem
import com.google.android.material.button.MaterialButton

class AppListAdapter(
    private val onInstallClick: (AppItem) -> Unit
) : ListAdapter<AppItem, AppListAdapter.ViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val appName: TextView = view.findViewById(R.id.appName)
        private val installedStatus: TextView = view.findViewById(R.id.installedStatus)
        private val versionInfo: TextView = view.findViewById(R.id.versionInfo)
        private val installButton: MaterialButton = view.findViewById(R.id.installButton)

        fun bind(item: AppItem) {
            appName.text = item.config.appName
            installedStatus.text = if (item.isInstalled) "Installed" else "Not Installed"

            val versionText = buildString {
                if (item.installedVersion != null) {
                    append("App version: ${item.installedVersion}")
                }
                if (item.availableVersion != null) {
                    if (isNotEmpty()) append("\n")
                    append("Source version: ${item.availableVersion}")
                }
            }
            versionInfo.text = versionText
            versionInfo.visibility = if (versionText.isEmpty()) View.GONE else View.VISIBLE

            installButton.isEnabled = item.isInstalled
            installButton.setOnClickListener { onInstallClick(item) }
        }
    }
}

private class AppDiffCallback : DiffUtil.ItemCallback<AppItem>() {
    override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean =
        oldItem.config.packageName == newItem.config.packageName

    override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean =
        oldItem == newItem
}
