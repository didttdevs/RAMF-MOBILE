package com.cocido.ramfapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Modelo para elementos de configuración
 */
sealed class SettingsItem : Parcelable {
    
    @Parcelize
    data class Section(
        val title: String
    ) : SettingsItem()
    
    @Parcelize
    data class Setting(
        val title: String,
        val description: String,
        val icon: Int,
        val action: Action
    ) : SettingsItem()
    
    @Parcelize
    sealed class Action : Parcelable {
        @Parcelize
        data class Navigate(
            val onClick: () -> Unit
        ) : Action()
        
        @Parcelize
        data class Toggle(
            val isEnabled: Boolean,
            val onToggle: (Boolean) -> Unit
        ) : Action()
    }
}









