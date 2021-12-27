package com.iodaniel.mobileclass.shared_classes

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class InternetConnection(val context: Context) {

    private lateinit var connectivityClass: ConnectivityClass

    fun checkInternetConnection(connectivityClass: ConnectivityClass){
        this.connectivityClass = connectivityClass
        checkConnectionManual()
    }

    fun checkConnectionManual(){
        when {
            isOnline() -> connectivityClass.connected()
            else -> connectivityClass.disConnected()
        }
    }

    inner class TimeTaskClass() : TimerTask() {
        override fun run() {
            println("TIMER RAN *************************")
            when {
                isOnline() -> connectivityClass.connected()
                else -> connectivityClass.disConnected()
            }
        }
    }

    private fun isOnline(): Boolean {
            val url = URL("https://www.google.com")
            val urlConnection = url.openConnection() as HttpURLConnection
            try{
                val inputStream = urlConnection.responseCode
                //println("TIMER socket ************************* Online $inputStream")
                return true
            } catch (e: Exception){
                //println("Error ********************* ${e.printStackTrace()}")
                urlConnection.disconnect()
                return false
            } finally {
                urlConnection.disconnect()
            }
    }

    private fun recurse() {
        Timer().scheduleAtFixedRate(TimeTaskClass() as TimerTask, 0, 5000)
    }
    interface ConnectivityClass {
        fun connected()
        fun disConnected()
    }
}