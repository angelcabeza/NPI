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
    var lastx = 0
    var lasty = 9999

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

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val index = MotionEventCompat.getActionIndex(event)
        var xPos = -1
        var yPos = -1

        xPos = MotionEventCompat.getX(event, index).toInt()
        yPos = MotionEventCompat.getY(event, index).toInt()


        if (event.action == 3 && event.pointerCount == 3) {
            checkVerticalSlice(event, xPos, yPos)
        }


        if (event.action == MotionEvent.ACTION_MOVE && event.pointerCount == 3) {
            checkSlice(event, xPos, yPos)
        }
        return true
    }

    fun checkSlice(event: MotionEvent, xPos: Int, yPos: Int): Boolean{
        Log.d("DEBUG", "MOTION EVENT. ${event.action}")
        Log.d("DEBUG", "EJE Y: $yPos")
        if (event.source and InputDevice.SOURCE_CLASS_POINTER != 0) {
            Log.d("DEBUG", "EJE Y: $yPos")
            if (xPos > lastx + 300) {
                lastx = xPos
                val intent = Intent(this, clasesActivity::class.java)
                startActivity(intent)
                return true
            } else if (xPos < lastx - 300) {
                lastx = xPos
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return true
    }

    fun checkVerticalSlice(event: MotionEvent, xPos: Int, yPos: Int): Boolean{
        Log.d("DEBUGasdasdasd", "MOTION EVENT. ${event.action}")
        Log.d("DEBUGasdasdasd", "EJE Y: $yPos")
        if (event.source and InputDevice.SOURCE_CLASS_POINTER != 0) {
            if (yPos < lasty - 300) {
                lasty = yPos
                val intent = Intent(this, TimeTableActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return true
    }

}