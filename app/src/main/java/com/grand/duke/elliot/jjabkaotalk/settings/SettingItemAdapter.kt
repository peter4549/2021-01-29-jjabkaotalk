package com.grand.duke.elliot.jjabkaotalk.util

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemSettingBinding

class SettingItemAdapter(private val settingItems: ArrayList<SettingItem>): RecyclerView.Adapter<SettingItemAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(id: Int)
    }

    inner class ViewHolder(private val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(settingItem: SettingItem) {
            binding as ItemSettingBinding
            binding.textTitle.text = settingItem.title

            if (settingItem.description.isBlank())
                binding.textDescription.visibility = View.GONE
            else {
                binding.textDescription.visibility = View.VISIBLE
                binding.textDescription.text = settingItem.description
            }

            settingItem.drawable?.let {
                binding.imageIcon.visibility = View.VISIBLE
                binding.imageIcon.setImageDrawable(it)
            } ?: run {
                binding as ItemSettingBinding
                binding.imageIcon.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onItemClickListener?.onClick(settingItem.id)
            }
        }
    }

    private fun from(parent: ViewGroup): ViewHolder  {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val settingItem = settingItems[position]
        holder.bind(settingItem)
    }

    override fun getItemCount(): Int = settingItems.count()
}

data class SettingItem(
        val id: Int,
        val title: String,
        val description: String = blank,
        val drawable: Drawable? = null
)