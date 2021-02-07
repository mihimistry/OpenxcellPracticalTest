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
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.openxcellpracticaltest.R
import com.example.openxcellpracticaltest.adapter.OnViewMapClickedListener
import com.example.openxcellpracticaltest.adapter.ProductListAdapter
import com.example.openxcellpracticaltest.adapter.ProductPagerAdapter
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
import com.google.android.material.tabs.TabLayoutMediator
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

class MainActivity : AppCompatActivity(), OnViewMapClickedListener {

    private lateinit var viewBinding: ActivityMainBinding
    private val tabTitles = arrayOf("Map", "List")
    private var pagerAdapter: ProductPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        ProductListAdapter.listener = this
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (isPermissionGranted()) {
            initUi()
        } else {
            requestPermissions()
        }
    }

    private fun initUi() {
        tabs.addTab(tabs.newTab().setText("Map"))
        tabs.addTab(tabs.newTab().setText("List"))

        viewPager.isUserInputEnabled = false
        pagerAdapter =
            ProductPagerAdapter(application, supportFragmentManager, lifecycle, tabs.tabCount)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = tabTitles[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show()
                initUi()
            } else {
                requestPermissions()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
        var listener: OnViewMapClickedListener? = null
    }

    override fun viewOnMap(productItem: ProductItem) {
        tabs.selectTab(tabs.getTabAt(0))
        listener?.viewOnMap(productItem)
    }

}