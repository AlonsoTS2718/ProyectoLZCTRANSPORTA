package com.example.proyectolzctransporta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates
//sfsfsfs


class LoginAppActivity : ComponentActivity() {
    companion object{
        lateinit var useremail : String
        lateinit var providerSession : String
//12102023
    }
    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etInicioCorreo: EditText
    private lateinit var etInicioContrasena: EditText
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        etInicioCorreo = findViewById(R.id.etInicioCorreo);
        etInicioContrasena = findViewById(R.id.etInicioContrasena)
        mAuth = FirebaseAuth.getInstance()

        val btnInicioSesion = findViewById<TextView>(R.id.btnInicioSesion)
        btnInicioSesion.setOnClickListener {
            login() // Llama al método login cuando se haga clic en el botón.
        }
        val txtInicioRegistrarme = findViewById<TextView>(R.id.txtInicioRegistrarme)
        txtInicioRegistrarme.setOnClickListener {
            registrar() // Llama al método login cuando se haga clic en el botón.
        }



    }
    fun login() {
        Log.d("LoginAppActivity", "Iniciando sesión")
        loginUser()
    }
    private fun loginUser() {
        email = etInicioCorreo.text.toString()
        password = etInicioContrasena.text.toString()
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        goMain(email, "email")
                    } else {
                        // Manejar errores aquí y mostrar un mensaje al usuario
                        val errorMessage = task.exception?.message ?: "Error desconocido"
                        Toast.makeText(this, "Error al iniciar sesión: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("LoginAppActivity", "Error inesperado: ${e.message}")
            Toast.makeText(this, "Error inesperado: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun goMain(email: String, provider: String) {
        Log.d("LoginAppActivity", "Ingresando a la actividad principal")
        useremail = email
        providerSession = provider
        startActivity(Intent(this, MapActivity::class.java))
    }

    fun registrar(){
        startActivity(Intent(this, RegistrarActivity::class.java))
    }




}


