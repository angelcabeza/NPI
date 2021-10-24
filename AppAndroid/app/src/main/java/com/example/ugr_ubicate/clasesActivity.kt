package com.example.ugr_ubicate

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.hardware.SensorEventListener
import android.widget.Toast

class clasesActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var running = false
    private var previousTotalSteps = 0f
    private var totalSteps = 0f
    private var instruccionesRuta1: Array<String> = arrayOf("Salga por la puerta de la clase", "Gire a la derecha", "Camine recto hasta encontrar la clase 3.6")
    private lateinit var  titulo: TextView
    private var cont = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clases)

        titulo = findViewById(R.id.instruccionesContainer)

        // Declaro el podómetro
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor= sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)


        titulo.text = totalSteps.toString()
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        totalSteps= p0!!.values[0]
        titulo.text = totalSteps.toString()
    }

    override fun onResume(){
        super.onResume()
        sensorManager.registerListener(this,sensor,1)
    }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    fun ruta1(){
        if (totalSteps.toInt() != 0 && totalSteps > 15 ){
            cont++
            titulo.text = instruccionesRuta1[cont]
            titulo.visibility = View.VISIBLE
        }
    }
}