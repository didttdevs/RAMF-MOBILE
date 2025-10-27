package com.cocido.ramfapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cocido.ramfapp.databinding.ItemSettingsSettingBinding

class SettingsAdapter(
    private val settings: List<SettingItem>
) : RecyclerView.Adapter<SettingsAdapter.SettingViewHolder>() {

    data class SettingItem(
        val title: String,
        val description: String,
        val icon: Int,
        val hasSwitch: Boolean = false,
        val hasArrow: Boolean = false,
        val switchState: Boolean = false,
        val onClick: () -> Unit = {}
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val binding = ItemSettingsSettingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SettingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        holder.bind(settings[position])
    }

    override fun getItemCount(): Int = settings.size

    inner class SettingViewHolder(private val binding: ItemSettingsSettingBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(setting: SettingItem) {
            binding.apply {
                tvTitle.text = setting.title
                ivIcon.setImageResource(setting.icon)
                
                // Configurar switch o flecha
                if (setting.hasSwitch) {
                    switchSetting.visibility = android.view.View.VISIBLE
                    ivArrow.visibility = android.view.View.GONE
                    switchSetting.isChecked = setting.switchState
                } else if (setting.hasArrow) {
                    switchSetting.visibility = android.view.View.GONE
                    ivArrow.visibility = android.view.View.VISIBLE
                } else {
                    switchSetting.visibility = android.view.View.GONE
                    ivArrow.visibility = android.view.View.GONE
                }
                
                root.setOnClickListener {
                    setting.onClick()
                }
            }
        }
    }
}