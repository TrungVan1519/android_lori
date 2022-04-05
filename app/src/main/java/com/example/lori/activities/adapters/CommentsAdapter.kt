package com.example.lori.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lori.R
import com.example.lori.models.Comment
import kotlinx.android.synthetic.main.layout_comments.view.*

class CommentsAdapter : ListAdapter<Comment, CommentsAdapter.ViewHolder>(DiffCallback()) {
    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_comments, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.displayData(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val listener: Listener?
    ) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                listener?.onClick(adapterPosition)
            }
            itemView.setOnLongClickListener {
                listener?.onLongClick(adapterPosition)
                true
            }
        }

        fun displayData(comment: Comment) {
            Glide.with(itemView.context).load(comment.userImage).centerCrop().into(itemView.ivUserImage)
            itemView.tvUserEmail.text = comment.userEmail
            itemView.tvCommentStart.text = comment.start.toString()
            itemView.tvCommentContent.text = comment.content
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean =
            oldItem === newItem

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean =
            oldItem.id == newItem.id
    }

    interface Listener {
        fun onClick(position: Int)
        fun onLongClick(position: Int)
    }
}
