package com.example.ugr_ubicate

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
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
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    // Siguiendo este tutorial: https://youtu.be/pjFcJ6EB8Dg

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var supportMapFragment: SupportMapFragment

    private lateinit var spType : Spinner
    private lateinit var btFind : Button

    private var mainHandler : Handler = Handler()
    private lateinit var progressDialog : ProgressDialog

    private val REQUEST_PERMISSION_CODE = 100

    private var latitudUsuario: Double? = null
    private var longitudUsuario: Double? = null

    private val radioBusqueda : Int = 5000
    private val placeNameList = arrayOf<String>("Banco", "Hospital", "Bar", "Edificios Universidad")
    private val placeTypeList = arrayOf<String>("bank", "hospital", "bar", "university")
    private var currentPlace : Int = -1
    private var marcadoresList : List<HashMap<String, Object>>? = null


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
            currentPlace = spType.selectedItemPosition

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

            val url_str: String = getUrlForJSON()

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

                    marcadoresList = jsonParser.parseResult(obj)

                    actualizarMarcadoresMapa()
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

    private fun getUrlForJSON(): String {
        val url_str: String = "https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];nwr(around:" +
                radioBusqueda + "," +
                latitudUsuario + "," +
                longitudUsuario + "" +
                ")[%22amenity%22=%22" +
                placeTypeList[currentPlace] + "%22];out%20tags%20center;"

        return url_str
    }

    private fun actualizarMarcadoresMapa() {
        this@MapsActivity.runOnUiThread(Runnable {
            map.clear()
        })

        if (marcadoresList != null) {
            var j: Int = 0

            while (j < marcadoresList!!.size) {
                val hashMarcadores: HashMap<String, Object> = marcadoresList!!.get(j)

                val lat: Double = hashMarcadores.get("lat") as Double
                val lng: Double = hashMarcadores.get("lon") as Double
                val nombre: String = hashMarcadores.get("name") as String

                val latLng: LatLng = LatLng(lat, lng)

                var options: MarkerOptions = MarkerOptions()
                options.position(latLng)
                options.title(nombre)

                this@MapsActivity.runOnUiThread(Runnable {
                    map.addMarker(options)
                })

                j++
            }
        }
    }


    // Ver la ultima ubicacion del ususario
    private fun obtenerUltimaUbicacion() {
        solicitarPermisoUbicacionPrecisa()

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

        var dataList : HashMap<String, Object> = HashMap()
        dataList.put("lat", 37.197282 as Object)
        dataList.put("lon", -3.624350 as Object)
        dataList.put("name", "ETSIIT" as Object)

        // Add a marker in ETSIIT and move the camera
        val marcador = LatLng(37.197282, -3.624350)
        map.addMarker(MarkerOptions().position(marcador).title("ETSIIT"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(marcador, 17.0F))

        activarUbicacionEnMapa()

        if (latitudUsuario != null && longitudUsuario != null){
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(latitudUsuario!!, longitudUsuario!!), 15.0F)
            )
        }
    }

    private fun activarUbicacionEnMapa() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisoUbicacionPrecisa()
        }

        map.isMyLocationEnabled = true
    }
}