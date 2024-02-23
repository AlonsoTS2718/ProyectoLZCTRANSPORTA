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

        private lateinit var googleMap: GoogleMap  // Referencia al mapa
        private var easyWayLocation: EasyWayLocation? = null // Gestión de ubicación
        private var myLocationLatLng: LatLng? = null // Coordenadas de la ubicación actual
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

        private fun createPolyLines(ruta: List<LatLng>) {
            //val initialCoordenates = LatLng(18.02540503645077, -102.21606601152054)

            //Elimina la poliniea si existe una en el mapa
            polyline?.remove()

            //Configuracion de las opciones de la nueva polilinea
            val polylineOptions: PolylineOptions = PolylineOptions()
                .width(15f) //Anocho de la linea
                //Color de la linea del mapa
                .color(ContextCompat.getColor(this, R.color.azulRuta))

            //Iteraion sobre las coordenas de la ruta y agrega cada punto
            //a la opciones de la polilinea
            for (i in ruta) {
                polylineOptions.add(i)
            }

            //Nueva polilinea
            polyline = googleMap.addPolyline(polylineOptions)

        }


        private fun showOptionsMenu(view: View) {
            //Menu
            val popupMenu = PopupMenu(this, view)
            //Menu inflado
            popupMenu.inflate(R.menu.opciones_menu)

            //Linstener para los clics
            popupMenu.setOnMenuItemClickListener { item ->
                //Opciones
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

                    //No evento
                    else -> false
                }
            }
            //Muestra el menu
            popupMenu.show()
        }

        private fun showToast(message: String) {
            //Mensaje
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

            private fun getNearbyDrivers() {
            //Si las coordenadas de la ubicacoin actual son nulas, sale de la funcion
            if (myLocationLatLng == null) return

                //Obtiene los conductores cercanos a un radio de 20.0 metros
                geoProvider.getNearbyDrivers(myLocationLatLng!!, 20.0)
                .addGeoQueryEventListener(object : GeoQueryEventListener {

                    //Metodo cuando un conductor entra a la zona de busqueda
                    override fun onKeyEntered(documentID: String, location: GeoPoint) {


                        //Id del documento y la ubicacion del conductor en log
                        Log.d("FIRESTORE", "Document id: $documentID")
                        Log.d("FIRESTORE", "location: $location")

                        //Itera a travez de los marcadores de los conductores existentes
                        for (marker in driverMarkers) {
                            //Verifica si el marcador tiene una etiqueta igual a ID del documento actual.
                            if (marker.tag != null) {
                                if (marker.tag == documentID) {
                                    // si existe un marcador con  el mismo ID del documento,
                                    //sale de la funcion
                                    return
                                }
                            }
                        }


                        //CREAR UN NUEVO MaRCADOR con las coordenadas PARA EL CONDUCTOR CONECTADO
                        val driverLatLng = LatLng(location.latitude, location.longitude)

                        //Crea un marcador en el mapa para el conductor con la ubicacion
                        // y el icono especificados
                        val marker = googleMap?.addMarker(
                            MarkerOptions().position(driverLatLng).title("Condutor disponible").icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.combi2)

                            )
                        )

                        //Asigna el ID del documento al marcador y lo agrega a la
                        // lista de marcadores de conductores
                        marker?.tag = documentID
                        driverMarkers.add(marker!!)

                        //Crea un objeto DriveLocation con el ID del documento y lo
                        //agrega a la lista de ubicaciones de conductores
                        val dl = DriverLocation()
                        dl.id = documentID
                        driversLocation.add(dl)
                    }

                    //Metodo cuando un conductor sale de la zona de busqueca
                    override fun onKeyExited(documentID: String) {

                        //Itera a traves de los conductores existente
                        for (marker in driverMarkers) {
                            //Verifica si el conductor tiene una etiqueta
                            // igual al ID del documento actual
                            if (marker.tag != null) {
                                if (marker.tag == documentID) {
                                    //Si existe un marcador con el mismo ID del documento,
                                    //se elimnina del mapa, de la lista de marcadores y de la
                                    //lista de ubicaciones de conductores.
                                    marker.remove()
                                    driverMarkers.remove(marker)
                                    driversLocation.removeAt(getPositionDriver(documentID))
                                   //Sale de la funcion despues de realizar la operaciones
                                    return
                                }
                            }
                        }
                    }

                    //Metodo cuando la ubicacoin de un conductor en tiempo real se mueve
                    override fun onKeyMoved(documentID: String, location: GeoPoint) {

                        //Itera atravez de los marcadores existentes
                        for (marker in driverMarkers) {
                            //Nuevo LatLng para la nueva ubicacion del conductor
                            val start = LatLng(location.latitude, location.longitude)
                            //Varieble end com nula
                            var end: LatLng? = null
                            //Obtiene la posicion del conductor en la lista de ubicaciones
                            // de conductores
                            val position = getPositionDriver(marker.tag.toString())

                            //Verifica si el marcador tiene una etiqueta igual al ID del documento acutal
                            if (marker.tag != null) {
                                if (marker.tag == documentID) {
                                    //marker.position = LatLng(location.latitude, location.longitude)
                                    // Si existe un marcador con el mismo ID del documento, actualiza su posición.
                                    // Si ya había una ubicación anterior del conductor, almacénala en la variable 'end'.
                                    if (driversLocation[position].latlng != null) {
                                        end = driversLocation[position].latlng
                                    }
                                    // Actualiza la ubicacion del conductor en lista de ubicaciones de conductores.
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

