package jacs.apps.notificationsender

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import su.litvak.chromecast.api.v2.*
import su.litvak.chromecast.api.v2.ChromeCasts.startDiscovery


class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView
    lateinit var listView: ListView
    lateinit var button: Button
    lateinit var arrayAdapter: ArrayAdapter<String>
    var devicesArray = ArrayList<String>()
    var ipArray = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textview)
        val deviceName = SharedPreferencesHelper.getDeviceName(context = this)
        val ipAddress = SharedPreferencesHelper.getDeviceIPAddress(context = this)
        val text = getString(R.string.device_name) + ": $deviceName\n" + getString(R.string.ip_address) + ": $ipAddress"
        textView.text = text
        listView = findViewById(R.id.listview)
        arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devicesArray)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener(){ _, _, position, _ ->
            val deviceName1 = devicesArray.get(position)
            val ipAddress1 = ipArray.get(position)
            SharedPreferencesHelper.setDeviceName(context = applicationContext, deviceName1)
            SharedPreferencesHelper.setDeviceIPAddress(context = applicationContext, ipAddress1)
            val text = getString(R.string.device_name) + ": $deviceName1\n" + getString(R.string.ip_address) + ": $ipAddress1"
            textView.text = text
            GlobalScope.launch{
                withContext(Dispatchers.IO) {
                    val url =
                        "https://translate.google.com/translate_tts?ie=UTF-8&q=I+am+currently+set+up+to+listen+to+notifications&tl=en-us&ttsspeed=1&total=1&idx=0&client=tw-ob&textlen=39&tk=272861.189055"
                    val chromecast = ChromeCast(ipAddress1)
                    val status: Status = chromecast.status
                    if (chromecast.isAppAvailable("CC1AD845") && !status.isAppRunning("CC1AD845")) {
                        val app: Application = chromecast.launchApp("CC1AD845")
                    }
                    chromecast.registerListener(ChromeCastSpontaneousEventListener {
                        val data = it.data
                        if(data is MediaStatus ){
                            Log.d("listener", data.toString())
                            if(data.idleReason == MediaStatus.IdleReason.FINISHED){
                                Log.d("listener", "everything stopped and shutdown")
                                chromecast.stopApp()
                                chromecast.disconnect()

                            }
                        }
                    })
                    chromecast.load(url)

                }}
        };


        button = findViewById(R.id.setListener)
        button.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        GlobalScope.launch{
            withContext(Dispatchers.IO) {

                try {


                    startDiscovery()
                    ChromeCasts.registerListener(object : ChromeCastsListener {
                        override fun newChromeCastDiscovered(chromeCast: ChromeCast?) {
                            if (chromeCast != null) {
                                Log.d("error", chromeCast.title)
                                runOnUiThread {
                                    devicesArray.add(chromeCast.title)
                                    ipArray.add(chromeCast.address)
                                    arrayAdapter.notifyDataSetChanged()
                                }

                            }
                        }

                        override fun chromeCastRemoved(chromeCast: ChromeCast?) {

                        }

                    })

                }catch (ex: Exception){
                    Log.d("error", ex.toString())
                }


            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ChromeCasts.stopDiscovery()
    }
}