package com.example.openxcellpracticaltest.ui.fragment

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.openxcellpracticaltest.R
import com.example.openxcellpracticaltest.adapter.OnViewMapClickedListener
import com.example.openxcellpracticaltest.databinding.FragmentMapBinding
import com.example.openxcellpracticaltest.model.PolylineData
import com.example.openxcellpracticaltest.model.ProductItem
import com.example.openxcellpracticaltest.ui.MainActivity
import com.example.openxcellpracticaltest.utils.AppUtils
import com.example.openxcellpracticaltest.viewmodel.MapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.maps.GeoApiContext
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import kotlinx.android.synthetic.main.fragment_map.*

class MapFragment(private val application: Application) : Fragment(), OnMapReadyCallback,
    OnViewMapClickedListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnMarkerClickListener {

    private lateinit var viewBinding: FragmentMapBinding
    private lateinit var productList: LiveData<List<ProductItem>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private var result: LiveData<DirectionsResult>? = null
    private lateinit var viewModel: MapViewModel
    private var pList = ArrayList<ProductItem>()
    private var markerList = ArrayList<Marker>()
    private var polylines = ArrayList<PolylineData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentMapBinding.inflate(layoutInflater, container, false)
        initMap()
        return viewBinding.root
    }

    private fun initMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        MainActivity.listener = this
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, factory).get(MapViewModel::class.java)

        getProductList()
    }


    private fun getProductList() {
        activity?.let { AppUtils.showProgress(it, "Loading..", "Please wait", false) }
        productList = viewModel.getProductList()

        productList.observe(this, Observer { list ->
            if (list != null) {
                AppUtils.dismissProgress()

                if (activity?.let { it1 -> AppUtils.isNetworkAvailable(it1) } == true) {
                    if (currentLocation != null) {
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
                            })
                            markerList.add(
                                createMarker(
                                    product.lat,
                                    product.lang,
                                    product.title,
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                )!!
                            )
                        }
                        viewModel.updateListInRoom(pList)

                    } else {
                        Toast.makeText(
                            activity,
                            "Current location not found, Retry!",
                            Toast.LENGTH_SHORT
                        ).show()
                        getCurrentLocation()
                    }
                } else {
                    for (product in list) {
                        pList.add(product)
                        markerList.add(
                            createMarker(
                                product.lat,
                                product.lang,
                                product.title,
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            )!!
                        )
                    }
                }
            } else {
                AppUtils.dismissProgress()
                Snackbar.make(viewBinding.root, "Data not found!", 2000).show()
            }
        })
    }

    private fun getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (activity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && activity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(activity, "Location permission required", Toast.LENGTH_SHORT).show()
        } else {
            val location = fusedLocationClient.lastLocation

            location.addOnCompleteListener {

                if (it.isSuccessful) {
                    currentLocation = it.result

                    if (currentLocation != null)
                        moveCamera(
                            LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                            DEFAULT_ZOOM,
                            "My Location",
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )

                    map.setOnMarkerClickListener(this)

                    map.setOnPolylineClickListener(this)
                }
            }
        }
    }

    private fun moveCamera(
        latLng: LatLng,
        zoom: Float,
        title: String,
        defaultMarker: BitmapDescriptor
    ) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        val markerOptions = MarkerOptions().position(latLng).title(title).icon(defaultMarker)
        map.addMarker(markerOptions)
    }

    private fun createMarker(
        latitude: Double,
        longitude: Double,
        title: String?,
        defaultMarker: BitmapDescriptor
    ): Marker? {
        return map.addMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title(title).icon(defaultMarker)
        )
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            map = p0
        }

        if (mGeoApiContext == null) {
            mGeoApiContext = GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_api_key))
                .build()
        }
        getCurrentLocation()

    }

    private fun getProductDetails(marker: Marker) {
        for (product in pList) {

            if (product.title.contains(marker.title)) {

                showProductDetails(
                    product.title,
                    product.description,
                    product.distance,
                    product.product_medias[0].media_path
                )
            }

        }
    }

    private fun showProductDetails(
        title: String?,
        description: String?,
        distance: String?,
        thumbnailUrl: String?
    ) {
        cv_details.visibility = View.VISIBLE

        Glide.with(this).load(thumbnailUrl).into(viewBinding.image)

        viewBinding.tvTitle.text = title
        viewBinding.tvDescription.text = description
        viewBinding.tvDistance.text = distance

    }

    private fun addPolylines(result: LiveData<DirectionsResult>) {
        result.observe(this, Observer {
            Handler(Looper.getMainLooper()).post(Runnable {
                if (!polylines.isNullOrEmpty()) {
                    for (polylineData in polylines) {
                        polylineData.polyline.remove()
                    }
                    polylines.clear()
                    polylines = ArrayList()
                }
                for (route in it.routes) {
                    val decodedPath: List<com.google.maps.model.LatLng> =
                        PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                    val newDecodedPath = ArrayList<LatLng>()

                    for (latLng in decodedPath) {
                        newDecodedPath.add(
                            LatLng(
                                latLng.lat,
                                latLng.lng
                            )
                        )
                    }

                    val polyline: Polyline =
                        map.addPolyline(PolylineOptions().addAll(newDecodedPath))
                    polyline.color =
                        ContextCompat.getColor(requireActivity(), android.R.color.darker_gray)
                    polyline.isClickable = true
                    polylines.add(PolylineData(polyline, route.legs[0]))
                }
            })
        })

    }

    override fun viewOnMap(productItem: ProductItem) {
        map.clear()
        for (marker in markerList) {
            if (marker.title == productItem.title) {
                moveCamera(
                    LatLng(productItem.lat, productItem.lang),
                    DEFAULT_ZOOM,
                    productItem.title,
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
                createMarker(
                    currentLocation!!.latitude,
                    currentLocation!!.longitude,
                    "My Location",
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
                cv_details.visibility = View.VISIBLE
                getProductDetails(marker)
                result?.let { addPolylines(it) }
            }
        }
    }

    override fun onPolylineClick(polyline: Polyline?) {
        for (polylineData in polylines) {
            if (polyline?.id!! == polylineData.polyline.id) {

                polylineData.polyline.color =
                    ContextCompat.getColor(requireActivity(), android.R.color.holo_blue_dark)
                polylineData.polyline.zIndex = 1F
            } else {

                polylineData.polyline.color =
                    ContextCompat.getColor(requireActivity(), android.R.color.darker_gray)
                polylineData.polyline.zIndex = 0F
            }
        }

    }

    override fun onMarkerClick(marker: Marker): Boolean {

        getProductDetails(marker)
        return true
    }


    companion object {
        private const val TAG = "MapFragment"
        private const val DEFAULT_ZOOM = 10f
        var currentLocation: Location? = null
        var mGeoApiContext: GeoApiContext? = null
    }


}