package com.example.openxcellpracticaltest.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.model.DirectionsRoute

class Converters {
    @TypeConverter
    fun convertToString(productMedias: List<ProductMedias>): String = Gson().toJson(productMedias)

    @TypeConverter
    fun convertFromString(mediaString: String): List<ProductMedias> =
        Gson().fromJson(mediaString, object : TypeToken<List<ProductMedias>>() {
        }.type)
}