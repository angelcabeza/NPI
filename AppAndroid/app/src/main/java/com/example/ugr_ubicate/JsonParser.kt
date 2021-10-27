package com.example.ugr_ubicate

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

public class JsonParser {
    val amenitiesStreet : Array<String> = arrayOf("bank", "hospital", "bar")

    private fun parseJsonObject (obj : JSONObject) : HashMap<String, Object> {
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
            //var name : String = obj.get("name") as String
            //var longitud : String = obj.getJSONObject("geometry").getJSONObject("location").get("lng") as String
            //var longitud : String = obj.getJSONObject("center").get("lon") as String
            //var latitud : String = obj.getJSONObject("geometry").getJSONObject("location").get("lat") as String
            //var latitud : String = obj.getJSONObject("center").get("lat") as String

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

    private fun parseJsonArray (jsonArray: JSONArray?) : List<HashMap<String, Object>>{
        var dataList : MutableList<HashMap<String, Object>> = ArrayList()

        var i : Int = 0

        if (jsonArray != null) {
            while (i < jsonArray.length()){
                try {
                    var data : HashMap<String, Object> = parseJsonObject(jsonArray?.get(i) as JSONObject)

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

    public fun parseResult (obj : JSONObject) : List<HashMap<String, Object>>{
        var jsonArray : JSONArray? = null

        try {
            jsonArray = obj.getJSONArray("elements")
        } catch (e : JSONException){
            e.printStackTrace()
        }

        return parseJsonArray(jsonArray)
    }
}

