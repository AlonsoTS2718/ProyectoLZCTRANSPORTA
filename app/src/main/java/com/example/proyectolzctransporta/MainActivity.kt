package com.example.proyectolzctransporta

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }




    fun registrar(v: View){
        val intent = Intent(this, RegistrarActivity::class.java)
        startActivity(intent)
    }

    fun olvideContraseña(v: View){
        startActivity(Intent(this,OlvideContrasenaActivity::class.java))
    }



}


