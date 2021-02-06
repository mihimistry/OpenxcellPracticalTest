package com.example.openxcellpracticaltest.data.repo

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.openxcellpracticaltest.data.api.APIObject
import com.example.openxcellpracticaltest.data.room.ProductDatabase
import com.example.openxcellpracticaltest.model.ProductItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.model.DirectionsRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapRepository(application: Application) {

    private val TAG = "MapRepository"

    private val database = ProductDatabase.getInstance(application)
    private val productDao = database?.productDao()

    fun getProductList(): LiveData<List<ProductItem>> {
        val productList = MutableLiveData<List<ProductItem>>()
        APIObject().getInstance()?.getProductList()?.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "onFailure: ", t)
                CoroutineScope(IO).launch {
                    productList.postValue(getProductListFromRoom())

                }
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val jsonObject = JSONObject(response.body()?.string())
                    val list: List<ProductItem> = Gson().fromJson(
                        jsonObject.getJSONArray("data").toString(),
                        object : TypeToken<List<ProductItem>>() {}.type
                    )
                    productList.value = list
                    insertProduct(list)
                } else Log.e(TAG, "onResponse: ${response.errorBody()?.string()}")
            }

        })
        return productList
    }

    private fun getProductListFromRoom(): List<ProductItem>? {
        return productDao?.getProducts()
    }

    private fun insertProduct(list: List<ProductItem>) {
        CoroutineScope(IO).launch {
            productDao?.insertProduct(list)
        }
    }

    fun addDirectionsToRoom(routes: Array<DirectionsRoute>?) {
    }

}