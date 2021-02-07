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
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.LatLng
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
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
                    // insertProduct(list)
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

    fun addProductInRoom(list: List<ProductItem>) {
        CoroutineScope(IO).launch {
            val oldList = getProductListFromRoom()
            if (oldList.isNullOrEmpty())
                insertProduct(list)
            else {
                deleteAndInsert(list)
            }
        }
    }

    private fun deleteAndInsert(list: List<ProductItem>) {
        Observable.fromCallable { productDao?.deleteAllProducts() }
            .subscribeOn(Schedulers.io())
            .doOnComplete { insertProduct(list) }
            .subscribe()
    }

    fun addDirectionsToRoom(routes: Array<DirectionsRoute>?) {
    }

    fun getDirectionResult(
        destination: LatLng,
        origin: LatLng,
        mGeoApiContext: GeoApiContext
    ): LiveData<DirectionsResult> {
        val directionsResult = MutableLiveData<DirectionsResult>()
        val directions = DirectionsApiRequest(mGeoApiContext)
        directions.alternatives(true)
        directions.origin(origin)
        directions.destination(destination)
            .setCallback(object : PendingResult.Callback<DirectionsResult?> {
                override fun onResult(result: DirectionsResult?) {
                    directionsResult.postValue(result)
                }

                override fun onFailure(e: Throwable) {
                    directionsResult.postValue(null)
                }

            })
        return directionsResult
    }


}