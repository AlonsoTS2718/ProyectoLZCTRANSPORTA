package com.example.proyectolzctransporta

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.startActivity
import com.example.proyectolzctransporta.ui.theme.ProyectoLZCTRANSPORTATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }




    fun registrarme(v: View){
        val intent = Intent(this, RegistrarmeActivity::class.java)
        startActivity(intent)
    }

    fun olvideContraseña(v: View){
        startActivity(Intent(this,OlvideContraseñaActivity::class.java))
    }



}


