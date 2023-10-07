package com.example.proyectolzctransporta

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.font.FontVariation
import com.google.android.gms.auth.api.identity.BeginSignInRequest

class MainActivity : ComponentActivity() {
    private lateinit var etInicioCorreo: EditText
     var activatedGPS: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        preguntaUbi()




    }

    private fun activacionLocalizacion(){
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }


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

                .show()

    }

    private fun SiUbicacion(): Boolean{
        var locationManager: LocationManager
        = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }




}


