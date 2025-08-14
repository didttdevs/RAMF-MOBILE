package com.example.rafapp.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rafapp.R
import com.example.rafapp.databinding.DialogStationSelectorBinding
import com.example.rafapp.models.WeatherStation
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StationSelectorDialog : DialogFragment() {
    
    private var _binding: DialogStationSelectorBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: StationAdapter
    private var stations: List<WeatherStation> = emptyList()
    private var selectedStationId: String = ""
    private var onStationSelected: ((WeatherStation) -> Unit)? = null
    
    companion object {
        fun newInstance(
            stations: List<WeatherStation>,
            selectedStationId: String,
            onStationSelected: (WeatherStation) -> Unit
        ): StationSelectorDialog {
            return StationSelectorDialog().apply {
                this.stations = stations
                this.selectedStationId = selectedStationId
                this.onStationSelected = onStationSelected
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogStationSelectorBinding.inflate(layoutInflater)
        
        setupRecyclerView()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Seleccionar Estación")
            .setView(binding.root)
            .setNegativeButton("Cancelar", null)
            .create()
    }
    
    private fun setupRecyclerView() {
        adapter = StationAdapter(stations, selectedStationId) { station ->
            onStationSelected?.invoke(station)
            dismiss()
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StationSelectorDialog.adapter
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private class StationAdapter(
        private val stations: List<WeatherStation>,
        private val selectedStationId: String,
        private val onStationClick: (WeatherStation) -> Unit
    ) : RecyclerView.Adapter<StationAdapter.StationViewHolder>() {
        
        class StationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            // Implement view holder if needed
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_single_choice, parent, false)
            return StationViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
            val station = stations[position]
            // Implement binding logic
        }
        
        override fun getItemCount(): Int = stations.size
    }
}