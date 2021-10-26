package com.example.ugr_ubicate

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Transformations.map
import androidx.loader.content.AsyncTaskLoader

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.ugr_ubicate.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.common.io.Files.map
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Double.parseDouble
import java.net.HttpURLConnection
import java.net.URL

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    // Siguiendo este tutorial: https://youtu.be/pjFcJ6EB8Dg

    ///
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    ///

    private lateinit var supportMapFragment: SupportMapFragment

    private lateinit var spType : Spinner
    private lateinit var btFind : Button

    ///
    private var latitud_usuario: Double = 0.0
    private var longitud_usuario: Double = 0.0

    val REQUEST_PERMISSION_CODE = 100
    ///


    // Creacion del objeto
    override fun onCreate(savedInstanceState: Bundle?) {
        ///
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ///

        spType = findViewById(R.id.sp_type)
        btFind = findViewById(R.id.bt_find)

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment

        val placeTypeList = arrayOf<String>("cajero automatico", "banco", "hospital", "teatro", "bar")
        val placeNameList = arrayOf<String>("Cajero automatico", "Banco", "Hospital", "Teatro", "Bar")

        spType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, placeNameList)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        obtenerUltimaUbicacion()

        btFind.setOnClickListener{
            var i : Int = spType.selectedItemPosition

            var url : String = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + // Url
                    "?location=" + latitud_usuario + "," + longitud_usuario + // Posicion usuario
                    "&radius=5000" + // radio de busqueda de sitios cercanos
                    "&types=" + placeTypeList[i] + // tipo de sitio
                    "&sensor=true" + // sensor
                    "&key=" + resources.getString(R.string.google_maps_key) // Google maps key

            // Descargar JSON
            PlaceTask().execute(url)

        }


    }

    private inner class PlaceTask() : AsyncTask<String, Int, String>() {
        override fun doInBackground(vararg p0: String?): String? {
            var data : String? = null

            try {
                data = downloadUrl(p0[0])
            } catch (e : IOException){
                e.printStackTrace()
            }

            return data
        }

        override fun onPostExecute(result: String?) {
            ParserTask().execute(result)
        }

        @Throws(IOException::class)
        private fun downloadUrl(s: String?): String {
            var url : URL = URL(s)

            var connection : HttpURLConnection = url.openConnection() as HttpURLConnection

            connection.connect()

            var stream : InputStream = connection.inputStream

            var reader : BufferedReader = BufferedReader(InputStreamReader(stream))

            var builder : StringBuilder = java.lang.StringBuilder()

            var line : String = ""

            while ( (reader.readLine().also { line = it }) != null ){
                builder.append(line)
            }

            var data : String = builder.toString()

            reader.close()

            return data
        }
    }

    private inner class ParserTask : AsyncTask<String, Int, List<HashMap<String, String>>>() {
        override fun doInBackground(vararg p0: String?): List<HashMap<String, String>>? {
            var jsonParser : JsonParser = JsonParser()

            var mapList : List<HashMap<String, String>>? = null

            var obj : JSONObject? = null

            try{
                obj = JSONObject(p0[0])

                mapList = jsonParser.parseResult(obj)

            } catch (e : JSONException){
                e.printStackTrace()
            }

            return mapList
        }

        override fun onPostExecute(result: List<HashMap<String, String>>?) {
            map.clear()

            var i : Int = 0

            if (result != null) {
                while(i < result.size){
                    var hashMapList : HashMap<String, String> = result.get(i)

                    var lat : Double = parseDouble(hashMapList.get("lat"))
                    var lng : Double = parseDouble(hashMapList.get("lng"))
                    var nombre : String? = hashMapList.get("name")

                    var latLng : LatLng = LatLng(lat, lng)

                    var options : MarkerOptions = MarkerOptions()
                    options.position(latLng)
                    options.title(nombre)
                    map.addMarker(options)

                    i++
                }
            }
        }

    }


    // Ver la ultima ubicacion del ususario
    private fun obtenerUltimaUbicacion() {
        solicitarPermisoUbicacionPrecisa()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    latitud_usuario = location.latitude
                    longitud_usuario = location.longitude

                    // Actualizar el mapa
                    supportMapFragment.getMapAsync(OnMapReadyCallback {
                        fun OnMapReady(googleMap: GoogleMap){
                            map = googleMap
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitud_usuario, longitud_usuario),
                                10F
                            ))
                        }
                    })
                }
            }
    }

    // Comprobar y solicitar el permiso de ubicacion precisa
    fun solicitarPermisoUbicacionPrecisa(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED){

            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_LONG).show()
        }
        else{
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_CODE)
        }
    }

    // Solicitar permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (REQUEST_PERMISSION_CODE == requestCode){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera
        val marcador = LatLng(37.197282, -3.624350)
        map.addMarker(MarkerOptions().position(marcador).title("ETSIIT"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(marcador, 17.0F))

        activarUbicacionEnMapa()
    }

    private fun activarUbicacionEnMapa() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisoUbicacionPrecisa()
        }
        map.setMyLocationEnabled(true)
    }
}