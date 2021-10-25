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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class clasesActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var running = false
    private var previousTotalSteps = 0f
    private var totalSteps = 0f
    private var instruccionesRuta1: Array<String> = arrayOf("Salga por la puerta de la clase", "Gire a la derecha", "Camine recto hasta encontrar la clase 3.6")
    private lateinit var  titulo: TextView
    private var cont = 0
    val ACTIVITY_RQ = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clases)

        titulo = findViewById(R.id.instruccionesContainer)

        checkForPermission(android.Manifest.permission.ACTIVITY_RECOGNITION,"activity",ACTIVITY_RQ)
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