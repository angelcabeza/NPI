package com.example.ugr_ubicate

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    val REQUEST_LOC_PERM_CODE = 100


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

        val buttonPermisoUbicacion = findViewById<Button>(R.id.permiso_ubication)
        buttonMaps.setOnClickListener{
            solicitarPermisoUbicacion()
        }
    }

    fun solicitarPermisoUbicacion(){
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){

            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_LONG).show()
        }
        else{
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOC_PERM_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (REQUEST_LOC_PERM_CODE == requestCode){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_LONG).show()
            }
        }
    }
}