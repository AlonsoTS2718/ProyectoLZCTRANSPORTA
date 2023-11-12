package com.example.proyectolzctransporta

import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.example.proyectolzctransporta.ui.theme.ProyectoLZCTRANSPORTATheme
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class TripInfoActivity : AppCompatActivity(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {

   // private lateinit var binding: ActivityInfoTripBinding
    // Variables para mapa y ubicación en tiempo real
    private var googleMap: GoogleMap? = null // Referencia al mapa
    private var easyWayLocation: EasyWayLocation? = null // Gestión de ubicación

    private lateinit var ImaAtras: ImageView

    private var extraOriginName = ""
    private var extraDestinationName = ""
    private var extraOriginLat = 0.0
    private var extraOriginLng = 0.0
    private var extraDestinationLat = 0.0
    private var extraDestinationLng = 0.0

    private lateinit var textViewOrigen: TextView
    private lateinit var textViewDestination: TextView
   // private lateinit var textViewOrigen: TextView
   // private lateinit var textViewOrigen: TextView
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var  directionUtil: DirectionUtil

    private var markerOrigin: Marker? = null
    private var markerDestination: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.infotrip)

        ImaAtras= findViewById<ImageView>(R.id.ImaVAtras)
        textViewOrigen = findViewById<TextView>(R.id.txtVOrigen)
        textViewDestination = findViewById<TextView>(R.id.txtVDestino)




        // Establece la actividad en modo pantalla completa sin barra superior
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        //Extra recibe info de la pantalla anterior
        extraOriginName = intent.getStringExtra("Origin")!!
        extraDestinationName = intent.getStringExtra("Destination")!!
        extraOriginLat = intent.getDoubleExtra("Origin_lat",0.0)
        extraOriginLng = intent.getDoubleExtra("Origin_lng",0.0)
        extraDestinationLat = intent.getDoubleExtra("Destination_lat",0.0)
        extraDestinationLng = intent.getDoubleExtra("Destination_lng",0.0)

        originLatLng = LatLng(extraOriginLat,extraOriginLng)
        destinationLatLng = LatLng(extraDestinationLat,extraDestinationLng)


        // Obtiene una instancia del fragmento de mapa del diseño
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        // Configura la solicitud de ubicación en tiempo real
        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)

        textViewOrigen.text = extraOriginName
        textViewDestination.text = extraDestinationName

        Log.d("LOCALIZACION", "Origin lat: ${originLatLng?.latitude}")
        Log.d("LOCALIZACION", "Origin lng: ${originLatLng?.longitude}")
        Log.d("LOCALIZACION", "Destinatio lat: ${destinationLatLng?.latitude}")
        Log.d("LOCALIZACION", "Destinatio lng: ${destinationLatLng?.longitude}")

        ImaAtras.setOnClickListener { finish() }

    }



    private fun addOriginMarker(){
        markerOrigin = googleMap?.addMarker(MarkerOptions().position(originLatLng!!).title("Mi position")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.miubi)))
    }
    private fun addDestinationMarker(){
        markerDestination = googleMap?.addMarker(MarkerOptions().position(destinationLatLng!!).title("LLegada")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
    }

    private fun  easyDrawRoute(){
        wayPoints.add(originLatLng!!)
        wayPoints.add(destinationLatLng!!)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(originLatLng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(destinationLatLng!!)
            .build()

        directionUtil.initPath()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map // Asigna el objeto GoogleMap proporcionado por el mapa
        googleMap?.uiSettings?.isZoomControlsEnabled = true // Habilita los controles de zoom en el mapa

        googleMap?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(originLatLng!!).zoom(13f).build()
            ))

        easyDrawRoute()
       // addOriginMarker()
       // addDestinationMarker()

        try {
            // Intenta establecer un estilo personalizado para el mapa utilizando un archivo JSON (R.raw.stilo)
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.stilo)
            )
            if (!success!!) {
                // Si no se pudo establecer el estilo, registra un mensaje de error en el log
                Log.d("MAPAS", "No se pudo encontrar el estilo")
            }
        } catch (e: Resources.NotFoundException) {
            // Captura una excepción si hay un error al cargar el estilo del mapa y registra un mensaje de error
            Log.d("MAPAS", "Error: ${e.toString()}")
        }

    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {

    }

    override fun locationCancelled() {

    }

    override fun onDestroy() {
        super.onDestroy() // Llama a la implementación de la superclase.

        // Esta función se ejecuta cuando la aplicación se cierra o la actividad se destruye. Puedes realizar tareas de limpieza aquí.
        // En este caso, se llama al método 'endUpdates()' de 'easyWayLocation' para detener las actualizaciones de ubicación en tiempo real.
        easyWayLocation?.endUpdates()
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directionUtil.drawPath(WAY_POINT_TAG)
    }
}

