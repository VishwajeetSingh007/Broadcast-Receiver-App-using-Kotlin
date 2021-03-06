@file:Suppress("DEPRECATION")

package com.example.checkinternetconnection

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.os.Build
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData

class NetworkConnection(private val context: Context): LiveData<Boolean>() {
    private var connectivityManager : ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var networkCallback : ConnectivityManager.NetworkCallback
    override fun onActive() {
        super.onActive()
        UpdateConnection()
        when{Build.VERSION.SDK_INT>= Build.VERSION_CODES.N -> {
            connectivityManager.registerDefaultNetworkCallback(ConnectivityManagerCallBack())
        }
            Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP -> {
                lollipopNetworkRequest()
            }
            else -> {
                context.registerReceiver(
                    networkReceiver,
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )
            }

        }
    }

    override fun onInactive() {
        super.onInactive()
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            connectivityManager.unregisterNetworkCallback(ConnectivityManagerCallBack())
        }else{
            context.unregisterReceiver(networkReceiver)
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lollipopNetworkRequest(){
        val requestBuilder = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        connectivityManager.registerNetworkCallback(
            requestBuilder.build(),
            ConnectivityManagerCallBack()
        )
    }
    private fun ConnectivityManagerCallBack(): ConnectivityManager.NetworkCallback{
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            networkCallback = object : ConnectivityManager.NetworkCallback(){
                override fun onLost(network: Network) {
                    super.onLost(network)
                    postValue(false)
                }

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    postValue(true)
                }
            }
            return networkCallback
        } else {
            throw IllegalAccessError("Error")
        }
        }
    private val networkReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            TODO("Not yet implemented")
        }
    }
    private fun UpdateConnection(){
        val activeNetwork : NetworkInfo? = connectivityManager.activeNetworkInfo
        postValue(activeNetwork?.isConnected == true)

    }
}