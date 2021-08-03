package com.pinkcloud.memento.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.databinding.ListItemMemoBinding
import com.pinkcloud.memento.utils.DoubleClickListener

class MemoAdapter(private val doubleTapItemListener: DoubleTapItemListener): ListAdapter<Memo, MemoAdapter.ViewHolder>(MemoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), doubleTapItemListener)
    }

    interface DoubleTapItemListener {
        fun onDoubleTapItem(memoId: Long)
    }

    class ViewHolder private constructor(val binding: ListItemMemoBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Memo, doubleTapItemListener: DoubleTapItemListener) {
            binding.memoView.imagePath = item.imagePath
            binding.memoView.frontCaption = item.frontCaption
            binding.memoView.backCaption = item.backCaption
            binding.memoView.priority = item.priority
            binding.memoView.alarmTime = item.alarmTime
            binding.memoView.setOnClickListener(object: DoubleClickListener() {
                override fun onDoubleClick(v: View?) {
                    doubleTapItemListener.onDoubleTapItem(item.memoId)
                }
            })
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemMemoBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class MemoDiffCallback: DiffUtil.ItemCallback<Memo>() {
    override fun areItemsTheSame(oldItem: Memo, newItem: Memo): Boolean {
        return oldItem.memoId == newItem.memoId
    }

    override fun areContentsTheSame(oldItem: Memo, newItem: Memo): Boolean {
        return oldItem == newItem
    }
}