package com.example.openxcellpracticaltest.ui

import android.Manifest
import android.app.PendingIntent.getActivity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.openxcellpracticaltest.R
import com.example.openxcellpracticaltest.databinding.ActivityMainBinding
import com.example.openxcellpracticaltest.model.ProductItem
import com.example.openxcellpracticaltest.utils.AppUtils.Companion.dismissProgress
import com.example.openxcellpracticaltest.utils.AppUtils.Companion.isNetworkAvailable
import com.example.openxcellpracticaltest.viewmodel.MapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var viewModel: MapViewModel
    private lateinit var viewBinding: ActivityMainBinding
    private var productList: LiveData<List<ProductItem>>? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var pList = ArrayList<ProductItem>()
    private lateinit var map: GoogleMap
    private var currentLocation: Location? = null
    private var prevMarker: Marker? = null
    private var distance = MutableLiveData<String>()
    private var mGeoApiContext: GeoApiContext? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, factory).get(MapViewModel::class.java)



        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (isPermissionGranted()) {
            initMap()
        } else {
            requestPermissions()
        }
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getHotelList()
    }

    private fun requestPermissions() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setMessage("Grant Location Permission to access Map")
                .setPositiveButton("Okay") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        PERMISSION_REQUEST_CODE
                    )
                }
                .create().show()
        } else
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getHotelList() {
        productList = viewModel.getProductList()

        productList?.observe(this, Observer { list ->
            if (list != null) {
                dismissProgress()
                pList = list as ArrayList<ProductItem>

                for (product in list) {
                    createMarker(
                        product.lat,
                        product.lang,
                        product.title
                    )
                }

//                mMap.moveCamera(
//                    CameraUpdateFactory.newLatLngZoom(
//                        LatLng(
//                            pList[0].lat,
//                            pList[0].lang
//                        ), 10F
//                    )
//                )
//                showPlaceDetails(
//                    hList[0].name,
//                    hList[0].nameSuffix,
//                    hList[0].starRating,
//                    hList[0].thumbnailUrl
//                )
            } else {
                dismissProgress()
                Snackbar.make(viewBinding.root, "Data not found!", 2000).show()
            }
        })
    }

    private fun getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
        } else {
            val location = fusedLocationClient.lastLocation
            location.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "onComplete: found location!")
                    currentLocation = it.result as Location

                    moveCamera(
                        LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                        DEFAULT_ZOOM, "My Location"
                    )

                }
            }
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float, title: String) {
        Log.d(
            TAG,
            "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        val markerOptions = MarkerOptions().position(latLng).title(title)
        map.addMarker(markerOptions)
    }

    private fun createMarker(
        latitude: Double,
        longitude: Double,
        title: String?
    ): Marker? {
        return map.addMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title(title)
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

        map.setOnMarkerClickListener { marker ->

            cv_details.visibility = View.VISIBLE
            getProductDetails(marker)
            calculateDirections(marker)
            return@setOnMarkerClickListener true
        }

    }


    private fun getProductDetails(marker: Marker) {
        for (product in pList) {

            if (product.title.contains(marker.title)) {
                distance.observe(this, Observer {
                    showProductDetails(
                        product.title,
                        product.description,
                        it,
                        product.product_medias[0].media_path
                    )
                })
            }
        }
    }

    private fun showProductDetails(
        title: String?,
        description: String?,
        distance: String?,
        thumbnailUrl: String?
    ) {
        Glide.with(this).load(thumbnailUrl).into(viewBinding.image)

        viewBinding.tvTitle.text = title
        viewBinding.tvDescription.text = description
        viewBinding.tvDistance.text = distance

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissions()
            }
        }
    }

    private fun calculateDirections(marker: Marker) {
        Log.d(TAG, "calculateDirections: calculating directions.")
        val destination = com.google.maps.model.LatLng(
            marker.position.latitude,
            marker.position.longitude
        )
        val directions = DirectionsApiRequest(mGeoApiContext)
        directions.alternatives(true)
        directions.origin(
            com.google.maps.model.LatLng(
                currentLocation!!.latitude,
                currentLocation!!.longitude
            )
        )

        Log.d(
            TAG,
            "calculateDirections: destination: $destination"
        )
        directions.destination(destination)
            .setCallback(object : PendingResult.Callback<DirectionsResult?> {
                override fun onResult(result: DirectionsResult?) {
                    distance.postValue(result!!.routes[0].legs[0].distance.toString())
                    addPolylinesToMap(result)

                    //viewModel.addDirectionsToRoom(result.routes)
                    Log.d(
                        TAG,
                        "onResult: routes: " + result.routes[0].toString()
                    )
                    Log.d(
                        TAG,
                        "onResult: distance: " + result.routes[0].legs[0].distance.toString()
                    )
                    Log.d(
                        TAG,
                        "onResult: geocodedWayPoints: " + result.geocodedWaypoints?.get(0)
                            .toString()
                    )
                }

                override fun onFailure(e: Throwable) {
                    Log.e(TAG, "onFailure: " + e.message)
                }

            })
    }

    private fun addPolylinesToMap(result: DirectionsResult) {
        Handler(Looper.getMainLooper()).post(Runnable {
            Log.d(
                TAG,
                "run: result routes: " + result.routes.size
            )
            for (route in result.routes) {
                Log.d(
                    TAG,
                    "run: leg: " + route.legs[0].toString()
                )
                val decodedPath: List<com.google.maps.model.LatLng> =
                    PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                val newDecodedPath = ArrayList<LatLng>()

                for (latLng in decodedPath) {

                    Log.d(TAG, "addPolylinesToMap:lat ${latLng.lat}, lng ${latLng.lng}")

                    newDecodedPath.add(
                        LatLng(
                            latLng.lat,
                            latLng.lng
                        )
                    )
                }
                val polyline: Polyline =
                    map.addPolyline(PolylineOptions().addAll(newDecodedPath))
                polyline.color = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
                polyline.isClickable = true
            }
        })
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
        private const val TAG = "MapActivity"
        private const val DEFAULT_ZOOM = 15f
    }

}