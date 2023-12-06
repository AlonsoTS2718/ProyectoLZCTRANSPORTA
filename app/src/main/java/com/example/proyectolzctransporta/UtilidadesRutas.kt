package com.example.proyectolzctransporta

import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class UtilidadesRutas(private val googleMap: GoogleMap) {
    private lateinit var polyline: Polyline

    fun createPolyline(coordinatesList: List<LatLng>) {
        val polylineOptions: PolylineOptions = PolylineOptions()
            .addAll(coordinatesList)
            .width(15f)

        polyline = googleMap.addPolyline(polylineOptions)
    }


}