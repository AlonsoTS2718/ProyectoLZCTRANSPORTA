package com.example.proyectolzctransporta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth

class LoginAppActivity : ComponentActivity() {

    // Variables de clase que almacenan el correo y el proveedor de inicio de sesión
    companion object {
        var useremail: String? = null
        var providerSession: String? = null
    }

    // Elementos de la interfaz de usuario
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var mAuth: FirebaseAuth
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Inicialización de elementos de la interfaz
        etCorreo = findViewById(R.id.etInicioCorreo)
        etContrasena = findViewById(R.id.etInicioContrasena)
        mAuth = FirebaseAuth.getInstance()

        // Agregar manejadores de clic a los botones
        val btnIniciarSesion = findViewById<TextView>(R.id.btnInicioSesion)
        btnIniciarSesion.setOnClickListener {
            iniciarSesion()
        }

        val txtRegistrarme = findViewById<TextView>(R.id.txtInicioRegistrarme)
        txtRegistrarme.setOnClickListener {
            abrirRegistro()
        }
    }

    // Método que se llama cuando se hace clic en el botón "Iniciar Sesión"
    fun iniciarSesion() {
        Log.d("LoginAppActivity", "Iniciando sesión")
        autenticarUsuario()
    }

    // Método para autenticar al usuario con correo y contraseña
    private fun autenticarUsuario() {
        val correo = etCorreo.text.toString()
        val contrasena = etContrasena.text.toString()

        try {
            mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        irAActividadPrincipal(correo, "email")
                    } else {
                        val mensajeError = task.exception?.message ?: "Error desconocido"
                        mostrarError("Error al iniciar sesión: $mensajeError")
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("LoginAppActivity", "Error inesperado: ${e.message}")
            mostrarError("Error inesperado: ${e.message}")
        }
    }

    // Método que se llama cuando se hace clic en el botón "Iniciar Sesión con Facebook"
    fun callSignInFacebook(view: View) {
        autenticarConFacebook()
    }

    // Método para autenticar al usuario con Facebook
    private fun autenticarConFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                result.let {
                    val token = it.accessToken
                    val credencial = FacebookAuthProvider.getCredential(token.token)

                    mAuth.signInWithCredential(credencial).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val correo = it.result.user?.email.toString()
                            irAActividadPrincipal(correo, "Facebook")
                        } else {
                            mostrarError("Facebook")
                        }
                    }
                }
            }

            override fun onCancel() {
                // El usuario canceló el inicio de sesión
            }

            override fun onError(error: FacebookException) {
                mostrarError("Facebook")
            }
        })
    }

    // Método para mostrar mensajes de error en un Toast
    private fun mostrarError(proveedor: String) {
        Toast.makeText(this, "Error en la conexión con $proveedor", Toast.LENGTH_SHORT).show()
    }


    // Método para ir a la actividad principal
    private fun irAActividadPrincipal(correo: String, proveedor: String) {
        Log.d("LoginAppActivity", "Ingresando a la actividad principal")
        useremail = correo
        providerSession = proveedor
        startActivity(Intent(this, MapActivity::class.java))
    }


    // Método para abrir la actividad de registro
    fun abrirRegistro() {
        startActivity(Intent(this, RegistrarActivity::class.java))
    }
}
