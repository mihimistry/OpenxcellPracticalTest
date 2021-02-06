package com.example.openxcellpracticaltest.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIObject {

    private var apiObject: ProductAPI? = null

    fun getInstance(): ProductAPI? {
        if (apiObject == null) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiObject = retrofit.create(ProductAPI::class.java)
        }
        return apiObject
    }

    companion object {
        const val BASE_URL = "https://api.mocki.io/"
    }
}