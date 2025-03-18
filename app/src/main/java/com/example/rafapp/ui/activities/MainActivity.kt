package com.example.rafapp.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.rafapp.R
import com.example.rafapp.models.WeatherStation
import com.example.rafapp.ui.adapters.ViewPagerAdapter
import com.example.rafapp.ui.fragments.WeatherInfoFragment
import com.example.rafapp.viewmodel.WeatherStationViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var stationSpinner: Spinner
    private lateinit var btnLogout: Button
    private lateinit var sharedPref: SharedPreferences

    private lateinit var tempTextView: TextView
    private lateinit var lastComTextView: TextView
    private lateinit var tempMinTextView: TextView
    private lateinit var tempMaxTextView: TextView

    private var weatherStations: List<WeatherStation> = listOf()
    private val viewModel: WeatherStationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)

        if (sharedPref.getString("auth_token", null) == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        viewPager = findViewById(R.id.viewPager)
        stationSpinner = findViewById(R.id.stationSpinner)
        btnLogout = findViewById(R.id.btnLogout)

        tempTextView = findViewById(R.id.tempTextView)
        lastComTextView = findViewById(R.id.lastComTextView)
        tempMinTextView = findViewById(R.id.tempMinTextView)
        tempMaxTextView = findViewById(R.id.tempMaxTextView)

        viewPager.adapter = ViewPagerAdapter(this)

        btnLogout.setOnClickListener {
            logout()
        }

        setupObservers()
        fetchWeatherData()
    }

    /** 🔹 Observadores para recibir datos de la ViewModel **/
    private fun setupObservers() {
        viewModel.weatherStations.observe(this) { stations ->
            if (stations.isNotEmpty()) {
                weatherStations = stations
                setupStationSpinner(weatherStations)
            }
        }

        viewModel.selectedStationData.observe(this) { station ->
            updateMainUI(station)
            updateWeatherFragment(station)
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            Log.e("MainActivity", "Error: $errorMessage")
        }
    }

    /** 🔹 Llamada para obtener estaciones desde la API **/
    private fun fetchWeatherData() {
        viewModel.fetchWeatherStations(this)
    }

    /** 🔹 Configurar el Spinner con las estaciones obtenidas **/
    private fun setupStationSpinner(weatherStations: List<WeatherStation>) {
        val stationNames = weatherStations.map { it.name?.custom ?: "Desconocida" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stationNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stationSpinner.adapter = adapter

        stationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStationId = weatherStations[position].id
                viewModel.fetchStationData(this@MainActivity, selectedStationId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /** 🔹 Actualiza la UI principal con los datos de la estación seleccionada **/
    private fun updateMainUI(weatherStation: WeatherStation) {
        val meta = weatherStation.meta

        tempTextView.text = "${meta?.airTemp ?: "--"} °C"
        lastComTextView.text = weatherStation.dates?.lastCommunication ?: "--/--/----"
        tempMinTextView.text = "Min: ${meta?.rain24h?.vals?.minOrNull() ?: "--"}°"
        tempMaxTextView.text = "Max: ${meta?.rain24h?.vals?.maxOrNull() ?: "--"}°"
    }

    /** 🔹 Envía los datos de la estación seleccionada al Fragment **/
    private fun updateWeatherFragment(weatherStation: WeatherStation) {
        val fragment = getFragmentByPosition(0) as? WeatherInfoFragment
        fragment?.updateWeatherData(weatherStation)
    }

    /** 🔹 Encuentra el fragmento en el ViewPager **/
    private fun getFragmentByPosition(position: Int): Fragment? {
        return supportFragmentManager.findFragmentByTag("f$position")
    }

    /** 🔹 Cierra sesión **/
    private fun logout() {
        with(sharedPref.edit()) {
            remove("auth_token")
            apply()
        }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
