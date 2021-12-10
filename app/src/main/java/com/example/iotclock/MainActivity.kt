package com.example.iotclock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*

const val SERVER_URI = "tcp://broker.hivemq.com:1883"
const val CLIENT_ID = "Android Client"

class MainActivity : AppCompatActivity() {

    private lateinit var client: MqttAndroidClient
    private lateinit var reconnect: Button
    private lateinit var clockSpinner: Spinner
    private lateinit var setClockButton: Button
    private lateinit var setAlarmButton: Button
    private lateinit var setMessageButton: Button
    private lateinit var messageEntry: EditText
    private lateinit var temperatureDisplay: TextView

    companion object {
        var customTime = false
        var setClock = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        client = MqttAndroidClient(this, SERVER_URI, CLIENT_ID)
        connect()

        reconnect = findViewById(R.id.reconnect)
        reconnect.setOnClickListener {
            connect()
        }

        messageEntry = findViewById(R.id.custom_text_entry)

        setMessageButton = findViewById(R.id.set_message_button)
        setMessageButton.setOnClickListener {
            publish("t/message", messageEntry.text.toString())
        }

        clockSpinner = findViewById(R.id.clock_spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.clock_set_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            clockSpinner.adapter = adapter
        }
        clockSpinner.onItemSelectedListener = ClockSpinner()

        setClockButton = findViewById(R.id.set_clock_button)
        setClockButton.setOnClickListener {
            setClock = true
            if (customTime) {
                TimePickerFragment().show(supportFragmentManager, "timePicker")
            }
            else {
                publish("t/time", DateFormat.format("HH:mm:ss", Date()).toString())
            }
        }

        setAlarmButton = findViewById(R.id.set_alarm)
        setAlarmButton.setOnClickListener {
            setClock = false
            TimePickerFragment().show(supportFragmentManager, "timePicker")
        }

        temperatureDisplay = findViewById(R.id.temperature_display)
    }

    private fun connect() {
        client = MqttAndroidClient(this, SERVER_URI, CLIENT_ID)

        client.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(CLIENT_ID, "Receive message: ${message.toString()} from topic: $topic")
                temperatureDisplay.text = "${message.toString()}°F"
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(CLIENT_ID, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d(CLIENT_ID, "Delivery complete to $SERVER_URI")
            }
        })

        val options = MqttConnectOptions()
        try {
            client.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(CLIENT_ID, "Connected!")
                    makeToast("Connected!")
                    client.subscribe("t/temperature", 0)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(CLIENT_ID, "Connection failed")
                    makeToast("Connection failed")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    fun publish(topic: String, data: String) {
        if (!client.isConnected) connect()
        try {
            val message = MqttMessage(data.toByteArray())
            client.publish(topic, message)
            makeToast("Message sent!")
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    class ClockSpinner: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            customTime = position != 0
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // No action needs to be taken
        }
    }

    fun makeToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}