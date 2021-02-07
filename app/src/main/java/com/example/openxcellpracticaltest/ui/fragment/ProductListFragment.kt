package com.example.openxcellpracticaltest.ui.fragment

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.openxcellpracticaltest.adapter.ProductListAdapter
import com.example.openxcellpracticaltest.databinding.FragmentProductListBinding
import com.example.openxcellpracticaltest.model.ProductItem
import com.example.openxcellpracticaltest.ui.fragment.MapFragment.Companion.currentLocation
import com.example.openxcellpracticaltest.ui.fragment.MapFragment.Companion.mGeoApiContext
import com.example.openxcellpracticaltest.utils.AppUtils
import com.example.openxcellpracticaltest.viewmodel.MapViewModel
import com.google.maps.model.DirectionsResult
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_product_list.*

class ProductListFragment(private val application: Application) : Fragment() {
    private lateinit var viewBinding: FragmentProductListBinding
    private lateinit var viewModel: MapViewModel
    private var adapter: ProductListAdapter? = null
    private var pList = ArrayList<ProductItem>()
    private lateinit var productList: LiveData<List<ProductItem>>
    private var result: LiveData<DirectionsResult>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentProductListBinding.inflate(layoutInflater)

        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, factory).get(MapViewModel::class.java)

        getProductList()

        return viewBinding.root
    }

    private fun getProductList() {
        productList = viewModel.getProductList()

        productList.observe(this, Observer { list ->
            if (list != null) {
                if (activity?.let { it1 -> AppUtils.isNetworkAvailable(it1) } == true) {
                    if (currentLocation != null)

                        for (product in list) {
                            result = mGeoApiContext?.let {
                                viewModel.getDirectionResult(
                                    com.google.maps.model.LatLng(product.lat, product.lang),
                                    com.google.maps.model.LatLng(
                                        currentLocation!!.latitude,
                                        currentLocation!!.longitude
                                    ), it
                                )
                            }
                            result?.observe(this, Observer {
                                product.distance = it.routes[0].legs[0].distance.toString()
                                pList.add(product)
                                if (pList.size == list.size) {
                                    adapter = ProductListAdapter(activity, pList)
                                    rv_product.adapter = adapter
                                    rv_product.layoutManager = LinearLayoutManager(activity)
                                    viewModel.updateListInRoom(pList)
                                }
                            })

                        } else
                        Toast.makeText(
                            activity,
                            "current location not found!",
                            Toast.LENGTH_SHORT
                        ).show()

                } else {

                    adapter = ProductListAdapter(activity, list)
                    rv_product.adapter = adapter
                    rv_product.layoutManager = LinearLayoutManager(activity)
                }
            }
        })
    }

}
