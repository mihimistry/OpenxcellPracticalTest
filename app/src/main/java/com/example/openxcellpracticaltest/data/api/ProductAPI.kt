package com.example.openxcellpracticaltest.data.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface ProductAPI {

    @GET("v1/2e6b2603")
    fun getProductList(): Call<ResponseBody>
}