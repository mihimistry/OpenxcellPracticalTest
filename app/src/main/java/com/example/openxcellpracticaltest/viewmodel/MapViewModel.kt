package com.example.openxcellpracticaltest.viewmodel

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.openxcellpracticaltest.data.repo.MapRepository
import com.example.openxcellpracticaltest.model.ProductItem
import com.google.maps.model.DirectionsRoute

class MapViewModel(application: Application) : AndroidViewModel(application) {

    var title: String? = null
    var description: String? = null
    var distance: String? = null
    private val mapRepository = MapRepository(application)

    fun getProductList() = mapRepository.getProductList()

    fun onViewButtonClicked(view: View) {

    }

    fun addDirectionsToRoom(routes: Array<DirectionsRoute>?) {
        mapRepository.addDirectionsToRoom(routes)
    }
}