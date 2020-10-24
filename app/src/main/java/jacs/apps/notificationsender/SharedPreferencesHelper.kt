package jacs.apps.notificationsender

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper {

    companion object{
        val key = "jacs.apps.notificationsender.PREFERENCESKEY"

        fun getDeviceName(context: Context) : String{
            var preferences = context.getSharedPreferences(key,Context.MODE_PRIVATE)
            return preferences.getString("device_name","none").toString()
        }
        fun setDeviceName(context: Context, name: String){
            var preferences = context.getSharedPreferences(key,Context.MODE_PRIVATE)
            var editor = preferences.edit()
            editor.putString("device_name",name)
            editor.apply()
        }
        fun getDeviceIPAddress(context: Context) : String{
            var preferences = context.getSharedPreferences(key,Context.MODE_PRIVATE)
            return preferences.getString("ip_address","none").toString()
        }
        fun setDeviceIPAddress(context: Context, ip: String){
            var preferences = context.getSharedPreferences(key,Context.MODE_PRIVATE)
            var editor = preferences.edit()
            editor.putString("ip_address",ip)
            editor.apply()
        }
    }
}