package com.example.ugr_ubicate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

}