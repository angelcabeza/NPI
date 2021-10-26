package com.example.ugr_ubicate

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.ugr_ubicate.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONException
import org.json.JSONObject
import org.json.XML
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
    private var latitudUsuario: Double = 0.0
    private var longitudUsuario: Double = 0.0

    private val radioBusqueda : Int = 3000

    private val REQUEST_PERMISSION_CODE = 100
    ///


    // Creacion del objeto
    override fun onCreate(savedInstanceState: Bundle?) {
        ///
        super.onCreate(savedInstanceState)

        solicitarPermisoInternet()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ///

        spType = findViewById(R.id.sp_type)
        btFind = findViewById(R.id.bt_find)

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment

        //val placeTypeList = arrayOf<String>("cajero automatico", "banco", "hospital", "teatro", "bar")
        val placeNameList = arrayOf<String>("Cajero automatico", "Banco", "Hospital", "Teatro", "Bar")

        val placeTypeList = arrayOf<String>("atm", "bank", "hospital", "movie_theater", "restaurant")
        //val placeNameList = arrayOf<String>("ATM", "Bank", "Hospital", "Movie Theater", "Restaurant")

        spType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, placeNameList)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        obtenerUltimaUbicacion()

        Log.e("onMapReady", "no se ve")

        btFind.setOnClickListener{
            val i : Int = spType.selectedItemPosition

            /*val url : String = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + // Url
                    "?location=" + latitudUsuario + "," + longitudUsuario + // Posicion usuario
                    "&radius=5000" + // radio de busqueda de sitios cercanos
                    "&type=" + placeTypeList[i] + // tipo de sitio
                    "&sensor=true" + // sensor
                    "&key=" + resources.getString(R.string.google_maps_key) // Google maps key*/

            val url : String = "http://overpass-api.de/api/interpreter?data=<query type=\"node\"><around " +
                    "lat='" +  latitudUsuario + "' lon='" + longitudUsuario +
                    "' radius='" + radioBusqueda +
                    "'/><has-kv k=\"amenity\" v=\"" + placeTypeList[i] +
                    "\"/></query><print />"

            // Descargar JSON
            PlaceTask().execute(url)

        }

    }

    private fun solicitarPermisoInternet() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET),
                REQUEST_PERMISSION_CODE)
        }
    }

    @SuppressLint("StaticFieldLeak")
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
            val url: URL = URL(s)

            val connection : HttpURLConnection = url.openConnection() as HttpURLConnection

            connection.connect()

            val stream : InputStream = connection.inputStream

            val reader : BufferedReader = BufferedReader(InputStreamReader(stream))

            val builder : StringBuilder = java.lang.StringBuilder()

            var line : String = ""

            while ( (reader.readLine().also { line = it }) != null ){
                builder.append(line)
            }

            val data : String = builder.toString()

            reader.close()

            return data
        }

    }

    @SuppressLint("StaticFieldLeak")
    private inner class ParserTask : AsyncTask<String, Int, List<HashMap<String, String>>>() {
        override fun doInBackground(vararg p0: String?): List<HashMap<String, String>>? {
            val jsonParser : JsonParser = JsonParser()

            var mapList : List<HashMap<String, String>>? = null

            var obj : JSONObject? = null

            try{
                //obj = JSONObject(p0[0])
                obj = XML.toJSONObject(p0[0])

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
                    val hashMapList : HashMap<String, String> = result.get(i)

                    val lat : Double = parseDouble(hashMapList.get("lat"))
                    val lng : Double = parseDouble(hashMapList.get("lng"))
                    val nombre : String? = hashMapList.get("name")

                    val latLng : LatLng = LatLng(lat, lng)

                    val options : MarkerOptions = MarkerOptions()
                    options.position(latLng)
                    options.title(nombre)
                    map.addMarker(options)

                    Log.e("Marker", latLng.toString())

                    i++
                }
            }
        }

    }


    // Ver la ultima ubicacion del ususario
    private fun obtenerUltimaUbicacion() {
        solicitarPermisoUbicacionPrecisa()

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    latitudUsuario = location.latitude
                    longitudUsuario = location.longitude

                    Log.e("Ubicacion", "lat: " + latitudUsuario + " long: " + longitudUsuario)

                    // Actualizar el mapa
                    /*supportMapFragment.getMapAsync(OnMapReadyCallback() {
                        fun OnMapReady(googleMap: GoogleMap){
                            map = googleMap
                            Log.e("onMapReady", "no se ve 1")

                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(latitudUsuario, longitudUsuario), 17.0F)
                            )
                        }
                    })*/
                    supportMapFragment.getMapAsync(this)

                }
            }
    }

    // Comprobar y solicitar el permiso de ubicacion precisa
    fun solicitarPermisoUbicacionPrecisa(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED){

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
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED){
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

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(latitudUsuario, longitudUsuario), 17.0F)
        )
    }

    private fun activarUbicacionEnMapa() {
        /*if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisoUbicacionPrecisa()
        }*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisoUbicacionPrecisa()
        }

        map.isMyLocationEnabled = true
    }
}