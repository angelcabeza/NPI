package com.example.ugr_ubicate

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

public class JsonParser {
    private fun parseJsonObject (obj : JSONObject) : HashMap<String, String> {
        var dataList : HashMap<String, String> = HashMap()

        try {
            var name : String = obj.get("name") as String

            var latitud : String = obj.getJSONObject("geometry").getJSONObject("location").get("lat") as String

            var longitud : String = obj.getJSONObject("geometry").getJSONObject("location").get("lng") as String

            dataList.put("name", name)
            dataList.put("lat", latitud)
            dataList.put("lng", longitud)

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
            jsonArray = obj.getJSONArray("results")
        } catch (e : JSONException){
            e.printStackTrace()
        }

        return parseJsonArray(jsonArray)
    }
}

