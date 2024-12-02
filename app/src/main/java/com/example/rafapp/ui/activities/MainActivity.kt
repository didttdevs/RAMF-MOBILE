package com.example.rafapp.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rafapp.R
import com.example.rafapp.databinding.ActivityMainBinding
import com.example.rafapp.ui.fragments.DashboardFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //binding.refreshButton.setOnClickListener {
        //    refreshData()
        //}

        //binding.btnLogout.setOnClickListener {
        //    logout()
        //}

        //if (savedInstanceState == null) {
        //    supportFragmentManager.beginTransaction()
        //        .replace(R.id.fragmentContainer, DashboardFragment())
        //        .commitNow()
        //}
    }

    private fun refreshData() {
        //val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        //if (fragment is DashboardFragment) {
        //    fragment.refreshData()
        //}
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    fun updateWeatherSummary(lastComm: String, temp: String, humidity: String, maxTemp: String, minTemp: String) {
        binding.tvLastCommunication.text = "Última comunicación: $lastComm"
        binding.temperatureTextView.text = "${temp}°C"
        binding.humidityTextView.text = "Humedad: $humidity%"
        binding.maxTemperatureTextView.text = "Máx: ${maxTemp}°"
        binding.minTemperatureTextView.text = "Min: ${minTemp}°"
    }
}