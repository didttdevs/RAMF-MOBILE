package com.cocido.ramfapp.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidades profesionales para manejo de rangos de fechas
 * Siguiendo clean code y mejores prácticas
 */
object DateRangeUtils {

    /**
     * Formatos de fecha estandarizados
     */
    object DateFormat {
        const val API_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val API_DATE_ONLY = "yyyy-MM-dd"
        const val DISPLAY_FORMAT = "dd/MM/yyyy"
        const val DISPLAY_WITH_TIME = "dd/MM/yyyy HH:mm"
        const val DISPLAY_SHORT = "dd MMM"
    }

    /**
     * Rangos predefinidos de fechas
     */
    enum class DateRangePreset(
        val displayName: String,
        val hours: Int? = null,
        val days: Int? = null
    ) {
        LAST_24_HOURS("Últimas 24 horas", hours = 24),
        LAST_48_HOURS("Últimas 48 horas", hours = 48),
        LAST_7_DAYS("Últimos 7 días", days = 7),
        LAST_15_DAYS("Últimos 15 días", days = 15),
        LAST_30_DAYS("Últimos 30 días", days = 30),
        CURRENT_MONTH("Mes actual"),
        LAST_MONTH("Mes pasado"),
        CURRENT_YEAR("Año actual"),
        CUSTOM("Personalizado");

        /**
         * Calcula el rango de fechas para este preset
         */
        fun getDateRange(): Pair<Date, Date> {
            val calendar = Calendar.getInstance()
            val endDate = calendar.time

            when (this) {
                LAST_24_HOURS, LAST_48_HOURS -> {
                    hours?.let {
                        calendar.add(Calendar.HOUR_OF_DAY, -it)
                    }
                }
                LAST_7_DAYS, LAST_15_DAYS, LAST_30_DAYS -> {
                    days?.let {
                        calendar.add(Calendar.DAY_OF_YEAR, -it)
                    }
                }
                CURRENT_MONTH -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                }
                LAST_MONTH -> {
                    // Último día del mes pasado
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    
                    val lastMonthEnd = calendar.time
                    
                    // Primer día del mes pasado
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    
                    return Pair(calendar.time, lastMonthEnd)
                }
                CURRENT_YEAR -> {
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                }
                CUSTOM -> {
                    // Para custom, retornar los últimos 7 días por defecto
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                }
            }

            val startDate = calendar.time
            return Pair(startDate, endDate)
        }
    }

    /**
     * Data class para almacenar un rango de fechas
     */
    data class DateRange(
        val startDate: Date,
        val endDate: Date,
        val preset: DateRangePreset = DateRangePreset.CUSTOM
    ) {
        /**
         * Formatea el rango para mostrar en UI
         */
        fun toDisplayString(): String {
            val formatter = SimpleDateFormat(DateFormat.DISPLAY_FORMAT, Locale.getDefault())
            return "${formatter.format(startDate)} - ${formatter.format(endDate)}"
        }

        /**
         * Formatea para enviar a la API
         */
        fun toApiString(): Pair<String, String> {
            val formatter = SimpleDateFormat(DateFormat.API_DATE_ONLY, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            return Pair(formatter.format(startDate), formatter.format(endDate))
        }

        /**
         * Formatea para enviar a la API con hora
         */
        fun toApiStringWithTime(): Pair<String, String> {
            val formatter = SimpleDateFormat(DateFormat.API_FORMAT, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            return Pair(formatter.format(startDate), formatter.format(endDate))
        }

        /**
         * Valida que el rango sea válido
         */
        fun isValid(): Boolean {
            return startDate.before(endDate) || startDate == endDate
        }

        /**
         * Calcula la duración en días
         */
        fun getDurationInDays(): Int {
            val diff = endDate.time - startDate.time
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }

        /**
         * Verifica si el rango es mayor a un número de días
         */
        fun isLongerThan(days: Int): Boolean {
            return getDurationInDays() > days
        }

        companion object {
            /**
             * Crea un DateRange desde un preset
             */
            fun fromPreset(preset: DateRangePreset): DateRange {
                val (start, end) = preset.getDateRange()
                return DateRange(start, end, preset)
            }

            /**
             * Crea un DateRange desde strings de la API
             */
            fun fromApiStrings(startStr: String, endStr: String): DateRange? {
                return try {
                    val formatter = SimpleDateFormat(DateFormat.API_DATE_ONLY, Locale.getDefault())
                    formatter.timeZone = TimeZone.getTimeZone("UTC")
                    val start = formatter.parse(startStr) ?: return null
                    val end = formatter.parse(endStr) ?: return null
                    DateRange(start, end)
                } catch (e: Exception) {
                    null
                }
            }

            /**
             * Obtiene el rango por defecto (últimas 24 horas)
             */
            fun getDefault(): DateRange {
                return fromPreset(DateRangePreset.LAST_24_HOURS)
            }
        }
    }

    /**
     * Valida que una fecha esté en un rango válido
     */
    fun isDateInValidRange(date: Date, maxFutureDays: Int = 0, maxPastYears: Int = 5): Boolean {
        val calendar = Calendar.getInstance()
        val now = calendar.time

        // Validar futuro
        if (maxFutureDays > 0) {
            calendar.add(Calendar.DAY_OF_YEAR, maxFutureDays)
            if (date.after(calendar.time)) {
                return false
            }
        } else if (date.after(now)) {
            return false
        }

        // Validar pasado
        calendar.time = now
        calendar.add(Calendar.YEAR, -maxPastYears)
        if (date.before(calendar.time)) {
            return false
        }

        return true
    }

    /**
     * Formatea una fecha para mostrar en UI
     */
    fun formatDateForDisplay(date: Date, includeTime: Boolean = false): String {
        val format = if (includeTime) DateFormat.DISPLAY_WITH_TIME else DateFormat.DISPLAY_FORMAT
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Formatea una fecha para la API
     */
    fun formatDateForApi(date: Date, includeTime: Boolean = true): String {
        val format = if (includeTime) DateFormat.API_FORMAT else DateFormat.API_DATE_ONLY
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }

    /**
     * Parsea una fecha desde string de la API
     */
    fun parseDateFromApi(dateStr: String): Date? {
        return try {
            // Intentar con formato completo primero
            var formatter = SimpleDateFormat(DateFormat.API_FORMAT, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.parse(dateStr) ?: run {
                // Intentar con formato solo fecha
                formatter = SimpleDateFormat(DateFormat.API_DATE_ONLY, Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                formatter.parse(dateStr)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene el inicio del día para una fecha
     */
    fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Obtiene el fin del día para una fecha
     */
    fun getEndOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    /**
     * Calcula el número de días entre dos fechas
     */
    fun daysBetween(start: Date, end: Date): Int {
        val diff = end.time - start.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * Verifica si dos fechas son el mismo día
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Verifica si una fecha es hoy
     */
    fun isToday(date: Date): Boolean {
        return isSameDay(date, Date())
    }

    /**
     * Verifica si una fecha es ayer
     */
    fun isYesterday(date: Date): Boolean {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.time
        return isSameDay(date, yesterday)
    }
}

