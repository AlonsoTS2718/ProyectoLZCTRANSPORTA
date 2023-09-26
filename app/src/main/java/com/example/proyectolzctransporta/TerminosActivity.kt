package com.example.proyectolzctransporta

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity

class TerminosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terminos)
    }

    fun irTerminos(v: View){
        val intent = Intent(this, TerminosActivity::class.java)
        startActivity(intent)
    }
}




