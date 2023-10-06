package com.example.proyectolzctransporta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class RegistrarActivity : ComponentActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrarme)

    }

    private var name by Delegates.notNull<String>()

    private var number by Delegates.notNull<String>()
    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etRegistrarNombre: EditText
    private lateinit var etRegistrarApellido: EditText
    private lateinit var etRegistrarTelefono: EditText
    private lateinit var etInicioCorreo: EditText
    private lateinit var etInicioContrasena: EditText
    private lateinit var mAuth: FirebaseAuth
    private fun goRegister() {
        email = etInicioCorreo.text.toString();
        password = etInicioContrasena.text.toString()


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(){
                if(it.isSuccessful){
                    var dateRegister = SimpleDateFormat("dd/mm/yyyy").format(Date())
                    var dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("users").document(email).set(hashMapOf(
                        "user" to email,
                        "dataRegister" to dateRegister
                    ))
                }else
                    Toast.makeText(this, "Algo no ha funcionado", Toast.LENGTH_SHORT).show()
            }
    }



    }







