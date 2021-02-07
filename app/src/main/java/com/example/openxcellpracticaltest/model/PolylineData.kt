package com.example.openxcellpracticaltest.model

import com.google.android.gms.maps.model.Polyline
import com.google.maps.model.DirectionsLeg

data class PolylineData(
    val polyline: Polyline,
    val directionsLeg: DirectionsLeg
)