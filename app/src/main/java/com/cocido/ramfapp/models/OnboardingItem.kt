package com.cocido.ramfapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OnboardingItem(
    val title: String,
    val description: String,
    val imageRes: Int,
    val backgroundColor: Int
) : Parcelable









