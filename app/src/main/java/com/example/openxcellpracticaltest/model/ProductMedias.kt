package com.example.openxcellpracticaltest.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "product_medias")
data class ProductMedias(

    @PrimaryKey(autoGenerate = true) val id: Int,
    @SerializedName("media_name") val media_name: String,
    @SerializedName("media_path") val media_path: String
)