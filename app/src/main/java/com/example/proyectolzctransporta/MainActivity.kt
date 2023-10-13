package com.example.proyectolzctransporta
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.font.FontVariation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
//import java.util.jar.Manifest
import android.Manifest


class MainActivity : ComponentActivity() {
//fsfaffsdfsfsfsfs
    companion object{
        val REQUIRED_PERMISSIONS_GPS=
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)
    }
    private lateinit var etInicioCorreo: EditText



    //Variable global
     var activatedGPS: Boolean = true

    //Variable para almacenar todos los datos de la geolozalizacion. altitud, latitud, etc.
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION_ID = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        //Incializa la pantalla y pregunta

            preguntaUbi()
            iniPermisosGPS()




    }

    private fun iniPermisosGPS(){
        if(allPermissionsGrantedGPS())
        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this)
        else
            requestPermissionLocation()
    }
    
    private fun requestPermissionLocation(){
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
    }

    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSIONS_GPS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED

    }

    /*
    Funcion para abrir la pantalla de configuración de ubicación del dispositivo,
    Nota
    Permite al usuario realizar cambios en la configuración de ubicación de su dispositivo.
     */

    private fun activacionLocalizacion(){
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    /*
     Funcion parar mmostrar un cuadro de diálogo de alerta que pregunta al usuario si desea activar la ubicación
    */

    private fun preguntaUbi() {

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.tituloUbicacion))
            .setMessage(getString(R.string.preguntaUbicacion))
            .setPositiveButton(R.string.siUbi,
                DialogInterface.OnClickListener { dialog, wich ->

                    activacionLocalizacion()
                })
            .setNegativeButton(R.string.noUbi,
                DialogInterface.OnClickListener { dialog, wich ->
                    Toast.makeText(
                        this,
                        "Debe aceptar el permiso para poder acceder al funcioamiento",
                        Toast.LENGTH_LONG
                    )
                    activatedGPS = false
                    SiUbicacion()
                    preguntaUbi()
                })
            .setCancelable(false)
            .show()

    }



    /*Funcion para verificacion de geolocalización en el dispositivo, consultando los proveedores de ubicación GPS y de red.

    Nota
    Si al menos uno de ellos está habilitado, la función devuelve true,
    lo que indica que la ubicación está habilitada; de lo contrario, devuelve false.
     */

    private fun SiUbicacion(): Boolean{
        var locationManager: LocationManager
        = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }




}


