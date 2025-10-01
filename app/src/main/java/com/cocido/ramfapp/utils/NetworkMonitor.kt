package com.cocido.ramfapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Monitor de conectividad de red con mejores prácticas
 */
class NetworkMonitor(private val context: Context) {
    private val TAG = "NetworkMonitor"
    
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected
    
    private val _connectionType = MutableLiveData<ConnectionType>()
    val connectionType: LiveData<ConnectionType> = _connectionType
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    /**
     * Tipos de conexión disponibles
     */
    enum class ConnectionType {
        NONE,
        WIFI,
        CELLULAR,
        ETHERNET,
        VPN,
        UNKNOWN
    }
    
    /**
     * Iniciar el monitoreo de red
     */
    fun startMonitoring() {
        Log.d(TAG, "Starting network monitoring")
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available: $network")
                updateConnectionStatus()
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost: $network")
                updateConnectionStatus()
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d(TAG, "Network capabilities changed for: $network")
                updateConnectionStatus()
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
        
        // Verificar estado inicial
        updateConnectionStatus()
    }
    
    /**
     * Detener el monitoreo de red
     */
    fun stopMonitoring() {
        Log.d(TAG, "Stopping network monitoring")
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
            networkCallback = null
        }
    }
    
    /**
     * Actualizar el estado de conexión
     */
    private fun updateConnectionStatus() {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val connectionType = determineConnectionType(networkCapabilities)
        
        _isConnected.postValue(isConnected)
        _connectionType.postValue(connectionType)
        
        Log.d(TAG, "Network status updated: connected=$isConnected, type=$connectionType")
    }
    
    /**
     * Determinar el tipo de conexión
     */
    private fun determineConnectionType(networkCapabilities: NetworkCapabilities?): ConnectionType {
        return when {
            networkCapabilities == null -> ConnectionType.NONE
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionType.VPN
            else -> ConnectionType.UNKNOWN
        }
    }
    
    /**
     * Verificar si hay conexión a internet
     */
    fun hasInternetConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
    
    /**
     * Verificar si la conexión es WiFi
     */
    fun isWiFiConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }
    
    /**
     * Verificar si la conexión es celular
     */
    fun isCellularConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    }
    
    /**
     * Obtener información detallada de la conexión
     */
    fun getConnectionInfo(): ConnectionInfo {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        return ConnectionInfo(
            isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true,
            connectionType = determineConnectionType(networkCapabilities),
            isMetered = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) != true,
            isRoaming = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING) != true
        )
    }
}

/**
 * Información detallada de la conexión
 */
data class ConnectionInfo(
    val isConnected: Boolean,
    val connectionType: NetworkMonitor.ConnectionType,
    val isMetered: Boolean,
    val isRoaming: Boolean
)

