package com.example.ugr_ubicate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TimeTableActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        val buttonNPI = findViewById<Button>(R.id.buttonNPI)
        buttonNPI.setOnClickListener{
            val intent = Intent(this, clasesActivity::class.java)
            startActivity(intent)
        }
    }
}