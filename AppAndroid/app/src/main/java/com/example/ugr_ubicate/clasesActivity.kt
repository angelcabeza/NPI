package com.example.ugr_ubicate

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.hardware.SensorEventListener
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*

class clasesActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManagerPodometro: SensorManager
    private lateinit var sensorManagerBrujula: SensorManager
    private lateinit var sensorManagerAcelerometro: SensorManager
    private lateinit var sensorPodometro: Sensor
    private lateinit var sensorBrujula: Sensor
    private lateinit var sensorAcelerometro: Sensor
    private var currentSteps = 0
    private var referencia = false
    private var referenciaGiro = 0f
    private var perdido = false
    private var giro = 0f
    private var instruccionesRuta1: Array<String> = arrayOf("Salga por la puerta de la clase", "Gire a la derecha", "Camine recto hasta encontrar la clase 3.6")
    private var instruccionesEscalera34: Array<String> = arrayOf("Gire a la derecha", "Ande todo recto hasta llegar a la clase 3.6")
    private lateinit var  instrucciones: TextView
    private lateinit var textGesto: TextView
    private lateinit var imagen: ImageView
    private var primeraInstruccionRuta36 = false
    private var segundaInstruccionRuta36 = false
    private var terceraInstruccionRuta36 = false
    private var primeraInstruccionPerdido36 = false
    private var segundaInstruccionPerdidio36 = false
    private var perdido1 = false
    private var primeraInstruccionPerdidoDicha = false

    private var cont = 0
    val ACTIVITY_RQ = 101
    val CAMERA_RQ = 102
    val AUDIO_RQ = 103


    //Variable del gesto de agitación
    private var lastUpdate: Long = 0
    private var last_x =0f
    private var last_y = 0f
    private var last_z = 0f
    private val SHAKE_THRESHOLD = 200
    var agitacionDetectada1 = false
    var agitacionDetectada2 = false

    // TextToSpeech
    private var textToSpeechEngine: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clases)

        // Miro si el usuario esta perdido
        var intent = getIntent()
        var sitio = intent.getStringExtra("qrActivity.Escalera")

        if (sitio != null)
            perdido = true
        else
            perdido = false

        textToSpeechEngine = TextToSpeech(
            this
        ) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.e("TTS", "Inicio de la síntesis fallido")
            }
        }
        instrucciones = findViewById(R.id.instruccionesContainer)
        textGesto = findViewById(R.id.textGesto)
        imagen = findViewById(R.id.imagenClases)


        val titulo = findViewById<TextView>(R.id.titulo)
        val ruta1 = findViewById<Button>(R.id.ruta1)


        if (!perdido) {
            instrucciones.visibility = View.INVISIBLE
            textGesto.visibility = View.INVISIBLE
            imagen.visibility = View.INVISIBLE
        } else{
            titulo.visibility = View.INVISIBLE
            ruta1.visibility = View.INVISIBLE
            instrucciones.text = instruccionesEscalera34[cont]
            textGesto.visibility = View.VISIBLE
            imagen.setImageResource(R.drawable.girarderecha)
            imagen.visibility = View.VISIBLE
        }

        if (!perdido) {
            ruta1.setOnClickListener {
                titulo.visibility = View.INVISIBLE
                ruta1.visibility = View.INVISIBLE
                instrucciones.text = instruccionesRuta1[cont]
                textToSpeechEngine?.speak(instrucciones.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                instrucciones.visibility = View.VISIBLE
                textGesto.visibility = View.VISIBLE
                imagen.visibility = View.VISIBLE
                currentSteps = 0
            }
        }

        checkForPermission(android.Manifest.permission.CAMERA,"camera",CAMERA_RQ)
        checkForPermission(android.Manifest.permission.ACTIVITY_RECOGNITION,"activity",ACTIVITY_RQ)
        checkForPermission(android.Manifest.permission.RECORD_AUDIO,"audio",AUDIO_RQ)

        // Declaro los sensores
        sensorManagerPodometro = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManagerBrujula = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManagerAcelerometro = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onSensorChanged(event: SensorEvent?) {

        //evento de acelerometro lineal (desprecia la gravedad, el normal siempre pilla la gravedad)
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            //Se obtienen los valores nuevos
            val new_x = event!!.values[0]
            val new_y = event!!.values[1]
            val new_z = event!!.values[2]

            // Se obtiene la hora actual
            val horaActual = System.currentTimeMillis()

            // Realiza las comprobaciones solo si han pasado 400 ms
            if (horaActual - lastUpdate >= 400) {
                val diferencia: Long = horaActual - lastUpdate
                // Modifica la hora de comparacion y calcula la velocidad
                lastUpdate = horaActual
                val velocidad: Float = Math.abs(new_y - last_y) / diferencia * 10000

                val norm_vector = Math.sqrt(new_x.toDouble() * new_x.toDouble() + new_y.toDouble() * new_y.toDouble() + new_z.toDouble() * new_z.toDouble())

                val norm_y = new_y / norm_vector

                val inclination = Math.round(Math.toDegrees(Math.acos(norm_y))).toInt();

                // Si la velocidad es suficiente y el movil no se ha girado en el eje X
                if (velocidad >= SHAKE_THRESHOLD && (inclination <= 20)) {
                    if (!agitacionDetectada1)
                        agitacionDetectada1 = true
                    else
                        agitacionDetectada2 = true
                }

                //Actualiza las coordenadas
                last_x = new_x
                last_y = new_y
                last_z = new_z
            }
        }

        if (event!!.sensor.type == Sensor.TYPE_STEP_DETECTOR){
            currentSteps +=1
        }

        if (event!!.sensor.type == Sensor.TYPE_ORIENTATION) {
            if (!referencia && (primeraInstruccionRuta36 || (perdido && !primeraInstruccionPerdido36))) {
                referenciaGiro = event!!.values[0]
                referencia = true
            }
            giro = referenciaGiro - event!!.values[0]
        }

        if (!agitacionDetectada1 && !perdido)
            ruta1()

        if (agitacionDetectada1 && !agitacionDetectada2 && !perdido1)
            estaPerdido()

        if (agitacionDetectada2)
            activarQR()

        if (perdido)
            rutaEscalera34()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }


    override fun onStop() {
        super.onStop()
        sensorManagerPodometro.unregisterListener(this,sensorPodometro)
        sensorManagerBrujula.unregisterListener(this,sensorBrujula)
        sensorManagerAcelerometro.unregisterListener(this,sensorAcelerometro)
    }

    override fun onResume() {
        super.onResume()
        sensorPodometro = sensorManagerPodometro.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        sensorManagerPodometro?.registerListener(this, sensorPodometro, SensorManager.SENSOR_DELAY_FASTEST)

        sensorBrujula = sensorManagerBrujula.getDefaultSensor((Sensor.TYPE_ORIENTATION))
        sensorManagerBrujula?.registerListener(this, sensorBrujula, SensorManager.SENSOR_DELAY_NORMAL)

        sensorAcelerometro = sensorManagerAcelerometro.getDefaultSensor((Sensor.TYPE_ACCELEROMETER))
        sensorManagerAcelerometro?.registerListener(this, sensorAcelerometro, SensorManager.SENSOR_DELAY_NORMAL)
    }


    fun ruta1(){
        if (currentSteps >= 10 && !primeraInstruccionRuta36){
            cont++
            instrucciones.text = instruccionesRuta1[cont]
            if (!primeraInstruccionRuta36) {
                textToSpeechEngine?.speak(instrucciones.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            }
            primeraInstruccionRuta36 = true
            currentSteps = 0
            imagen.setImageResource(R.drawable.girarderecha)
        }
        else if (primeraInstruccionRuta36 && giro <= -80 && giro >= -110 && !segundaInstruccionRuta36) {
            cont++
            instrucciones.text = instruccionesRuta1[cont]
            textToSpeechEngine?.speak(instrucciones.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            segundaInstruccionRuta36 = true
            currentSteps = 0
            imagen.setImageResource(R.drawable.caminar)
        }
        else if (primeraInstruccionRuta36 && segundaInstruccionRuta36 && currentSteps >= 5){
            instrucciones.text = "¡Ha llegado a su destino!"
            terceraInstruccionRuta36 = true
            textToSpeechEngine?.speak(instrucciones.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            imagen.setImageResource(R.drawable.tick)
        }
    }

    fun rutaEscalera34(){
        if (!primeraInstruccionPerdidoDicha) {
            textToSpeechEngine?.speak(instrucciones.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            primeraInstruccionPerdidoDicha = true
        }
        if (!primeraInstruccionPerdido36 && giro <= -80 && giro >= -110 && !segundaInstruccionPerdidio36){
            cont++
            instrucciones.text = instruccionesEscalera34[cont]
            textToSpeechEngine?.speak(instrucciones.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            primeraInstruccionPerdido36 = true
            currentSteps = 0
            imagen.setImageResource(R.drawable.caminar)
        }
        else if (currentSteps >= 20 && primeraInstruccionPerdido36 && !segundaInstruccionPerdidio36) {
            cont++
            instrucciones.text = "Ha llegado a su destino"
            textToSpeechEngine?.speak(instrucciones.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            segundaInstruccionPerdidio36 = true
            imagen.setImageResource(R.drawable.tick)
        }
    }

    private fun estaPerdido(){
        instrucciones.text = "Si se ha perdido busque un código QR y le indicaremos la ruta desde allí"
        textToSpeechEngine?.speak(instrucciones.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
        imagen.setImageResource(R.drawable.qr)
        perdido1 = true
    }

    private fun activarQR(){
        agitacionDetectada1 = false
        agitacionDetectada2 = false
        val intent = Intent(this, qrActivity::class.java)
        startActivity(intent)
    }



    // MANAGE PERMISSION
    private fun checkForPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            when {
                ContextCompat.checkSelfPermission(applicationContext,permission) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
                }

                shouldShowRequestPermissionRationale(permission) -> showDialog(permission,name,requestCode)

                else -> ActivityCompat.requestPermissions(this,arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "$name permission refused", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
        }

        when(requestCode) {
            ACTIVITY_RQ  -> innerCheck("activity")
            CAMERA_RQ -> innerCheck("camera")
        }
    }
    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to acces your $name is required to use this app")
            setTitle("Permission requierd")
            setPositiveButton("OK") { dialog, wich ->
                ActivityCompat.requestPermissions(this@clasesActivity, arrayOf(permission),requestCode)
            }
        }

        val dialog = builder.create()
        dialog.show()
    }
}