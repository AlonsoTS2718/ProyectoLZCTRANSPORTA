package com.example.proyectolzctransporta

import android.Manifest
import android.content.Context

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.proyectolzctransporta.LoginAppActivity.Companion.useremail
import com.example.proyectolzctransporta.R.*
import com.example.proyectolzctransporta.models.DriverLocation
import com.example.proyectolzctransporta.utils.CarMoveAnim
import com.google.android.gms.common.api.Status
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
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.oAuthProvider
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.SphericalUtil
import org.imperiumlabs.geofirestore.GeoQuery
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener


class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    // Variables para mapa y ubicación en tiempo real
    private lateinit var googleMap: GoogleMap  // Referencia al mapa
    private var easyWayLocation: EasyWayLocation? = null // Gestión de ubicación
    private var myLocationLatLng: LatLng? = null // Coordenadas de la ubicación actual

    //  private var markerUser: Marker? = null // Marcador de usuario en el mapa
    //  private lateinit var btnActivarServicio: Button
    //  private lateinit var btnDesactivarServicio: Button
    private val aunProvider = AunProvider()
    private val geoProvider = GeoProviders()
    private lateinit var btnSolicitarviaje: Button
    private lateinit var btnOp: ImageView



    //Variable google places
    private var places: PlacesClient? = null
    private var autocompleteOrigin: AutocompleteSupportFragment? = null
    private var autocompleteDestination: AutocompleteSupportFragment? = null
    private var originName = ""
    private var destinationName = ""
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var isLocationEnabled = false


    private val driverMarkers = ArrayList<Marker>()
    private val driversLocation = ArrayList<DriverLocation>()

    private var polyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TripInfoActivity", "onCreate called")
        setContentView(R.layout.mapa)

        btnSolicitarviaje = findViewById<Button>(R.id.btnSolicitarCombi)
        btnOp = findViewById<ImageView>(R.id.BtnOp)


        /*

                btnActivarServicio = findViewById<Button>(R.id.btnActivarServicio)
                btnDesactivarServicio = findViewById<Button>(R.id.btnDesactivarServicio)

        */


        // Establece la actividad en modo pantalla completa sin barra superior
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Obtiene una instancia del fragmento de mapa del diseño
        val mapFragment = supportFragmentManager.findFragmentById(id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Configura la solicitud de ubicación en tiempo real
        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        // Inicializa EasyWayLocation para gestionar la ubicación en tiempo real

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)
        //easyWayLocation?.startLocation()
        // Solicita permisos de ubicación al usuario
        locationPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        startGooglePlaces()

        // Llama al método ActivarServicio cuando se haga clic en el botón.
        btnSolicitarviaje.setOnClickListener { goToTripInfo() }
        btnOp.setOnClickListener { showOptionsMenu(it) }
        /*  btnActivarServicio.setOnClickListener {
              ActivarServicio()// Llama al método ActivarServicio cuando se haga clic en el botón.
          }
          btnDesactivarServicio.setOnClickListener {
              DesactivarServicio()// Llama al método DesactivarServicio cuando se haga clic en el botón.
          }
  */


    }

    // Declaramos una variable llamada 'locationPermissions' que se usa para solicitar
    // permisos de ubicación al usuario.
    val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Comprobamos si el dispositivo está ejecutando Android Nougat (versión 24) o superior.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    // Verificamos si el permiso de ubicación precisa (ACCESS_FINE_LOCATION) fue concedido.
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        // El permiso de ubicación precisa fue concedido.
                        Log.d("LOCALIZACION", "Permiso concedido")
                        // Iniciamos la ubicación en tiempo real utilizando 'easyWayLocation'.
                        easyWayLocation?.startLocation()
                        //checkIfDriverConected()
                    }
                    // Verificamos si el permiso de ubicación aproximada (ACCESS_COARSE_LOCATION) fue concedido.
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        // El permiso de ubicación aproximada fue concedido.
                        Log.d("LOCALIZACION", "Permiso concedido con limitación")
                        // En este caso, podríamos tomar medidas alternativas si la ubicación precisa no está disponible.
                        easyWayLocation?.startLocation()
                        //  checkIfDriverConected()
                    }

                    else -> {
                        // Ninguno de los permisos de ubicación fue concedido.
                        Log.d("LOCALIZACION", "Permiso no concedido")
                        // En este punto, podrías informar al usuario que la aplicación no funcionará
                        // correctamente sin permisos de ubicación.
                    }
                }
            }
        }


    /*private fun checkIfDriverConected(){
        geoProvider.getLocation(aunProvider.getId()).addOnSuccessListener { document ->
            if(document.exists()){
                if(document.contains("l")){
                    ActivarServicio()
                }
                else{
                    MostrarBotonSinServicio()

                }
            }
            else{
                MostrarBotonServicio()
            }
        }
    }
*/
    /* private fun saveLocation(){
         if(myLocationLatLng!=null){
             geoProvider.saveLocation(aunProvider.getId(), myLocationLatLng!!)
         }
     }*/

    /*private fun ActivarServicio(){
        easyWayLocation?.endUpdates()
        easyWayLocation?.startLocation()
        MostrarBotonSinServicio()


    }
    private fun DesactivarServicio(){
        easyWayLocation?.endUpdates()
        if(myLocationLatLng != null){
            geoProvider.removeLocation(aunProvider.getId())
            MostrarBotonServicio()

        }

    }
    private fun MostrarBotonServicio(){

        btnActivarServicio.visibility = View.VISIBLE
        btnDesactivarServicio.visibility = View.GONE

    }
    private fun MostrarBotonSinServicio(){

        btnActivarServicio.visibility = View.GONE
        btnDesactivarServicio.visibility = View.VISIBLE

    }*/


    // Esta función se encarga de agregar un marcador en el mapa.
    /*private fun addMarker() {
        // Obtenemos un objeto 'Drawable' que representa el icono del marcador.
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.miubi)

        // Creamos un 'BitmapDescriptor' a partir del 'Drawable' usando la función 'getMarkerFromDrawable'.
        val markerIcon = getMarkerFromDrawable(drawable!!)

        // Verificamos si ya existe un marcador en el mapa.
        if (markerUser != null) {
            markerUser?.remove() // Eliminamos el marcador existente para no redibujarlo.
        }

        // Verificamos si tenemos una ubicación (LatLng) disponible.
        if (myLocationLatLng != null) {
            // Creamos un nuevo marcador en el mapa con la ubicación, anclaje, y estilo adecuados.
            markerUser = googleMap?.addMarker(
                MarkerOptions()
                    .position(myLocationLatLng!!)
                    .anchor(0.5f, 0.5f) // Configuramos el punto de anclaje del icono.
                    .flat(true) // Hacemos que el marcador sea plano (sin inclinación).
                    .icon(markerIcon) // Asignamos el icono al marcador.
            )
        }
    }
*/
    /*
        // Esta función toma un objeto 'Drawable' y devuelve un 'BitmapDescriptor' que se puede utilizar como icono de marcador.
        private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor {
            // Creamos un lienzo (canvas) para dibujar el 'Drawable' en un 'Bitmap'.
            val canvas = Canvas()

            // Creamos un 'Bitmap' con el tamaño especificado (100x100 píxeles) y configuración ARGB_8888.
            val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

            // Establecemos el lienzo para que dibuje en el 'Bitmap'.
            canvas.setBitmap(bitmap)

            // Establecemos los límites del 'Drawable' en el lienzo. En este caso, se ajusta al tamaño del 'Bitmap'.
            drawable.setBounds(0, 0, 100, 100)

            // Dibujamos el 'Drawable' en el lienzo.
            drawable.draw(canvas)

            // Convertimos el 'Bitmap' en un 'BitmapDescriptor' utilizando 'BitmapDescriptorFactory'.
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }




    */

    private fun createPolyLines(ruta: List<LatLng>) {
        //val initialCoordenates = LatLng(18.02540503645077, -102.21606601152054)
        polyline?.remove()
        val polylineOptions: PolylineOptions = PolylineOptions()
            .width(15f)
            .color(ContextCompat.getColor(this, R.color.azulRuta))
        for (i in ruta) {
            polylineOptions.add(i)
        }
        polyline = googleMap.addPolyline(polylineOptions)

    }


    private fun showOptionsMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.opciones_menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.OpFlor -> {
                    showToast("Ruta flor de abril seleccionada")
                    createPolyLines(Rutas.RUTA_FLOR_ABRIL)
                    true
                }

                R.id.Op5 -> {
                    showToast("Ruta 05 de mayo seleccionada")
                    createPolyLines(Rutas.RUTA_05_MAYO)
                    true
                }

                R.id.OpBascula -> {
                    showToast("Ruta Bascula seleccionada")
                    createPolyLines(Rutas.RUTA_BASCULA)
                    true
                }

                R.id.OpPrincipal -> {
                    showToast("Ruta bascula principal seleccionada")
                    createPolyLines(Rutas.RUTA_PRINCIPAL)
                    true
                }

                R.id.OpAlondra -> {
                    showToast("Ruta alondra seleccionada")
                    createPolyLines(Rutas.RUTA_ALONDRA)
                    true
                }

                R.id.OpPuesta -> {
                    createPolyLines(Rutas.RUTA_PUESTA_SOL)
                    showToast("Ruta puesta del sol seleccionada")
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getNearbyDrivers() {

        if (myLocationLatLng == null) return
        geoProvider.getNearbyDrivers(myLocationLatLng!!, 20.0)
            .addGeoQueryEventListener(object : GeoQueryEventListener {

                override fun onKeyEntered(documentID: String, location: GeoPoint) {
                    Log.d("FIRESTORE", "Document id: $documentID")
                    Log.d("FIRESTORE", "location: $location")
                    for (marker in driverMarkers) {
                        if (marker.tag != null) {
                            if (marker.tag == documentID) {
                                return
                            }
                        }
                    }


                    //CREAR UN NUEVO MRCADOR PARA EL CONDUCTOR CONECTADO
                    val driverLatLng = LatLng(location.latitude, location.longitude)


                    val marker = googleMap?.addMarker(
                        MarkerOptions().position(driverLatLng).title("Condutor disponible").icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.combi2)

                        )
                    )

                    marker?.tag = documentID
                    driverMarkers.add(marker!!)

                    val dl = DriverLocation()
                    dl.id = documentID
                    driversLocation.add(dl)
                }

                override fun onKeyExited(documentID: String) {
                    for (marker in driverMarkers) {
                        if (marker.tag != null) {
                            if (marker.tag == documentID) {
                                marker.remove()
                                driverMarkers.remove(marker)
                                driversLocation.removeAt(getPositionDriver(documentID))
                                return
                            }
                        }
                    }
                }

                override fun onKeyMoved(documentID: String, location: GeoPoint) {
                    for (marker in driverMarkers) {
                        val start = LatLng(location.latitude, location.longitude)
                        var end: LatLng? = null
                        val position = getPositionDriver(marker.tag.toString())

                        if (marker.tag != null) {
                            if (marker.tag == documentID) {
                                //marker.position = LatLng(location.latitude, location.longitude)
                                if (driversLocation[position].latlng != null) {
                                    end = driversLocation[position].latlng
                                }
                                driversLocation[position].latlng =
                                    LatLng(location.latitude, location.longitude)
                                if (end != null) {
                                    CarMoveAnim.carAnim(marker, end, start)
                                }

                            }
                        }
                    }
                }

                override fun onGeoQueryError(exception: Exception) {

                }

                override fun onGeoQueryReady() {

                }


            })

    }

    private fun goToTripInfo() {
        Log.d("MapActivity", "goToTripInfo() called")

        if (originLatLng != null && destinationLatLng != null) {

            val i = Intent(this, TripInfoActivity::class.java)
            i.putExtra("Origin", originName)
            i.putExtra("Destination", destinationName)
            i.putExtra("Origin_lat", originLatLng?.latitude)
            i.putExtra("Origin_lng", originLatLng?.longitude)
            i.putExtra("Destination_lat", destinationLatLng?.latitude)
            i.putExtra("Destination_lng", destinationLatLng?.longitude)
            startActivity(i)
        } else {
            Toast.makeText(this, "Debes seleccionar el origen y el destino", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getPositionDriver(id: String): Int {
        var position = 0
        for (i in driversLocation.indices) {
            if (id == driversLocation[i].id) {
                position = 1
                break
            }
        }
        return position
    }


    private fun onCameraMove() {
        googleMap?.setOnCameraIdleListener {
            try {
                val geocoder = Geocoder(this)
                originLatLng = googleMap?.cameraPosition?.target

                if (originLatLng != null) {
                    val addressList = geocoder.getFromLocation(
                        originLatLng?.latitude!!,
                        originLatLng?.longitude!!,
                        1
                    )
                    if (addressList != null) {
                        if (addressList.size > 0) {
                            val city = addressList.get(0).locality
                            val country = addressList.get(0).countryName
                            val addres = addressList.get(0).getAddressLine(0)
                            originName = "$addres $city"
                            autocompleteOrigin?.setText("$addres $city")

                        }
                    }
                }


            } catch (e: Exception) {
                Log.d("ERROR", "Mensaje error: ${e.message}")
            }
        }
    }

    private fun startGooglePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, resources.getString(R.string.google_maps_key))

        }

        places = Places.createClient(this)
        instanceAutocompleteOrigin()
        instanceAutocompleteDestination()

    }

    //Busqueda 5k a la redonda
    private fun limitSearch() {
        val northSide = SphericalUtil.computeOffset(myLocationLatLng, 5000.0, 0.0)
        val southSide = SphericalUtil.computeOffset(myLocationLatLng, 5000.0, 180.0)


        autocompleteOrigin?.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
        autocompleteDestination?.setLocationBias(
            RectangularBounds.newInstance(
                southSide,
                northSide
            )
        )
    }

    private fun instanceAutocompleteOrigin() {
        autocompleteOrigin =
            supportFragmentManager.findFragmentById(R.id.placesAutocompleteOrigin) as AutocompleteSupportFragment
        autocompleteOrigin?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
            )
        )
        autocompleteOrigin?.setHint("Lugar de recogida")
        autocompleteOrigin?.setCountry("MX")
        autocompleteOrigin?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                originName = place.name!!
                originLatLng = place.latLng
                Log.d("PLACES", "Address: $originName")
                Log.d("PLACES", "LAT: ${originLatLng?.latitude}")
                Log.d("PLACES", "LNG: ${originLatLng?.longitude}")
            }

            override fun onError(p0: Status) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun instanceAutocompleteDestination() {
        autocompleteDestination =
            supportFragmentManager.findFragmentById(R.id.placesAutocompleteDestination) as AutocompleteSupportFragment
        autocompleteDestination?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
            )
        )
        autocompleteDestination?.setHint("Destino")
        autocompleteDestination?.setCountry("MX")
        autocompleteDestination?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                destinationName = place.name!!
                destinationLatLng = place.latLng
                Log.d("PLACES", "Address: $destinationName")
                Log.d("PLACES", "LAT: ${destinationLatLng?.latitude}")
                Log.d("PLACES", "LNG: ${destinationLatLng?.longitude}")
            }

            override fun onError(p0: Status) {
                TODO("Not yet implemented")
            }
        })
    }


    // Esta función se llama cuando la actividad está en el estado "en pausa" y se reanuda a su estado anterior.
    override fun onResume() {
        super.onResume() // Llama a la implementación de la superclase.

        // Aquí puedes realizar tareas específicas que deben realizarse cuando la actividad se reanuda.
        // En este caso, no se realiza ninguna acción adicional, pero puedes agregar código adicional si es necesario.
    }

    // Esta función se llama cuando la actividad se está destruyendo o pasando a otro estado.
    override fun onDestroy() {
        super.onDestroy() // Llama a la implementación de la superclase.

        // Esta función se ejecuta cuando la aplicación se cierra o la actividad se destruye. Puedes realizar tareas de limpieza aquí.
        // En este caso, se llama al método 'endUpdates()' de 'easyWayLocation' para detener las actualizaciones de ubicación en tiempo real.
        easyWayLocation?.endUpdates()
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map // Asigna el objeto GoogleMap proporcionado por el mapa
        googleMap?.uiSettings?.isZoomControlsEnabled =
            true // Habilita los controles de zoom en el mapa
        onCameraMove()
        // Comienza la actualización de la ubicación en tiempo real
        easyWayLocation?.startLocation()

        // Verifica si la aplicación tiene permisos para acceder a la ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no se tienen permisos, se sale de la función
            return
        }

        // Deshabilita la capa de "Mi ubicación" en el mapa
        googleMap?.isMyLocationEnabled = false

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

        // Deshabilita la capacidad de rotar el mapa con gestos y los controles de zoom en el mapa
        googleMap?.uiSettings?.isRotateGesturesEnabled = false
        googleMap?.uiSettings?.isZoomControlsEnabled = false
    }


    override fun locationOn() {
        // Este método se ejecuta cuando la ubicación está activada, pero en este caso no realiza ninguna acción específica.
        // Puede ser utilizado para manejar eventos relacionados con la activación de la ubicación.
    }

    override fun currentLocation(location: Location) { // Actualización de la posición en tiempo real
        // Este método se llama cuando se obtiene una actualización de la ubicación en tiempo real.

        // Se crea un objeto `LatLng` que representa la ubicación actual utilizando la latitud y longitud proporcionadas por el objeto `location`.
        myLocationLatLng = LatLng(location.latitude, location.longitude)

        // Mueve la cámara del mapa a la nueva ubicación con un nivel de zoom de 17 (más cercano).
        /* googleMap?.moveCamera(
             CameraUpdateFactory.newCameraPosition(
                 CameraPosition.builder().target(myLocationLatLng!!).zoom(17f).build()
             ))*/

        if (!isLocationEnabled) {//Una sola vez

            isLocationEnabled = true

            // Mueve la cámara del mapa a la nueva ubicación con un nivel de zoom de 15 (más cercano).
            googleMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder().target(myLocationLatLng!!).zoom(15f).build()
                )
            )
            getNearbyDrivers()
            limitSearch()
        }

        // Llama a la función `addMarker()` para agregar un marcador en la nueva ubicación.
        /* addMarker()
         saveLocation()*/
    }

    override fun locationCancelled() {
        // Este método se llama cuando la actualización de la ubicación se cancela o detiene.

        // En este caso, no se realiza ninguna acción específica en caso de cancelación de la ubicación.
        // Puede ser utilizado para manejar eventos relacionados con la cancelación de la actualización de la ubicación.
    }

    /*
        // Método para cerrar sesión
        fun signOut(view: View) {
            // Este método se invoca cuando se hace clic en un elemento de la vista (por ejemplo, un botón) con el atributo `android:onClick="signOut"` en el archivo XML.
            // El parámetro `view` es una referencia al elemento de la vista que se hace clic, pero en este caso, no se utiliza.

            // Llama a la función `signOut()` para cerrar la sesión del usuario.
            signOut()
        }

        private fun signOut(){
            // Esta función privada se encarga de cerrar la sesión del usuario.
            // Obtiene una instancia de FirebaseAuth (autenticación de Firebase) y utiliza `signOut()` para cerrar la sesión del usuario actual.
            FirebaseAuth.getInstance().signOut()
            // Crea un Intent para redirigir al usuario a la actividad de inicio de sesión (LoginAppActivity).
            val intent = Intent(this, LoginAppActivity::class.java)
            // Inicia la actividad de inicio de sesión.
            startActivity(intent)
        }
    */
    fun callSignOut(view: View) {
        signOut()
    }

    private fun signOut() {
        useremail = ""

        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginAppActivity::class.java))
    }


}

