package com.example.ugr_ubicate

import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

public class JsonParser {
    val amenitiesStreet : Array<String> = arrayOf("bank", "hospital", "bar")

    private fun parseJsonObjectMarcadores (obj : JSONObject) : HashMap<String, Object> {
        var dataList : HashMap<String, Object> = HashMap()

        /*
        Bank:
            lat
            lon
            tags
                name
                addr:street (no en todos)
        Hospital:
            center
                lat
                lon
            tags
                name
                addr:street (no en todos)
                healthcare (no en todos)
        Bar:
            lat
            lon
            tags
                name
                addr:street (no en todos)
        Universidad:
            center
                lat
                lon
            tags
                name

         */

        try {
            var name : String? = obj.getJSONObject("tags").get("name") as String?

            var amenity : String? = obj.getJSONObject("tags").get("amenity") as String?

            var street : String? = null
            if (amenity in amenitiesStreet){
                street = obj.getJSONObject("tags").get("addr:street") as String?
            }

            var healthcare : String? = null
            if (amenity == "hospital"){
                healthcare = obj.getJSONObject("tags").get("healthcare") as String?
            }

            var latitud : Double? = null
            try{
                latitud = obj.get("lat") as Double?
            } catch (e : JSONException){}

            var longitud : Double? = null
            try{
                longitud = obj.get("lon") as Double?
            } catch (e : JSONException){}

            if (latitud == null || longitud == null){
                latitud = obj.getJSONObject("center").get("lat") as Double?
                longitud = obj.getJSONObject("center").get("lon") as Double?
            }


            if (name != null && latitud != null && longitud != null) {
                dataList.put("name", name as Object)
                dataList.put("lat", latitud as Object)
                dataList.put("lon", longitud as Object)
            }

        } catch (e : JSONException){
            e.printStackTrace()
        }

        return dataList
    }


    private fun parseJsonArrayMarcadores (jsonArray: JSONArray?) : List<HashMap<String, Object>>{
        var dataList : MutableList<HashMap<String, Object>> = ArrayList()

        var i : Int = 0

        if (jsonArray != null) {
            while (i < jsonArray.length()){
                try {
                    var data : HashMap<String, Object> = parseJsonObjectMarcadores(jsonArray?.get(i) as JSONObject)

                    if (data.size > 0){
                        dataList.add(data)
                    }

                } catch (e : JSONException){
                    e.printStackTrace()
                }

                i++
            }
        }

        return dataList
    }


