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
    private lateinit var textViewTimAndDis: TextView
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

        //Variables a elementos de la interfaz
        ImaAtras= findViewById<ImageView>(R.id.ImaVAtras)
        textViewOrigen = findViewById<TextView>(R.id.txtVOrigen)
        textViewDestination = findViewById<TextView>(R.id.txtVDestino)
        textViewTimAndDis = findViewById<TextView>(R.id.txtVDisAndTim)



        // Establece la actividad en modo pantalla completa sin barra superior
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        //Extra recibe info de la pantalla anterior
        extraOriginName = intent.getStringExtra("Origin")!!
        extraDestinationName = intent.getStringExtra("Destination")!!
        extraOriginLat = intent.getDoubleExtra("Origin_lat",0.0)
        extraOriginLng = intent.getDoubleExtra("Origin_lng",0.0)
        extraDestinationLat = intent.getDoubleExtra("Destination_lat",0.0)
        extraDestinationLng = intent.getDoubleExtra("Destination_lng",0.0)

        //Variables donde se almacenan las coordenadas origen, destino
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

        //Muestra la informacion de origen y destino en la interfaz
        textViewOrigen.text = extraOriginName
        textViewDestination.text = extraDestinationName


        //Coordenadas de origen y destino en log, depuracion manual
        Log.d("LOCALIZACION", "Origin lat: ${originLatLng?.latitude}")
        Log.d("LOCALIZACION", "Origin lng: ${originLatLng?.longitude}")
        Log.d("LOCALIZACION", "Destinatio lat: ${destinationLatLng?.latitude}")
        Log.d("LOCALIZACION", "Destinatio lng: ${destinationLatLng?.longitude}")

        //boton retroceso
        ImaAtras.setOnClickListener { finish() }

    }



    private fun addOriginMarker(){
        //Marcador en el mapa, posicion origen con titulo e icono
        markerOrigin = googleMap?.addMarker(
            MarkerOptions()
                .position(originLatLng!!) //Establece la poscion de origen
                .title("Mi position") //Establece el titulo "Mi posicion
                //Icono
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }
    private fun addDestinationMarker(){
        //Marcador en el mapa, posicion destino con titulo e icono
        markerDestination = googleMap?.addMarker(
            MarkerOptions()
                .position(destinationLatLng!!) //Establece la poscion de destino
                .title("LLegada") ////Establece el titulo "Mi posicion
                //Icono
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
    }

    private fun  easyDrawRoute(){
        //Agrega el origen y la coordenada a la lsita
        //de puntos de paso
        wayPoints.add(originLatLng!!)
        wayPoints.add(destinationLatLng!!)

        directionUtil = DirectionUtil.Builder()
            //API de google par aobtener direccion
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            //Coordenada origen
            .setOrigin(originLatLng!!)
            //Puntos paso de la ruta
            .setWayPoints(wayPoints)
            //objeto GoogleMap
            .setGoogleMap(googleMap!!)
            //Color ruta
            .setPolyLinePrimaryColor(R.color.black)
            //Ancho ruta
            .setPolyLineWidth(10)
            //Animacion ruta
            .setPathAnimation(true)
            //Eventos de la ruta
            .setCallback(this)
            //Coordenada destino
            .setDestination(destinationLatLng!!)
            .build()

        //Inicializacion de la ruta
        directionUtil.initPath()
    }

    override fun onMapReady(map: GoogleMap) {
        //Objeto GoogleMap del mapa a la var global googleMap
        googleMap = map // Asigna el objeto GoogleMap proporcionado por el mapa
        //Zoom del mapa activado
        googleMap?.uiSettings?.isZoomControlsEnabled = true // Habilita los controles de zoom en el mapa

        //Mueve la camara del mapa a el origen con zoom de 13
        googleMap?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(originLatLng!!).zoom(13f).build()
            ))
        //Funcion para dibujar ruta
        //easyDrawRoute()
        //Funcion de marcador origen
        addOriginMarker()
        //Funcion de marcador destino
        addDestinationMarker()

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
        /*Esta funcion se puede utilizar para acciones especificas
        cuando la ubicacion esta activada
        */
    }

    override fun currentLocation(location: Location?) {
        /*
        Esta funcion obtiene la informacion de la ubicacion actual como
        la latitud y longitud.
        Se pueden realizar accion basadas en la ubicacion del dispositov en
        tiempo real
         */
    }

    override fun locationCancelled() {
        /*
        Funcion para cuando la ubicacion e cancelada
        caso 1: el usuario no da permisos.
        caso 2: probleamas en obtener ubicacion.
        Se usa segun los requisitos de la app
         */
    }

    override fun onDestroy() {
        super.onDestroy() // Llama a la implementación de la superclase.

        // Esta función se ejecuta cuando la aplicación se cierra o la actividad se destruye.
        // Puedes realizar tareas de limpieza aquí.
        // En este caso, se llama al método 'endUpdates()' de 'easyWayLocation'
        // para detener las actualizaciones de ubicación en tiempo real.
        easyWayLocation?.endUpdates()

    }

    //Funcion cuando se completa la busqueda de la ruta
    override fun pathFindFinish(

        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
        // polyLineDetailsMap: Un mapa que asocia identificadores de segmentos de la polilínea con objetos PolyLineDataBean.
        // Cada PolyLineDataBean contiene detalles específicos sobre un segmento de la polilínea, como distancia y tiempo.

        // polyLineDetailsArray: Una lista que contiene objetos PolyLineDataBean.
        // Cada elemento en la lista representa información detallada sobre un segmento específico de la polilínea.
        // La lista en su conjunto proporciona detalles sobre toda la ruta.

    )

    {
        //Varibles de distancia y tiempo
        var p: String=""
        var q: String=""

        //Extrae la distancia y el tiempo de la ruta obtenida
        var distance = polyLineDetailsArray[1].distance.toDouble() //metros
        var time = polyLineDetailsArray[1].time.toDouble() //segundos

        // Unidad de medida de la distancia
            if(distance < 1.0){
                p="centimetros"
            }
            if(distance >= 1.0 && distance<1000.0){
                p="metros"
            }
            if(distance >= 1000.0){
                distance = distance / 1000.0
                p="kilometros"
            }

        // Unidad de medida del tiempo
            if(time < 60.0){
                q="segundos"
            }
            if(time>=60.0&& time<3600){
                time = time / 60.0
                q="minutos"
            }
            if(time>=3600.0) {
                time = time / 3600.0
                q = "horas"
            }

        /*if(p=="centimetros"&&q=="segundos"){
            val timeString = String.format("%.2f",time)
            val distanceString = String.format("%.2f",distance)
            textViewTimAndDis.text = "$timeString seg. - $distanceString cm."
        }
        if(p=="metros"&&q=="minutos"){
            val timeString = String.format("%.2f",time)
            val distanceString = String.format("%.2f",distance)
            textViewTimAndDis.text = "$timeString min. - $distanceString mts."
        }
        if(p=="kilometros"&&q=="horas"){
            val timeString = String.format("%.2f",time)
            val distanceString = String.format("%.2f",distance)
            textViewTimAndDis.text = "$timeString hrs. - $distanceString km."
        }

        if(p=="centimetros"&&q=="minutos"){
            val timeString = String.format("%.2f",time)
            val distanceString = String.format("%.2f",distance)
            textViewTimAndDis.text = "$timeString min. - $distanceString cm."
        }
        if(p=="centimetros"&&q=="horas"){
            val timeString = String.format("%.2f",time)
            val distanceString = String.format("%.2f",distance)
            textViewTimAndDis.text = "$timeString hrs. - $distanceString cm."
        }
        if(p=="metros"&&q=="segundos"){
            val timeString = String.format("%.2f",time)
            val distanceString = String.format("%.2f",distance)
            textViewTimAndDis.text = "$timeString seg. - $distanceString mts."
        }
        if(p=="metros"&&q=="horas"){
            val timeString = String.format("%.2f",time)
            val distanceString = String.format("%.2f",distance)
            textViewTimAndDis.text = "$timeString hrs. - $distanceString mts."
        }
        if(p=="kilometros"&&q=="segundos"){
            val timeString = String.format("%.2f",time)
            val distanceString = String.format("%.2f",distance)
            textViewTimAndDis.text = "$timeString seg. - $distanceString km."
        }
        if(p=="kilometros"&&q=="minutos"){
            val timeString = String.format("%.2f",time)
            val distanceString = String.format("%.2f",distance)
            textViewTimAndDis.text = "$timeString min. - $distanceString km."
        }*/

        if ((p == "metros" || p == "kilometros") && q == "segundos") {
            // Si la distancia es en metros o kilómetros y el tiempo en segundos
            val timeString = String.format("%.2f", time)
            val distanceString = String.format("%.2f", distance)
            textViewTimAndDis.text = "$timeString seg. - $distanceString ${if (p == "metros") "mts." else "km."}"
        }

        if ((p == "metros" || p == "kilometros") && q == "minutos") {
            // Si la distancia es en metros o kilómetros y el tiempo en minutos
            val timeString = String.format("%.2f", time)
            val distanceString = String.format("%.2f", distance)
            textViewTimAndDis.text = "$timeString min. - $distanceString ${if (p == "metros") "mts." else "km."}"
        }

        if ((p == "metros" || p == "kilometros") && q == "horas") {
            // Si la distancia es en metros o kilómetros y el tiempo en horas
            val timeString = String.format("%.2f", time)
            val distanceString = String.format("%.2f", distance)
            textViewTimAndDis.text = "$timeString hrs. - $distanceString ${if (p == "metros") "mts." else "km."}"
        }


   //Dibuja la ruta en el mapa
        directionUtil.drawPath(WAY_POINT_TAG)
    }


}



