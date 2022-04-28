package com.example.lori.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.models.TermsAndCondition
import kotlinx.android.synthetic.main.layout_terms_and_condition.view.*

class TermsAndConditionAdapter :
    ListAdapter<TermsAndCondition, TermsAndConditionAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_terms_and_condition, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.displayData(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun displayData(termsAndCondition: TermsAndCondition) {
            itemView.tvTermsAndConditionContent.text = termsAndCondition.content
            itemView.tvTermsAndConditionContent.setCompoundDrawablesWithIntrinsicBounds(
                termsAndCondition.drawableStart ?: R.drawable.ic_launcher_foreground,
                0,
                0,
                0,
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TermsAndCondition>() {
        override fun areItemsTheSame(
            oldItem: TermsAndCondition,
            newItem: TermsAndCondition
        ): Boolean =
            oldItem === newItem

        override fun areContentsTheSame(
            oldItem: TermsAndCondition,
            newItem: TermsAndCondition
        ): Boolean =
            oldItem.id == newItem.id
    }
}