    public fun parseResultMarcadores (obj : JSONObject) : List<HashMap<String, Object>>{
        var jsonArray : JSONArray? = null

        try {
            jsonArray = obj.getJSONArray("elements")
        } catch (e : JSONException){
            e.printStackTrace()
        }

        return parseJsonArrayMarcadores(jsonArray)
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private fun parseJsonArrayIntersections (jsonArray: JSONArray) : List<LatLng> {
        var listIntersections : MutableList<LatLng> = ArrayList()

        var i : Int = 0

        if (jsonArray != null) {
            while (i < jsonArray.length()){
                try {
                    var step : JSONObject = jsonArray.get(i) as JSONObject

                    var longitud : Double = step.getJSONArray("location").get(0) as Double
                    var latitud : Double = step.getJSONArray("location").get(1) as Double

                    var coordenadas : LatLng = LatLng(latitud, longitud)

                    listIntersections.add(coordenadas)

                } catch (e : JSONException){
                    e.printStackTrace()
                }

                i++
            }
        }

        return listIntersections
    }


    private fun parseIndividualStepRuta (obj : JSONObject) : HashMap<String, Object> {
        var dataList : HashMap<String, Object> = HashMap()

        /*
        intersections:
            0:
                location:
                    0: (longitud)
                    1: (latitud)
            1: ...
        geometry: (encoded polyline)
        name:
        maneuver:
            location:
                0: (longitud)
                1: (latitud)
            type:
            modifier: (no en todos)
         */

        try {
            var name : String = obj.get("name") as String


            var intersections : List<LatLng> = parseJsonArrayIntersections(obj.getJSONArray("intersections"))


            var geometries : String = obj.get("geometry") as String


            var maneuver : JSONObject = obj.getJSONObject("maneuver")

            var typeManeuver : String = maneuver.get("type") as String

            var modifierManeuver : String? = null
            try{
                modifierManeuver = maneuver.get("modifier") as String?
            }
            catch (e : JSONException){}

            if (modifierManeuver != null){
                typeManeuver += " " + modifierManeuver
            }


            var longitud : Double = maneuver.getJSONArray("location").get(0) as Double
            var latitud : Double = maneuver.getJSONArray("location").get(1) as Double
            var coordenadas : LatLng = LatLng(latitud, longitud)


            dataList.put("name", name as Object)
            dataList.put("coordenadas", coordenadas as Object)
            dataList.put("geometries", geometries as Object)
            dataList.put("typeManeuver", typeManeuver as Object)
            dataList.put("intersections", intersections as Object)

        } catch (e : JSONException){
            e.printStackTrace()
        }

        return dataList
    }


    private fun parseArrayStepsRuta (jsonArray: JSONArray?) : objectRuta{
        var ruta : objectRuta = objectRuta()

        var i : Int = 0

        if (jsonArray != null) {
            while (i < jsonArray.length()){
                try {
                    var data : HashMap<String, Object> = parseIndividualStepRuta(jsonArray?.getJSONObject(i))

                    if (data.size > 0){
                        ruta.aniadirStep(data)
                    }

                } catch (e : JSONException){
                    e.printStackTrace()
                }

                i++
            }
        }

        return ruta
    }

    //https://router.project-osrm.org/route/v1/foot/-3.608628,37.173484;-3.607011,37.175732?steps=true&geometries=polyline
    public fun parseResultRuta (obj : JSONObject) : objectRuta{
        var jsonArrayAux : JSONArray? = null

        var jsonObjectAux : JSONObject? = null

        var jsonArraySteps : JSONArray? = null
        var polilineaTotal : String? = null

        /*
        routes:
            0:
                legs:
                    0:
                        steps:
                weight_name:
                geometry:
         */

        try {
            jsonArrayAux = obj.getJSONArray("routes") // routes:

            jsonObjectAux = jsonArrayAux.get(0) as JSONObject // 0:

            polilineaTotal = jsonObjectAux.get("geometry") as String? // geometry

            jsonArrayAux = jsonObjectAux.getJSONArray("legs") // legs:
            jsonObjectAux = jsonArrayAux.get(0) as JSONObject// 0:

            jsonArraySteps = jsonObjectAux.getJSONArray("steps") // steps

        } catch (e : JSONException){
            e.printStackTrace()
        }

        var ruta : objectRuta = parseArrayStepsRuta(jsonArraySteps)
        ruta.actualizarPolylineTotal(polilineaTotal)

        return ruta
    }
}



public class objectRuta{
    private var polylineTotal : String? = null
    private var arraySteps : ArrayList<HashMap<String, Object>> = ArrayList()

    public fun actualizarPolylineTotal (nueva : String?) {
        if (nueva != null){
            polylineTotal = nueva
        }
    }

    public fun aniadirStep (nuevo : HashMap<String, Object>) {
        arraySteps.add(nuevo)
    }

    public fun polylineTotal () : String? {
        return polylineTotal
    }

    public fun getStep (i : Int) : HashMap<String, Object>{
        return arraySteps.get(i)
    }

    public fun getName (i : Int) : String? {
        return arraySteps.get(i).get("name") as String
    }

    public fun getCoordenadas (i : Int) : LatLng? {
        return arraySteps.get(i).get("coordenadas") as LatLng
    }

    public fun getPolyline (i : Int) : String? {
        return arraySteps.get(i).get("geometries") as String
    }

    public fun getTypeManeuver (i : Int) : String? {
        return arraySteps.get(i).get("typeManeuver") as String
    }

    public fun getIntersections (i : Int) : List<LatLng>? {
        return arraySteps.get(i).get("intersections") as List<LatLng>
    }

    public fun numeroSteps () : Int {
        return arraySteps.size
    }

    public fun getPuntosRuta () : List<LatLng> {
        var lista : MutableList<LatLng> = ArrayList()

        for (step in arraySteps){
            lista.add(step.get("coordenadas") as LatLng)
        }

        return lista
    }
}