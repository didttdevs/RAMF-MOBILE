package com.example.rafapp.ui.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.rafapp.R

class MapActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnNotifications: ImageButton
    private lateinit var filterContainer: LinearLayout
    private lateinit var tvSelectedParameter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnNotifications = findViewById(R.id.btnNotifications)
        filterContainer = findViewById(R.id.filterContainer)
        tvSelectedParameter = findViewById(R.id.tvSelectedParameter)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnNotifications.setOnClickListener {
            // TODO: Implementar notificaciones
        }

        filterContainer.setOnClickListener {
            showParameterSelectionDialog()
        }
    }

    private fun showParameterSelectionDialog() {
        val parameters = arrayOf(
            "Air temperature",
            "Relative humidity", 
            "Wind speed",
            "Precipitation",
            "Solar radiation",
            "Soil moisture"
        )

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Parameter")
        builder.setItems(parameters) { dialog, which ->
            tvSelectedParameter.text = parameters[which]
            // TODO: Actualizar mapa con el nuevo parámetro
            dialog.dismiss()
        }
        builder.show()
    }

    // TODO: Implementar funciones del mapa cuando se configure Google Maps
    /*
    private fun setupGoogleMaps() {
        // Configurar Google Maps
    }

    private fun loadWeatherStations() {
        // Cargar estaciones meteorológicas en el mapa
    }

    private fun addMarkersToMap(parameter: String) {
        // Agregar marcadores con datos del parámetro seleccionado
    }
    */
}