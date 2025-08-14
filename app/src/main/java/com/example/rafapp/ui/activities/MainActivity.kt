package com.example.rafapp.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.button.MaterialButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.rafapp.R
import com.example.rafapp.models.Sensors
import com.example.rafapp.models.User
import com.example.rafapp.models.WeatherData
import com.example.rafapp.models.WeatherStation
import com.example.rafapp.ui.adapters.ViewPagerAdapter
import com.example.rafapp.viewmodel.WeatherStationViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var stationSpinner: MaterialAutoCompleteTextView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var tempTextView: TextView
    private lateinit var lastComTextView: TextView
    private lateinit var tempMinTextView: TextView
    private lateinit var tempMaxTextView: TextView

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private lateinit var navHeaderProfileImage: ImageView
    private lateinit var navHeaderName: TextView
    private lateinit var navHeaderRoleUser: TextView

    private val viewModel: WeatherStationViewModel by viewModels()
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var weatherStations: List<WeatherStation> = listOf()
    private var selectedStationPosition = 0

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

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.nav_view)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        stationSpinner = findViewById(R.id.stationSpinner)
        swipeRefreshLayout = findViewById(R.id.swiperefresh)
        tempTextView = findViewById(R.id.tempTextView)
        lastComTextView = findViewById(R.id.lastComTextView)
        tempMinTextView = findViewById(R.id.tempMinTextView)
        tempMaxTextView = findViewById(R.id.tempMaxTextView)

        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Datos" else "Gráficos"
        }.attach()

        val headerView = navigationView.getHeaderView(0)
        navHeaderProfileImage = headerView.findViewById(R.id.navHeaderProfileImage)
        navHeaderName = headerView.findViewById(R.id.navHeaderUsername)
        navHeaderRoleUser = headerView.findViewById(R.id.navHeaderDetailsUser)

        val user = getUserDataFromSharedPreferences()
        user?.let {
            navHeaderName.text = "${it.firstName} ${it.lastName}"
            navHeaderRoleUser.text = it.role
            Glide.with(this).load(it.avatar).circleCrop().into(navHeaderProfileImage)
        }

        findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        swipeRefreshLayout.setOnRefreshListener { fetchWeatherData() }

        setupObservers()
        fetchWeatherData()
    }

    private fun setupObservers() {
        viewModel.weatherStations.observe(this) { stations ->
            Log.d("MainActivity", "weatherStations observer called with ${stations.size} stations")
            if (stations.isNotEmpty()) {
                weatherStations = stations
                setupStationSpinner(weatherStations)
                
                // Cargar datos de la primera estación por defecto
                if (stations.isNotEmpty()) {
                    val firstStation = stations[0]
                    val stationName = firstStation.id // En la nueva API, id es el stationName
                    Log.d("MainActivity", "Loading data for first station: ${firstStation.name} (${stationName})")
                    viewModel.fetchStationData(stationName)
                    viewModel.fetchTemperatureMaxMin(stationName)
                    viewModel.fetchWeatherDataLastDay(stationName)
                    stationSpinner.setText(firstStation.name ?: "Desconocida", false)
                }
            } else {
                Log.e("MainActivity", "No weather stations received")
            }
        }

        viewModel.selectedStationData.observe(this) { station ->
            updateMainUI(station)
            // El WeatherInfoFragment ya observa al ViewModel y se actualiza solo.
        }

        viewModel.temperatureMaxMin.observe(this) { tempMaxMin ->
            tempMinTextView.text = "Min: ${tempMaxMin.min ?: "--"}°"
            tempMaxTextView.text = "Max: ${tempMaxMin.max ?: "--"}°"
        }

        // Usamos esta lista para refrescar temperatura actual y el ícono de estado del cielo
        viewModel.weatherDataLastDay.observe(this) { weatherDataList ->
            val latest = weatherDataList.firstOrNull()
            latest?.let { wd ->
                // Temperatura actual (si está disponible)
                wd.sensors.hcAirTemperature?.avg?.let { t ->
                    tempTextView.text = String.format(Locale.getDefault(), "%.1f °C", t)
                }
                // Determinar condición del cielo con sensores (si hay radiación/humedad)
                val condition = determineSkyConditionFromSensors(wd.sensors, isDaytime = true)
                updateBackgroundAndIcon(condition, isDaytime = true)
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            Log.e("MainActivity", "Error: $errorMessage")
        }
    }

    private fun fetchWeatherData() {
        Log.d("MainActivity", "fetchWeatherData called")
        swipeRefreshLayout.isRefreshing = true
        viewModel.fetchWeatherStations()
        
        // También cargar una estación por defecto si no hay ninguna seleccionada
        if (weatherStations.isEmpty()) {
            Log.d("MainActivity", "Loading default station data")
            viewModel.fetchStationData("00213962") // stationName por defecto basado en API
            viewModel.fetchTemperatureMaxMin("00213962")
            viewModel.fetchWeatherDataLastDay("00213962")
        }
        
        swipeRefreshLayout.isRefreshing = false
    }

    private fun setupStationSpinner(stations: List<WeatherStation>) {
        val stationNames = stations.map { it.name ?: "Desconocida" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stationNames)
        stationSpinner.setAdapter(adapter)

        stationSpinner.setOnItemClickListener { _, _, position, _ ->
            selectedStationPosition = position
            val stationName = stations[position].id // En la nueva API, id contiene el stationName
            Log.d("MainActivity", "Selected station: ${stations[position].name} (${stationName})")
            viewModel.fetchStationData(stationName)
            viewModel.fetchTemperatureMaxMin(stationName)
            viewModel.fetchWeatherDataLastDay(stationName)
        }
    }

    private fun updateMainUI(station: WeatherStation) {
        // La nueva API no trae 'meta' en la estación; usamos lastCommunication y dejamos temp hasta que lleguen datos
        lastComTextView.text = station.lastCommunication ?: "--/--/----"
        if (tempTextView.text.isNullOrBlank()) tempTextView.text = "-- °C"
    }

    private fun getUserDataFromSharedPreferences(): User? {
        val userJson = sharedPref.getString("user_data", null) ?: return null
        return Gson().fromJson(userJson, User::class.java)
    }

    private fun logout() {
        sharedPref.edit().remove("auth_token").apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun determineSkyConditionFromSensors(sensors: Sensors, isDaytime: Boolean): String {
        val rh = sensors.hcRelativeHumidity?.avg ?: 0.0
        val sr = sensors.solarRadiation?.avg ?: 0.0
        return if (isDaytime) {
            when {
                sr > 500 -> "Despejado"
                sr in 100.0..500.0 -> "Parcialmente Nublado"
                sr <= 100 -> "Nublado"
                else -> "Muy Nublado"
            }
        } else {
            when {
                rh < 80.0 && sr > 1000 -> "Noche Despejada"
                rh >= 80.0 -> "Noche Parcialmente Nublada"
                else -> "Noche Muy Nublada"
            }
        }
    }

    private fun updateBackgroundAndIcon(weatherCondition: String, isDaytime: Boolean) {
        val cardView = findViewById<MaterialCardView>(R.id.card_temp)
        val weatherIcon = findViewById<ImageView>(R.id.weatherIcon)
        val iconRes = when (weatherCondition) {
            "Despejado" -> R.drawable.ic_weather_sunny
            "Parcialmente Nublado" -> R.drawable.ic_weather_sun_cloud
            "Nublado" -> R.drawable.ic_weather_cloud
            "Muy Nublado" -> R.drawable.ic_weather_very_cloudy
            "Noche Despejada" -> R.drawable.ic_weather_clear_night
            "Noche Parcialmente Nublada" -> R.drawable.ic_weather_cloudy_night
            else -> R.drawable.ic_weather_sunny
        }
        weatherIcon.setImageResource(iconRes)
        // Si querés cambiar fondo del card según condición, podés hacerlo acá con setCardBackgroundColor(...)
    }
}
