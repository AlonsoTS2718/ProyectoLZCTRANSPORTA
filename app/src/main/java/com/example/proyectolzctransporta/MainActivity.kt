package com.example.proyectolzctransporta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest

class MainActivity : ComponentActivity() {
    private lateinit var etInicioCorreo: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }



}


