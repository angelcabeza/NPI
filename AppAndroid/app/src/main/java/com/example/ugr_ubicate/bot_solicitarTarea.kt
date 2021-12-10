package com.example.ugr_ubicate

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log
import com.google.cloud.dialogflow.v2.*


class solicitarTarea : AsyncTask<Void, Void, DetectIntentResponse> {

    // Declaramos estas variables
    var actividad: Activity? = null
    private var sesion: SessionName? = null
    private var sesionesCliente: SessionsClient? = null
    private var entradaConsulta: QueryInput? = null

    // Usamos las variables en un constructor
    constructor(actividad: Activity, sesion: SessionName, sesionesCliente: SessionsClient, entradaConsulta: QueryInput){
        this.actividad = actividad
        this.sesion = sesion
        this.sesionesCliente = sesionesCliente
        this.entradaConsulta = entradaConsulta
   }

    // Ejecutamos las tareas de fondo con el método de Android 'doInBackground'
    override fun doInBackground(vararg params: Void?): DetectIntentResponse? {

        // Usamos 'try' para realizar las tareas de fondo
        try {
            val detectarIntentosolicitarTarea = DetectIntentRequest.newBuilder()
                .setSession(sesion.toString())
                .setQueryInput(entradaConsulta)
                .build()
            return sesionesCliente?.detectIntent(detectarIntentosolicitarTarea)
        }
        // Si hay algún error, devolvemos una exception en 'catch'
        catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // El método de Android 'onPostExecute' ejecuta determinados hilos de la interface luego que se ejecutó
    // el método anterior llamado 'doInBackground'
    override fun onPostExecute(result: DetectIntentResponse?) {
        //Pasamos el resultado al método validar()
        (actividad as BotActivity).validar(result)
    }


}