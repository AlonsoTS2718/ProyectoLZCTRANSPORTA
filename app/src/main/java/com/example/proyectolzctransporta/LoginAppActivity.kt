package com.example.proyectolzctransporta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class LoginAppActivity : ComponentActivity() {
    // Variables de clase que almacenarán el correo y el proveedor de inicio de sesión
    companion object {
        lateinit var useremail: String
        lateinit var providerSession: String
    }

    // Código de resultado para el inicio de sesión con Google
    private var RESULT_CODE_GOOGLE_SIGN_IN = 100

    // Variables para correo y contraseña
    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()

    // Elementos de la interfaz de usuario
    private lateinit var etInicioCorreo: EditText
    private lateinit var etInicioContrasena: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var textView: TextView
    private lateinit var client: GoogleSignInClient

    // Objeto para gestionar el inicio de sesión con Facebook
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Inicialización de elementos de la interfaz
        etInicioCorreo = findViewById(R.id.etInicioCorreo)
        etInicioContrasena = findViewById(R.id.etInicioContrasena)
        mAuth = FirebaseAuth.getInstance()

        // Agregar manejadores de clic a los botones
        val btnInicioSesion = findViewById<TextView>(R.id.btnInicioSesion)
        btnInicioSesion.setOnClickListener {
            login() // Llama al método login cuando se hace clic en el botón.
        }

        val txtInicioRegistrarme = findViewById<TextView>(R.id.txtInicioRegistrarme)
        txtInicioRegistrarme.setOnClickListener {
            registrar() // Llama al método registrar cuando se hace clic en el botón.
        }
    }

    // Método para iniciar sesión con Google
    fun callSignInGoogle(view: View) {
        signInGoogle()
    }

    private fun signInGoogle() {
        // Configurar las opciones de inicio de sesión con Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Crear un cliente de inicio de sesión de Google
        var googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        // Iniciar la actividad de inicio de sesión de Google
        startActivityForResult(googleSignInClient.signInIntent, RESULT_CODE_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Verificar si la solicitud se originó desde la actividad de inicio de sesión de Google
        if (requestCode == RESULT_CODE_GOOGLE_SIGN_IN) {
            try {
                // Obtener la cuenta de Google desde los datos de la actividad
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)!!

                if (account != null) {
                    email = account.email!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    // Autenticar con Firebase utilizando las credenciales de Google
                    mAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) goMain(email, "Google")
                        else Toast.makeText(this, "Error en la conexión con Google", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error en la conexión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para iniciar sesión con Facebook
    fun callSignInFacebook(view: View) {
        signInFacebook()
    }

    private fun signInFacebook() {
        // Solicitar permisos de correo electrónico para el inicio de sesión de Facebook
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

        // Registrar un callback para el inicio de sesión de Facebook
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                result.let {
                    val token = it.accessToken
                    val credential = FacebookAuthProvider.getCredential(token.token)

                    // Autenticar con Firebase utilizando las credenciales de Facebook
                    mAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            email = it.result.user?.email.toString()
                            goMain(email, "Facebook")
                        } else showError("Facebook")
                    }
                }
            }

            override fun onCancel() {
                // El usuario canceló el inicio de sesión
            }

            override fun onError(error: FacebookException) {
                showError("Facebook")
            }
        })
    }

    private fun showError(provider: String) {
        // Mostrar un mensaje de error en un Toast
        Toast.makeText(this, "Error en la conexión con $provider", Toast.LENGTH_SHORT).show()
    }

    fun login() {
        Log.d("LoginAppActivity", "Iniciando sesión")
        loginUser()
    }

    private fun loginUser() {
        // Obtener el correo y contraseña ingresados por el usuario
        email = etInicioCorreo.text.toString()
        password = etInicioContrasena.text.toString()

        try {
            // Iniciar sesión con Firebase utilizando correo y contraseña
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        goMain(email, "email")
                    } else {
                        // Mostrar un mensaje de error si la autenticación falla
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
        // Almacenar el correo y el proveedor de inicio de sesión
        useremail = email
        providerSession = provider

        // Iniciar la actividad principal de la aplicación
        startActivity(Intent(this, MapActivity::class.java))
    }

    fun registrar() {
        // Iniciar la actividad de registro
        startActivity(Intent(this, RegistrarActivity::class.java))
    }
}
