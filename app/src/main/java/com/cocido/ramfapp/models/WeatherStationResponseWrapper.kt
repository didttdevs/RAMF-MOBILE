package com.cocido.ramfapp.models

data class WeatherStationResponseWrapper(
    val data: List<WeatherStation>,
    val meta: MetaData?,
    val links: Map<String, String>?
)
