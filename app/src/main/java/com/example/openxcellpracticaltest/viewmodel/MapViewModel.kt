package com.example.openxcellpracticaltest.viewmodel

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.openxcellpracticaltest.data.repo.MapRepository
import com.example.openxcellpracticaltest.model.ProductItem
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.LatLng

class MapViewModel(application: Application) : AndroidViewModel(application) {

    var title: String? = null
    var description: String? = null
    var distance: String? = null
    private val mapRepository = MapRepository(application)

    fun getProductList() = mapRepository.getProductList()

    fun getDirectionResult(
        destination: LatLng,
        origin: LatLng,
        mGeoApiContext: GeoApiContext
    ): LiveData<DirectionsResult> =
        mapRepository.getDirectionResult(destination, origin, mGeoApiContext)

    fun updateListInRoom(updatedList: List<ProductItem>?) {
        updatedList?.let { mapRepository.addProductInRoom(it) }
    }

}