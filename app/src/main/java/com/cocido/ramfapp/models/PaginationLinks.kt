package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

data class PaginationLinks(
    @SerializedName("first")
    val first: String? = null,
    
    @SerializedName("last")
    val last: String? = null,
    
    @SerializedName("prev")
    val prev: String? = null,
    
    @SerializedName("next")
    val next: String? = null
)





