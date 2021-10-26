package com.example.ugr_ubicate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.hardware.SensorEventListener
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class clasesActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManagerPodometro: SensorManager
    private lateinit var sensorManagerBrujula: SensorManager
    private lateinit var sensorPodometro: Sensor
    private lateinit var sensorBrujula: Sensor
    private var previousTotalSteps = 0f
    private var totalSteps = 0f
    private var currentSteps = 0
    private var referencia = false
    private var referenciaGiro = 0f
    private var giro = 0f
    private var instruccionesRuta1: Array<String> = arrayOf("Salga por la puerta de la clase", "Gire a la derecha", "Camine recto hasta encontrar la clase 3.6")
    private lateinit var  instrucciones: TextView
    private lateinit var  debug: TextView
    private lateinit var  textGiro: TextView
    private var primeraInstruccion = false
    private var segundaInstruccion = false
    private var terceraInstruccion = false
    private var cont = 0
    val ACTIVITY_RQ = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clases)

        instrucciones = findViewById(R.id.instruccionesContainer)
        debug = findViewById(R.id.debug)
        textGiro = findViewById(R.id.textGiro)
        instrucciones.visibility = View.INVISIBLE

        val titulo = findViewById<TextView>(R.id.titulo)
        val ruta1 = findViewById<Button>(R.id.ruta1)
        ruta1.setOnClickListener{
            titulo.visibility = View.INVISIBLE
            ruta1.visibility = View.INVISIBLE
            instrucciones.text = instruccionesRuta1[cont]
            instrucciones.visibility = View.VISIBLE
            sensorPodometro = sensorManagerPodometro.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            sensorManagerPodometro?.registerListener(this, sensorPodometro, SensorManager.SENSOR_DELAY_FASTEST)

            sensorBrujula = sensorManagerBrujula.getDefaultSensor((Sensor.TYPE_ORIENTATION))
            sensorManagerBrujula?.registerListener(this, sensorBrujula, SensorManager.SENSOR_DELAY_NORMAL)
        }

        checkForPermission(android.Manifest.permission.ACTIVITY_RECOGNITION,"activity",ACTIVITY_RQ)

        // Declaro el podómetro
        sensorManagerPodometro = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManagerBrujula = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_STEP_DETECTOR){
            currentSteps +=1
            debug.text = currentSteps.toString()
        }

        if (event!!.sensor.type == Sensor.TYPE_ORIENTATION && primeraInstruccion && !segundaInstruccion) {
            if (!referencia && primeraInstruccion) {
                referenciaGiro = event!!.values[0]
                referencia = true
            }
            giro = referenciaGiro - event!!.values[0]
            textGiro.text = giro.toString()
        }

        ruta1()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }


    override fun onStop() {
        super.onStop()
        sensorManagerPodometro.unregisterListener(this,sensorPodometro)
        sensorManagerBrujula.unregisterListener(this,sensorBrujula)
    }


    fun ruta1(){
        if (currentSteps >= 10 && !primeraInstruccion){
            cont++
            instrucciones.text = instruccionesRuta1[cont]
            primeraInstruccion = true
            currentSteps = 0
        }
        else if (primeraInstruccion && giro <= -80 && giro >= -110 && !segundaInstruccion) {
            cont++
            instrucciones.text = instruccionesRuta1[cont]
            segundaInstruccion = true
        }
        else if (primeraInstruccion && segundaInstruccion && currentSteps >= 5){
            instrucciones.text = "¡Ha llegado a su destino!"
            terceraInstruccion = true
        }
    }


    // MANAGE PERMISSION
    private fun checkForPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            when {
                ContextCompat.checkSelfPermission(applicationContext,permission) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
                }

                shouldShowRequestPermissionRationale(permission) -> showDialog(permission,name,requestCode)

                else -> ActivityCompat.requestPermissions(this,arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "$name permission refused", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
        }

        when(requestCode) {
            ACTIVITY_RQ  -> innerCheck("activity")
        }
    }
    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to acces your $name is required to use this app")
            setTitle("Permission requierd")
            setPositiveButton("OK") { dialog, wich ->
                ActivityCompat.requestPermissions(this@clasesActivity, arrayOf(permission),requestCode)
            }
        }

        val dialog = builder.create()
        dialog.show()
    }
}