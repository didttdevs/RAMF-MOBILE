package com.example.rafapp.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatDelegate
import com.example.rafapp.R
import com.example.rafapp.models.WeatherStation
import com.example.rafapp.network.RetrofitClient
import com.example.rafapp.ui.adapters.ViewPagerAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var stationSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        viewPager = findViewById(R.id.viewPager)
        stationSpinner = findViewById(R.id.stationSpinner)

        viewPager.adapter = ViewPagerAdapter(this)

        fetchWeatherData()
    }

    private fun fetchWeatherData() {
        RetrofitClient.instance.getWeatherStations()
            .enqueue(object : Callback<List<WeatherStation>> {
                override fun onFailure(call: Call<List<WeatherStation>>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<List<WeatherStation>>, response: Response<List<WeatherStation>>) {
                    if (response.isSuccessful) {
                        val weatherStations = response.body() ?: emptyList()
                        setupStationSpinner(weatherStations)
                    }
                }
            })
    }

    private fun setupStationSpinner(weatherStations: List<WeatherStation>) {
        val stationNames = weatherStations.map { "Estación ${it.name?.custom}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stationNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stationSpinner.adapter = adapter
    }
}
