package com.cocido.ramfapp.models

data class WeatherDataResponseWrapper(
    val data: List<WeatherData>,
    val meta: MetaData?,
    val links: Map<String, String>?
)
