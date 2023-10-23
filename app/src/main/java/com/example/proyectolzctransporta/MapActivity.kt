package com.example.proyectolzctransporta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.proyectolzctransporta.R.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth


class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    //Variables para mapa y ubicación en tiempo real
    private var googleMap: GoogleMap? = null //MAPA
    private var easyWayLocation: EasyWayLocation? = null //UBICACION
    private var myLocationLatLng: LatLng? = null
    private var markerUser: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       setContentView(layout.mapa)
        //Sin barrra superio
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        //Invoca al mapa
       val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        //Invoca a ubicacion en tiempo real
        val locationRequest = LocationRequest.create().apply {
            //Configuracion de los intervalos para la actualización de la ubicación
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)

        locationPermissions.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))


    }

    val locationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permission ->

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when{
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                  Log.d("LOCALIZACION","Permiso concedido")
                    easyWayLocation?.startLocation();
                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("LOCALIZACION","Permiso concedido con limitacion")
                }
                else ->{
                    Log.d("LOCALIZACION","Permiso no concedido")
                }
            }
        }

    }


    private fun addMarker(){
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.miubi)
        val markerIcon = getMarkerFromDrawable(drawable!!)
        if(markerUser != null){
            markerUser?.remove() //No redibuja el icono
        }

        if(myLocationLatLng != null){
            markerUser = googleMap?.addMarker(
                MarkerOptions()
                    .position(myLocationLatLng!!)
                    .anchor(0.5f,0.5f)
                    .flat(true)
                    .icon(markerIcon)
            )
        }


    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor{
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            100,
            100,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,100,100)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {//Se ejecuta cuando se cierra la app o se pasa a otra activity
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        easyWayLocation?.startLocation();

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        googleMap?.isMyLocationEnabled = false

        try{
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.stilo)
            )
            if(!success!!){
                Log.d("MAPAS","No se pudo encontrar el estilo")
            }
        }catch(e: Resources.NotFoundException){
            Log.d("MAPAS", "Error: ${e.toString()}" )
        }
        googleMap?.uiSettings?.isRotateGesturesEnabled = false
        googleMap?.uiSettings?.isZoomControlsEnabled = false

    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location) { //Actualizacion de la posicion en tiempo real
        myLocationLatLng = LatLng(location.latitude, location.longitude) //lat y long de la posicion actual

        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(myLocationLatLng!!).zoom(17f).build()

        ))

        addMarker()
    }

    override fun locationCancelled() {

    }

    //CERRAR SESION
    fun callSignOut(view: View){
        signOut()
    }
    private fun signOut(){
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this,LoginAppActivity::class.java))

    }

}

