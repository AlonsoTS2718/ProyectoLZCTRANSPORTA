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
//sfsfsfs
//hohoho

class LoginAppActivity : ComponentActivity() {
    companion object{
        lateinit var useremail : String
        lateinit var providerSession : String

//12102023
    }

    private var RESULT_CODE_GOOGLE_SIGN_IN = 100
    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etInicioCorreo: EditText
    private lateinit var etInicioContrasena: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var textView: TextView
    private lateinit var client: GoogleSignInClient




    /*VARIABLES PARA INICIO SESIÓN FACEBOOK*/
    private val callbackManager = CallbackManager.Factory.create()





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

    /*CODIG PARA INICIO SESIÓN GOOGLE*/

    fun callSignInGoogle (view:View){
        signInGoogle()
    }
    private fun signInGoogle(){
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        var googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        startActivityForResult(googleSignInClient.signInIntent, RESULT_CODE_GOOGLE_SIGN_IN)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RESULT_CODE_GOOGLE_SIGN_IN) {

            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!

                if (account != null){
                    email = account.email!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    mAuth.signInWithCredential(credential).addOnCompleteListener{
                        if (it.isSuccessful) goMain(email, "Google")
                        else Toast.makeText(this, "Error en la conexión con Google", Toast.LENGTH_SHORT)

                    }
                }


            } catch (e: ApiException) {
                Toast.makeText(this, "Error en la conexión con Google", Toast.LENGTH_SHORT)
            }
        }

    }

    /*---------------------------*/

    /*INICIO DE SESION FACEBOOK*/
    fun callSignInFacebook (view:View){
        signInFacebook()
    }
    private fun signInFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                result.let{
                    val token = it.accessToken
                    val credential = FacebookAuthProvider.getCredential(token.token)
                    mAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful){
                            email = it.result.user?.email.toString()
                            goMain(email, "Facebook")
                        }
                        else showError("Facebook")
                    }
                }
                //handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() { }
            override fun onError(error: FacebookException) { showError("Facebook") }
        })

    }
    private fun showError (provider: String){
        Toast.makeText(this, "Error en la conexión con $provider", Toast.LENGTH_SHORT)
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


    public override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            goMain(currentUser.email.toString(), currentUser.providerId)
        }
    }

    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

}


