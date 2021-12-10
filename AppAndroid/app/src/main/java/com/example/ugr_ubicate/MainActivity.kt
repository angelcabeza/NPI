package com.example.ugr_ubicate

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.view.MotionEventCompat
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    var enPausa = false
    var yaLLamado = false
    var xPosIni = -1
    var yPosIni = -1
    var xPosFin = -1
    var yPosFin = -1
    var contDedos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iniciarBotones()
    }

    fun iniciarBotones(){
        val buttonClases = findViewById<Button>(R.id.irClases)
        buttonClases.setOnClickListener{
            val intent = Intent(this, clasesActivity::class.java)
            startActivity(intent)
        }

        val buttonMaps = findViewById<Button>(R.id.irSitios)
        buttonMaps.setOnClickListener{
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        val buttonbOT = findViewById<Button>(R.id.ChatBot)
        buttonbOT.setOnClickListener{
            val intent = Intent(this, BotActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val index = MotionEventCompat.getActionIndex(event)
        val action = MotionEventCompat.getActionMasked(event)

        // Cojo las coordenadas de cuando empieza la acción
        if (action == 5 || action == 0) {
            xPosIni = MotionEventCompat.getX(event, index).toInt()
            yPosIni = MotionEventCompat.getY(event, index).toInt()
            contDedos++
        }
        // Cojo las coordenadas de cada movimiento de los dedos
        if (event.action == MotionEvent.ACTION_MOVE) {
            xPosFin = MotionEventCompat.getX(event, index).toInt()
            yPosFin = MotionEventCompat.getY(event, index).toInt()
        }

        // Cuando la acción termina o se cancela compruebo lo que ha querido hacer el usuario
        if ( (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)) {
            if (contDedos == 2) {
                contDedos = 0
                var xPos = xPosFin - xPosIni
                var yPos = yPosFin - yPosIni

                if (Math.abs(xPos) > Math.abs(yPos) && !enPausa) {
                    if (xPos > 0 && !yaLLamado) {
                        if (Math.abs(xPos) > 500) {
                            val intent = Intent(this, clasesActivity::class.java)
                            startActivity(intent)
                            yaLLamado = true
                        }
                    } else {
                        if (Math.abs(xPos) > 500) {
                            val intent = Intent(this, MapsActivity::class.java)
                            startActivity(intent)
                            yaLLamado = true
                        }
                    }
                } else if (!enPausa && !yaLLamado) {
                    if (Math.abs(yPos) > 500) {
                        val intent = Intent(this, TimeTableActivity::class.java)
                        startActivity(intent)
                    }
                }
            } else{
                contDedos = 0
            }
        }
        
        return true
    }

    override fun onPause() {
        super.onPause()
        enPausa = true
        contDedos = 0
    }
    override fun onResume() {
        super.onResume()
        enPausa = false
        yaLLamado = false
        contDedos = 0
    }
}