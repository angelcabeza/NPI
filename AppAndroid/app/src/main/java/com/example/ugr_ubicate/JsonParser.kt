package com.example.ugr_ubicate

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

public class JsonParser {
    val amenitiesStreet : Array<String> = arrayOf("bank", "hospital", "bar")

    private fun parseJsonObject (obj : JSONObject) : HashMap<String, String> {
        var dataList : HashMap<String, String> = HashMap()

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

            var name : String? = obj.getJSONObject("tags").get("name") as String

            var amenity : String? = obj.getJSONObject("tags").get("amenity") as String

            var street : String? = null
            if (amenity in amenitiesStreet){
                street = obj.getJSONObject("tags").get("addr:street") as String

                if (street == null){
                    name = null
                }
            }

            var healthcare : String? = null
            if (amenity == "hospital"){
                healthcare = obj.getJSONObject("tags").get("healthcare") as String

                if (healthcare == null){
                    name = null
                }
            }

            var latitud : String? = obj.get("lat") as String
            var longitud : String? = obj.get("lon") as String

            if (latitud == null || longitud == null){
                latitud = obj.getJSONObject("center").get("lat") as String
                longitud = obj.getJSONObject("center").get("lon") as String
            }


            if (name != null && latitud != null && longitud != null) {
                dataList.put("name", name)
                dataList.put("lng", latitud)
                dataList.put("lng", longitud)
            }

        } catch (e : JSONException){
            e.printStackTrace()
        }

        return dataList
    }

    private fun parseJsonArray (jsonArray: JSONArray?) : List<HashMap<String, String>>{
        var dataList : MutableList<HashMap<String, String>> = ArrayList()

        var i : Int = 0

        if (jsonArray != null) {
            while (i < jsonArray.length()){
                try {
                    var data : HashMap<String, String> = parseJsonObject(jsonArray?.get(i) as JSONObject)

                    dataList.add(data)

                } catch (e : JSONException){
                    e.printStackTrace()
                }

                i++
            }
        }

        return dataList
    }

    public fun parseResult (obj : JSONObject) : List<HashMap<String, String>>{
        var jsonArray : JSONArray? = null

        try {
            jsonArray = obj.getJSONArray("elements")
        } catch (e : JSONException){
            e.printStackTrace()
        }

        return parseJsonArray(jsonArray)
    }
}

