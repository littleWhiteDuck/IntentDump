package me.dumpIntent.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.dumpIntent.R
import me.dumpIntent.bean.MainItem
import me.dumpIntent.databinding.ItemMainLayoutBinding

class ConfigAdapter(private val onClick: (MainItem) -> Unit) :
    ListAdapter<MainItem, ConfigAdapter.ViewHolder>(MainDiff) {

    class ViewHolder(binding: ItemMainLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvAppName = binding.tvAppName
        val tvPackageName = binding.tvPackageName
        val tvClassName = binding.tvClassName
        val ivIcon = binding.ivIcon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMainLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        holder.itemView.setOnClickListener {
            val mainItem = it.getTag(R.id.main_item_position) as MainItem
            onClick(mainItem)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setTag(R.id.main_item_position, item)
        holder.apply {
            item.apply {
                tvAppName.text = appName
                tvPackageName.text = configBean.packageName
                tvClassName.text = configBean.className
                ivIcon.setImageDrawable(appIcon)
            }
        }
    }


    object MainDiff : DiffUtil.ItemCallback<MainItem>() {

        override fun areItemsTheSame(oldItem: MainItem, newItem: MainItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MainItem, newItem: MainItem) =
            oldItem.configBean == newItem.configBean
    }

}