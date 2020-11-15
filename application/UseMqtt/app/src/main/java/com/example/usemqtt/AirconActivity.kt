package com.example.usemqtt

import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_aircon.*
import kotlinx.android.synthetic.main.activity_light.*
import kotlinx.coroutines.delay
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer

class AirconActivity : AppCompatActivity() {
    private var timerTask: Timer? = null
    lateinit var mqttClient: Mqtt
    var manualtemp = 27
    var manualhumi = 20
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aircon)
        mqttClient = Mqtt(this, SERVER_URI)
        try {
            // mqttClient.setCallback { topic, message ->}
            mqttClient.setCallback(::onReceived)
            mqttClient.connect(arrayOf<String>(
                    "home/livingroom/temp",
                    "home/livingroom/humi"
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        airconswitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                mqttClient.publish("home/livingroom/manualstate","1")
                manuaircon()
            } else {
                // The toggle is disabled
                mqttClient.publish("home/livingroom/manualstate","0")
                timerTask?.cancel()
            }
        }
        humiup.setOnClickListener(){
            manualhumi += 1
            curhumi.text = manualhumi.toString()
        }
        humidown.setOnClickListener(){
            manualhumi -= 1
            curhumi.text = manualhumi.toString()
        }
        humibar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                manualhumi = i+30
                curhumi.text = manualhumi.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tempup.setOnClickListener(){
            manualtemp += 1
            curtemp.text = manualtemp.toString()
        }
        tempdown.setOnClickListener(){
            manualtemp -= 1
            curtemp.text = manualtemp.toString()
        }

        tempbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                manualtemp = i+16
                curtemp.text = manualtemp.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    fun onReceived(topic: String, message: MqttMessage) {
        // 토픽 수신 처리
        val msg = String(message.payload)
        if (topic == "home/livingroom/temp") {
            roomhumi.text = msg
        }
        else {
            roomtemp.text = msg
        }
    }

    fun manuaircon() {
        timerTask=timer(period = 2500){
            mqttClient.publish("home/livingroom/manual/humi", manualhumi.toString() )
            Timer().schedule(1000){
                    mqttClient.publish("home/livingroom/manual/temp", manualtemp.toString() )
            }

        }
    }
}