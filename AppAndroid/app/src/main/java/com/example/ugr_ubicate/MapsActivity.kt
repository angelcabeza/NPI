package com.example.ugr_ubicate

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.util.Log
import android.view.View
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
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Double.parseDouble
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import com.google.android.gms.maps.model.CameraPosition

import com.google.android.gms.maps.model.PolylineOptions
import java.net.MalformedURLException


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

    private var mainHandler : Handler = Handler()
    private lateinit var progressDialog : ProgressDialog

    ///
    private var latitudUsuario: Double = 0.0
    private var longitudUsuario: Double = 0.0

    private val radioBusqueda : Int = 5000

    private val REQUEST_PERMISSION_CODE = 100
    ///

    val placeNameList = arrayOf<String>("Banco", "Hospital", "Bar", "Edificios Universidad")
    val placeTypeList = arrayOf<String>("bank", "hospital", "bar", "university")

    var i : Int = -1


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

        spType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, placeNameList)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        obtenerUltimaUbicacion()

        btFind.setOnClickListener{
            i = spType.selectedItemPosition

            /*val url : String = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + // Url
                    "?location=" + latitudUsuario + "," + longitudUsuario + // Posicion usuario
                    "&radius=5000" + // radio de busqueda de sitios cercanos
                    "&type=" + placeTypeList[i] + // tipo de sitio
                    "&sensor=true" + // sensor
                    "&key=" + resources.getString(R.string.google_maps_key) // Google maps key

            val url : String = "https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];nwr(around:" +
                    radioBusqueda + ","+
                    latitudUsuario + "," +
                    longitudUsuario + "" +
                    ")[%22amenity%22=%22" +
                    placeTypeList[i] + "%22];out%20tags%20center;"*/

            //Log.e("URL", url)

            //fetchJSONMarcadores(url)

            // Descargar JSON
            //PlaceTask().execute(url)

            fetchData().start()
        }

    }

    inner class fetchData : Thread() {
        var data : String = ""

        override public fun run(){
            mainHandler.post(Runnable {
                progressDialog = ProgressDialog(this@MapsActivity)
                progressDialog.setMessage("Leyendo datos")
                progressDialog.setCancelable(false)
                progressDialog.show()
            })

            val url_str : String = "https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];nwr(around:" +
                    radioBusqueda + ","+
                    latitudUsuario + "," +
                    longitudUsuario + "" +
                    ")[%22amenity%22=%22" +
                    placeTypeList[i] + "%22];out%20tags%20center;"

            try{
                var url : URL = URL(url_str)

                var httpsURLConnection : HttpsURLConnection = url.openConnection() as HttpsURLConnection

                var inputStream : InputStream = httpsURLConnection.inputStream

                var bufferedReader : BufferedReader = BufferedReader(InputStreamReader(inputStream))

                var line : String? = null

                line = bufferedReader.readLine()

                while ( line != null ){
                    data = data + line

                    line = bufferedReader.readLine()
                }

                if (! data.isEmpty()){
                    val jsonParser : JsonParser = JsonParser()

                    var obj : JSONObject = JSONObject(data)

                    var mapList : List<HashMap<String, Object>> = jsonParser.parseResult(obj)

                    Log.e("Tamanio mapa", mapList.size.toString())

                    this@MapsActivity.runOnUiThread(Runnable {
                        map.clear()
                    })

                    var j : Int = 0

                    if (mapList != null) {
                        while(j < mapList.size){
                            val hashMapList : HashMap<String, Object> = mapList.get(j)

                            val lat : Double = hashMapList.get("lat") as Double
                            val lng : Double = hashMapList.get("lon") as Double
                            val nombre : String = hashMapList.get("name") as String

                            val latLng : LatLng = LatLng(lat, lng)

                            var options : MarkerOptions = MarkerOptions()
                            options.position(latLng)
                            options.title(nombre)

                            this@MapsActivity.runOnUiThread(Runnable {
                                map.addMarker(options)
                            })

                            j++
                        }
                    }
                }
            } catch (e : MalformedURLException){
                e.printStackTrace()
            } catch (e : IOException){
                e.printStackTrace()
            } catch (e : JSONException){
                e.printStackTrace()
            }

            mainHandler.post(Runnable {
                if(progressDialog.isShowing){
                    progressDialog.dismiss()
                }
            })
        }
    }

    /*private fun fetchJSON(url: String): JSONObject {
        Log.e("Download JSON", url)
        val apiResponse : String = URL(url).readText()
        Log.e("Download JSON", "finalizado")

        val obj : JSONObject = JSONObject(apiResponse)

        return obj
    }

    private fun obtenerMarcadores(obj : JSONObject): List<HashMap<String, Object>>? {
        val jsonParser : JsonParser = JsonParser()

        var mapList : List<HashMap<String, Object>>? = null

        try{
            mapList = jsonParser.parseResult(obj)
        } catch (e : JSONException){
            e.printStackTrace()
            Log.e("JSON", "Error en parsing")
        }

        return mapList
    }

    private fun aniadirMarcadores(resultados: List<HashMap<String, String>>?) {
        map.clear()

        var i : Int = 0

        if (resultados != null) {
            while(i < resultados.size){
                val hashMapList : HashMap<String, String> = resultados.get(i)

                val lat : Double = parseDouble(hashMapList.get("lat"))
                val lng : Double = parseDouble(hashMapList.get("lng"))
                val nombre : String? = hashMapList.get("name")

                val latLng : LatLng = LatLng(lat, lng)

                val options : MarkerOptions = MarkerOptions()
                options.position(latLng)
                options.title(nombre)
                map.addMarker(options)

                i++
            }
        }
    }

    private fun fetchJSONMarcadores (url : String){
        var jsonObject : JSONObject = fetchJSON(url)

        var listaMarcadores : List<HashMap<String, String>>? = obtenerMarcadores(jsonObject)

        aniadirMarcadores(listaMarcadores)
    }*/

    /*@SuppressLint("StaticFieldLeak")
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

            var line : String? = ""

            line = reader.readLine()

            while ( line != null ){
                builder.append(line)

                line = reader.readLine()
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
                    val hashMapList : HashMap<String, String> = result.get(i)

                    val lat : Double = parseDouble(hashMapList.get("lat"))
                    val lng : Double = parseDouble(hashMapList.get("lng"))
                    val nombre : String? = hashMapList.get("name")

                    val latLng : LatLng = LatLng(lat, lng)

                    val options : MarkerOptions = MarkerOptions()
                    options.position(latLng)
                    options.title(nombre)
                    map.addMarker(options)

                    i++
                }
            }
        }

    }*/


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

    // Comprobar y solicitar el permiso de Internet
    private fun solicitarPermisoInternet() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET),
                REQUEST_PERMISSION_CODE)
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
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray,
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