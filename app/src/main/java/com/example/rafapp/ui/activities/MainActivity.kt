package com.example.rafapp.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.rafapp.R
import com.example.rafapp.models.User
import com.example.rafapp.models.WeatherDataLastDayResponse
import com.example.rafapp.models.WeatherStation
import com.example.rafapp.ui.adapters.ViewPagerAdapter
import com.example.rafapp.ui.fragments.WeatherInfoFragment
import com.example.rafapp.viewmodel.WeatherStationViewModel
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var stationSpinner: Spinner
    private lateinit var sharedPref: SharedPreferences
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var tempTextView: TextView
    private lateinit var lastComTextView: TextView
    private lateinit var tempMinTextView: TextView
    private lateinit var tempMaxTextView: TextView

    private var weatherStations: List<WeatherStation> = listOf()
    private var selectedStationPosition = 0  // Guardar la estación seleccionada
    private val viewModel: WeatherStationViewModel by viewModels()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private lateinit var navHeaderProfileImage: ImageView
    private lateinit var navHeaderName: TextView
    private lateinit var navHeaderRoleUser: TextView

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

        // Inicia las vistas
        viewPager = findViewById(R.id.viewPager)
        stationSpinner = findViewById(R.id.stationSpinner)
        swipeRefreshLayout = findViewById(R.id.swiperefresh)

        tempTextView = findViewById(R.id.tempTextView)
        lastComTextView = findViewById(R.id.lastComTextView)
        tempMinTextView = findViewById(R.id.tempMinTextView)
        tempMaxTextView = findViewById(R.id.tempMaxTextView)

        viewPager.adapter = ViewPagerAdapter(this)

        // Configurar el menú lateral
        val headerView = navigationView.getHeaderView(0)
        navHeaderProfileImage = headerView.findViewById(R.id.navHeaderProfileImage)
        navHeaderName = headerView.findViewById(R.id.navHeaderUsername)
        navHeaderRoleUser = headerView.findViewById(R.id.navHeaderDetailsUser)

        // Obtener los datos del usuario
        val user = getUserDataFromSharedPreferences()

        if (user != null) {
            navHeaderName.text = "${user.firstName} ${user.lastName}"
            navHeaderRoleUser.text = user.role  // Mostrar el tipo de usuario

            Glide.with(this)
                .load(user.avatar)  // URL de la imagen de perfil
                .circleCrop()  // Esto hará que la imagen sea redonda
                .into(navHeaderProfileImage)
        } else {
            Log.e("MainActivity", "Error: No se encontraron datos de usuario")
        }

        // Abre el drawer cuando el botón de hamburguesa es presionado
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Configura el listener para el menú
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    fetchWeatherData()
                    true
                }
                R.id.nav_profile -> {
                    true
                }
                R.id.nav_settings -> {
                    true
                }
                R.id.nav_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            fetchWeatherData()
        }

        setupObservers()
        fetchWeatherData()  // Llamada para obtener las estaciones por primera vez
    }

    private fun setupObservers() {
        viewModel.weatherStations.observe(this) { stations ->
            if (stations.isNotEmpty()) {
                weatherStations = stations
                setupStationSpinner(weatherStations)
                stationSpinner.setSelection(selectedStationPosition)
            }
        }

        viewModel.selectedStationData.observe(this) { station ->
            updateMainUI(station)
            updateWeatherFragment(station)
        }

        viewModel.temperatureMaxMin.observe(this) { tempMaxMin ->
            // Mostrar las temperaturas máximas y mínimas en la UI
            tempMinTextView.text = "Min: ${tempMaxMin.min ?: "--"}°"
            tempMaxTextView.text = "Max: ${tempMaxMin.max ?: "--"}°"
        }

        viewModel.weatherData.observe(this) { weatherData ->
            // Actualizamos la UI con los datos del clima del último día
            updateWeatherFragmentLastDay(weatherData)
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            Log.e("MainActivity", "Error: $errorMessage")
        }
    }

    private fun fetchWeatherData() {
        swipeRefreshLayout.isRefreshing = true
        viewModel.fetchWeatherStations(this)
        swipeRefreshLayout.isRefreshing = false
    }

    private fun setupStationSpinner(weatherStations: List<WeatherStation>) {
        val stationNames = weatherStations.map { it.name?.custom ?: "Desconocida" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stationNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stationSpinner.adapter = adapter

        stationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedStationPosition = position
                val selectedStationId = weatherStations[position].id
                viewModel.fetchStationData(this@MainActivity, selectedStationId)
                // Obtener las temperaturas max y min
                viewModel.fetchTemperatureMaxMin(this@MainActivity, selectedStationId)
                // Obtener los datos del último día
                viewModel.fetchWeatherDataLastDay(this@MainActivity, selectedStationId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateMainUI(weatherStation: WeatherStation) {
        val meta = weatherStation.meta
        tempTextView.text = "${meta?.airTemp ?: "--"} °C"
        lastComTextView.text = weatherStation.dates?.lastCommunication ?: "--/--/----"
    }

    private fun updateWeatherFragment(weatherStation: WeatherStation) {
        val fragment = getFragmentByPosition(0) as? WeatherInfoFragment
        fragment?.updateWeatherData(weatherStation)
    }

    // Función para actualizar los datos del clima del último día
    private fun updateWeatherFragmentLastDay(weatherData: List<WeatherDataLastDayResponse>) {
        val fragment = getFragmentByPosition(0) as? WeatherInfoFragment
        // Si la lista no está vacía, pasamos el primer item
        weatherData.firstOrNull()?.let {
            fragment?.updateWeatherDataLastDay(it)  // Pasamos solo un objeto, no la lista
        }
    }

    private fun getFragmentByPosition(position: Int): Fragment? {
        return supportFragmentManager.findFragmentByTag("f$position")
    }

    private fun logout() {
        with(sharedPref.edit()) {
            remove("auth_token")
            apply()
        }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun getUserDataFromSharedPreferences(): User? {
        val userJson = sharedPref.getString("user_data", null) ?: return null
        return Gson().fromJson(userJson, User::class.java)
    }
}
