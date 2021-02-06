package com.example.openxcellpracticaltest.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "product_item")
data class ProductItem (

    @PrimaryKey(autoGenerate = true) val p_id: Int,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lang") val lang: Double,
    @SerializedName("title") val title: String,
    @SerializedName("user_id") val user_id: Int,
    @SerializedName("user_name") val user_name: String,
    @SerializedName("user_email") val user_email: String,
    @SerializedName("description") val description: String,
    @SerializedName("product_medias") val product_medias: List<ProductMedias>
)