package com.cocido.ramfapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.OnboardingItem

/**
 * Adapter para el ViewPager2 del onboarding
 */
class OnboardingAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<OnboardingItem, OnboardingAdapter.OnboardingViewHolder>(OnboardingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(getItem(position), position, onItemClick)
    }

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivOnboardingImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvOnboardingTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvOnboardingDescription)

        fun bind(item: OnboardingItem, position: Int, onItemClick: (Int) -> Unit) {
            ivImage.setImageResource(item.imageRes)
            tvTitle.text = item.title
            tvDescription.text = item.description
            
            // Cambiar color de fondo
            itemView.setBackgroundColor(itemView.context.getColor(item.backgroundColor))
            
            // Click listener
            itemView.setOnClickListener {
                onItemClick(position)
            }
        }
    }

    class OnboardingDiffCallback : DiffUtil.ItemCallback<OnboardingItem>() {
        override fun areItemsTheSame(oldItem: OnboardingItem, newItem: OnboardingItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: OnboardingItem, newItem: OnboardingItem): Boolean {
            return oldItem == newItem
        }
    }
}









