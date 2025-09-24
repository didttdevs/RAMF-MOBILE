package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de la API para estaciones meteorológicas
 */
data class StationsResponse(
    @SerializedName("data") val data: List<WeatherStation>,
    @SerializedName("meta") val meta: StationsMeta,
    @SerializedName("links") val links: StationsLinks
)

/**
 * Metadatos de la respuesta de estaciones
 */
data class StationsMeta(
    @SerializedName("itemsPerPage") val itemsPerPage: Int,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("sortBy") val sortBy: List<List<String>>
)

/**
 * Enlaces de la respuesta de estaciones
 */
data class StationsLinks(
    @SerializedName("current") val current: String
)
