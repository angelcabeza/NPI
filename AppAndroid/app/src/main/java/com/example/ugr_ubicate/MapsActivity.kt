package com.example.ugr_ubicate

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ugr_ubicate.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.SphericalUtil
import android.content.DialogInterface

import android.content.Intent

import android.location.LocationManager
import android.provider.Settings


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    // Siguiendo este tutorial: https://youtu.be/pjFcJ6EB8Dg

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var supportMapFragment: SupportMapFragment

    private lateinit var spType : Spinner
    private lateinit var btFind : Button
    private lateinit var btUbicacion : Button

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
    private var currentMarker : Int = -1

    private var ruta : objectRuta? = null
    private var currentMarcadorRuta : Int = -1
    private var coordMarcadorRuta : LatLng? = null
    private val distanciaMinMarcador : Float = 20F
    private var rutaActiva : Boolean = false

    private var mLocationRequest: LocationRequest? = null

    private val UPDATE_INTERVAL = (1 * 1000 /* 1 secs */).toLong()
    private val FASTEST_INTERVAL: Long = 500 /* 2 sec */

    private var flechaPuntoRuta: Marker? = null
    private var flechaDibujada : Boolean = false

    //Vector de gravedad para marcar la rotación del telefono
    private var gravity: FloatArray? = FloatArray(3)

    //Variables de coordenadas
    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f

    //Esto marca la ventana de tiempo para hacer el gesto
    private var lastUpdate1: Long = 0

    //Estas son las variables que marcan si se ha hecho uno de los gestos. Después de usarlas ponlas a 0
    private var swipeExit: Boolean = false



    // Creacion del objeto
    override fun onCreate(savedInstanceState: Bundle?) {
        ///
        super.onCreate(savedInstanceState)

        checkForPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, "Ubicacion",
            REQUEST_PERMISSION_CODE)
        statusCheck()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ///

        spType = findViewById(R.id.sp_type)
        btFind = findViewById(R.id.bt_find)
        btUbicacion = findViewById(R.id.bt_ubicacion)

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment

        spType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, placeNameList)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        obtenerUltimaUbicacion()
        startLocationUpdates()

        btFind.setOnClickListener{
            currentPlace = spType.selectedItemPosition

            fetchMarcadores().start()
        }

        btUbicacion.setOnClickListener{
            this@MapsActivity.runOnUiThread(Runnable {
                statusCheck()
                obtenerUltimaUbicacion()
                startLocationUpdates()
            })
        }

        var sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var sens_grav = sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
        var se = object: SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int){}

            override fun onSensorChanged(sensorEvent: SensorEvent?){
                when (sensorEvent?.sensor?.getType()){
                    Sensor.TYPE_GRAVITY        -> gravity = sensorEvent.values.clone()
                }

                if (gravity != null){
                    x = ((gravity!![0]/9.8f)*100).toInt().toFloat()
                    y = ((gravity!![1]/9.8f)*100).toInt().toFloat()
                    z = ((gravity!![2]/9.8f)*100).toInt().toFloat()

                    val horaActual = System.currentTimeMillis()

                    if (y > 80 && z > 0){
                        lastUpdate1 = horaActual
                    }

                    if ( ((horaActual - lastUpdate1) <= 400) && x > 50){
                        lastUpdate1 -= 400
                        swipeExit = true
                    }
                    if ( ((horaActual - lastUpdate1) <= 400) && y < 50 && x < 50 && x > -50 && z > 0){
                        this@MapsActivity.runOnUiThread(Runnable {
                            if (currentMarker == -1){
                                currentPlace = spType.selectedItemPosition

                                fetchMarcadores().start()
                            }
                            else{
                                pasarSiguienteMarcador()
                            }
                        })
                        lastUpdate1 -= 400
                    }
                    else{
                        if( ((horaActual - lastUpdate1) <= 400) && y < 85 && x < 50 && x > -50 && z < 0) {
                            lastUpdate1 -= 400
                            rutaActiva = true
                            this@MapsActivity.runOnUiThread(Runnable {
                                fetchRuta().start()
                            })
                        }
                    }
                }
            }
        }
        sm.registerListener(se,sens_grav,SensorManager.SENSOR_DELAY_FASTEST)
    }


    inner class fetchMarcadores : Thread() {
        var data : String = ""

        override public fun run(){
            if (latitudUsuario != null && longitudUsuario != null && currentPlace != -1){
                rutaActiva = false

                mainHandler.post(Runnable {
                    progressDialog = ProgressDialog(this@MapsActivity)
                    progressDialog.setMessage("Leyendo marcadores")
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                })

                val url_str: String = getUrlForMarcadores()

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

                        marcadoresList = jsonParser.parseResultMarcadores(obj)

                        reiniciarMarcadoresMapa()
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
    }

    inner class fetchRuta : Thread() {
        var data : String = ""

        override public fun run(){
            if (currentMarker != -1 && longitudUsuario != null && latitudUsuario != null){
                mainHandler.post(Runnable {
                    progressDialog = ProgressDialog(this@MapsActivity)
                    progressDialog.setMessage("Leyendo ruta")
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                })

                val url_str: String = getUrlForRoute()

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

                        ruta = jsonParser.parseResultRuta(obj)

                        actualizarMarcadorRuta(0)

                        mostrarRuta()
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
    }

    private fun comprobarUsuarioEnMarcadorRuta(){
        var distancia : FloatArray = FloatArray(1)

        Location.distanceBetween(coordMarcadorRuta!!.latitude, coordMarcadorRuta!!.longitude,
            latitudUsuario!!, longitudUsuario!!, distancia)

        if (distancia[0] <= distanciaMinMarcador){
            Toast.makeText(this, ruta!!.getTypeManeuver(currentMarcadorRuta), Toast.LENGTH_LONG).show()

            pasarSiguienteMarcadorRuta()
        }
    }

    private fun actualizarMarcadorRuta(i: Int) {
        if (i < ruta!!.numeroSteps()){
            currentMarcadorRuta = i
            if (ruta != null){
                coordMarcadorRuta = ruta!!.getCoordenadas(i)
            }
        }
        else{
            currentMarcadorRuta = -1
        }
    }

    private fun pasarSiguienteMarcadorRuta() {
        if ( (currentMarcadorRuta + 1) < ruta!!.numeroSteps() ){
            actualizarMarcadorRuta(currentMarcadorRuta + 1)

            mostrarRuta()
        }
        else{
            rutaActiva = false
            Toast.makeText(this, "Fin de ruta", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarRuta(){
        if (ruta != null){
            rutaActiva = true

            var pathPoints : List<LatLng> = PolyUtil.decode(ruta!!.polylineTotal())

            val polylineOptions = PolylineOptions()
                .addAll(pathPoints)
                .color(Color.BLUE)
                .width(12f)

            this@MapsActivity.runOnUiThread(Runnable {
                map.clear()
                flechaDibujada = false

                map.addPolyline(polylineOptions)
            })

            mostrarPuntosRuta()
        }
    }

    private fun mostrarPuntosRuta() {
        var listaPuntosRuta: List<LatLng> = ruta!!.getPuntosRuta()

        for (i in 0..(listaPuntosRuta.size-1)) {
            this@MapsActivity.runOnUiThread(Runnable {
                if (i == (listaPuntosRuta.size-1)){
                    map.addMarker(
                        MarkerOptions().position(listaPuntosRuta[i])
                            .icon(BitmapFromVector(getApplicationContext(), R.drawable.flag_destino_24))
                    )
                }
                else if (i != currentMarcadorRuta) {
                    map.addMarker(
                        MarkerOptions().position(listaPuntosRuta[i])
                            .icon(
                                BitmapFromVector(
                                    getApplicationContext(),
                                    R.drawable.orange_flag_24
                                )
                            )
                    )
                }
                else {
                    map.addMarker(
                        MarkerOptions().position(listaPuntosRuta[i])
                            .icon(BitmapFromVector(getApplicationContext(), R.drawable.red_flag_24))
                    )
                }
            })
        }

        actualizarFlechaMarcador()
        Log.e("Activar flecha", "mostrarPuntosRuta")

        this@MapsActivity.runOnUiThread(Runnable {
            if (latitudUsuario != null && longitudUsuario != null){
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitudUsuario!!, longitudUsuario!!), 16.0F)
                )
            }
        })
    }

    private fun getUrlForRoute(): String {
        val hashMarcadores: HashMap<String, Object> = marcadoresList!!.get(currentMarker)

        val lat: Double = hashMarcadores.get("lat") as Double
        val lng: Double = hashMarcadores.get("lon") as Double

        // https://router.project-osrm.org/route/v1/foot/-3.608628,37.173484;-3.607011,37.175732?steps=true&geometries=polyline
        val url_str : String = "https://router.project-osrm.org/route/v1/foot/" +
                longitudUsuario + "," +
                latitudUsuario + ";" +
                lng + "," +
                lat +
                "?steps=true&geometries=polyline"

        return url_str
    }

    private fun getUrlForMarcadores(): String {
        // https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];nwr(around:1000,37.178358,-3.602850)[%22amenity%22=%22hospital%22];out%20tags%20center;

        val url_str: String = "https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];nwr(around:" +
                radioBusqueda + "," +
                latitudUsuario + "," +
                longitudUsuario + "" +
                ")[%22amenity%22=%22" +
                placeTypeList[currentPlace] + "%22];out%20tags%20center;"

        return url_str
    }

    private fun reiniciarMarcadoresMapa(){
        this@MapsActivity.runOnUiThread(Runnable {
            map.clear()

            flechaDibujada = false
        })

        if (marcadoresList != null) {
            currentMarker = 0

            val hashMarcadores: HashMap<String, Object> = marcadoresList!!.get(currentMarker)

            val lat: Double = hashMarcadores.get("lat") as Double
            val lng: Double = hashMarcadores.get("lon") as Double
            val nombre: String = hashMarcadores.get("name") as String

            val latLng: LatLng = LatLng(lat, lng)

            var options: MarkerOptions = MarkerOptions()
            options.position(latLng)
            options.title(nombre)

            this@MapsActivity.runOnUiThread(Runnable {
                var marcadorAniadido = map.addMarker(options)
                marcadorAniadido.showInfoWindow()

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(lat, lng), 13.0F)
                )
            })
        }
    }

    private fun pasarSiguienteMarcador(){
        rutaActiva = false

        this@MapsActivity.runOnUiThread(Runnable {
            map.clear()
            flechaDibujada = false
        })

        if (marcadoresList != null && (currentMarker + 1) < marcadoresList!!.size ) {
            currentMarker = currentMarker + 1

            val hashMarcadores: HashMap<String, Object> = marcadoresList!!.get(currentMarker)

            val lat: Double = hashMarcadores.get("lat") as Double
            val lng: Double = hashMarcadores.get("lon") as Double
            val nombre: String = hashMarcadores.get("name") as String

            val latLng: LatLng = LatLng(lat, lng)

            var options: MarkerOptions = MarkerOptions()
            options.position(latLng)
            options.title(nombre)

            this@MapsActivity.runOnUiThread(Runnable {
                var marcadorAniadido = map.addMarker(options)
                marcadorAniadido.showInfoWindow()

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(lat, lng), 13.0F)
                )
            })
        }
        else{
            currentMarker = -1
        }
    }

    private fun aniadirTodosMarcadoresMapa() {
        this@MapsActivity.runOnUiThread(Runnable {
            map.clear()

            flechaDibujada = false
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

        this@MapsActivity.runOnUiThread(Runnable {
            if (latitudUsuario != null && longitudUsuario != null){
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitudUsuario!!, longitudUsuario!!), 13.0F)
                )
            }
        })
    }

    // Ver la ultima ubicacion del ususario
    private fun obtenerUltimaUbicacion() {
        checkForPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, "Ubicacion",
            REQUEST_PERMISSION_CODE)

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


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ){

        }

        var etsiitMarcador : HashMap<String, Object> = HashMap()
        etsiitMarcador.put("lat", 37.197282 as Object)
        etsiitMarcador.put("lon", -3.624350 as Object)
        etsiitMarcador.put("name", "ETSIIT" as Object)

        var dataList : MutableList<HashMap<String, Object>> = ArrayList()
        dataList.add(etsiitMarcador)
        marcadoresList = dataList

        currentMarker = 0

        activarUbicacionEnMapa()

        // Add a marker in ETSIIT and move the camera
        val marcador = LatLng(37.197282, -3.624350)
        map.addMarker(MarkerOptions().position(marcador).title("ETSIIT"))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marcador, 17.0F))

        if (latitudUsuario != null && longitudUsuario != null){
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(latitudUsuario!!, longitudUsuario!!), 15.0F)
            )
        }
    }


    private fun BitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        // below line is use to generate a drawable.
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)

        // below line is use to set bounds to our vector drawable.
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        // below line is use to create a bitmap for our
        // drawable which we have added.
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        // below line is use to add bitmap in our canvas.
        val canvas = Canvas(bitmap)

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas)

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    private fun activarUbicacionEnMapa() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            checkForPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, "Ubicacion",
                REQUEST_PERMISSION_CODE)
        }

        map.isMyLocationEnabled = true
    }


    private fun statusCheck() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Parece que el GPS esta desactivado, quieres activarlo?")
            .setCancelable(false)
            .setPositiveButton(
                "Si"
            ) { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(
                "No"
            ) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }


    // Trigger new location updates at interval
    private fun startLocationUpdates() {
        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest()
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest!!.setInterval(UPDATE_INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
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
        getFusedLocationProviderClient(this).requestLocationUpdates(
            mLocationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    // do work here
                    onLocationChanged(locationResult.lastLocation)
                }
            },
            Looper.myLooper()
        )
    }

    private fun onLocationChanged(location: Location) {
        latitudUsuario = location.latitude
        longitudUsuario = location.longitude

        if (rutaActiva) {
            comprobarUsuarioEnMarcadorRuta()
            actualizarFlechaMarcador()
        }
    }

    private fun actualizarFlechaMarcador() {
        this@MapsActivity.runOnUiThread(Runnable {
            if (flechaPuntoRuta != null){
                flechaPuntoRuta!!.isVisible = false
            }
        })

        val locationUser = Location("user") //provider name is unnecessary
        locationUser.latitude = latitudUsuario!! //your coords of course
        locationUser.longitude = longitudUsuario!!

        val locationMarker = Location("marker") //provider name is unnecessary
        locationMarker.latitude = coordMarcadorRuta!!.latitude //your coords of course
        locationMarker.longitude = coordMarcadorRuta!!.longitude

        var nueva_pos : LatLng = LatLng(latitudUsuario!!,longitudUsuario!!)
        var nueva_rotacion : Float = locationUser.bearingTo(locationMarker)

        if (flechaDibujada){
            this@MapsActivity.runOnUiThread(Runnable {
                flechaPuntoRuta!!.position = nueva_pos
                flechaPuntoRuta!!.rotation = nueva_rotacion
                flechaPuntoRuta!!.isVisible = rutaActiva
            })
        }
        else{
            this@MapsActivity.runOnUiThread(Runnable {
                flechaPuntoRuta = map.addMarker(MarkerOptions()
                    .position(nueva_pos)
                    .flat(true)
                    .rotation(nueva_rotacion)
                    .icon(BitmapFromVector(getApplicationContext(), R.drawable.arrow_next_marker_24))
                    .visible(rutaActiva))
            })
        }

        flechaDibujada = true
    }


    // MANAGE PERMISSION
    private fun checkForPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            when {
                ContextCompat.checkSelfPermission(applicationContext,permission) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
                }

                //shouldShowRequestPermissionRationale(permission) -> showDialog(permission,name,requestCode)

                else -> ActivityCompat.requestPermissions(this,arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(applicationContext, "Permiso de $name actualmente denegado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Permiso de $name concedido", Toast.LENGTH_SHORT).show()

                this@MapsActivity.runOnUiThread(Runnable {
                    supportMapFragment.getMapAsync(this)
                })
            }
        }

        when(requestCode) {
            REQUEST_PERMISSION_CODE  -> innerCheck("Ubicacion")
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Los permisos para acceder a su $name son necesarios para utilizar la app")
            setTitle("Permisos requeridos")
            setPositiveButton("OK") { dialog, wich ->
                ActivityCompat.requestPermissions(this@MapsActivity, arrayOf(permission),requestCode)
            }
        }

        val dialog = builder.create()
        dialog.show()
    }
}