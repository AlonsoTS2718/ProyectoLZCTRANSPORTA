package com.example.proyectolzctransporta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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



class LoginAppActivity : ComponentActivity() {
    companion object{
        lateinit var useremail : String
        lateinit var providerSession : String

    }
    private var RESULT_CODE_GOOGLE_SIGN_IN = 100
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

    }
    fun login(view: View){
        loginUser()
    }
    private fun loginUser() {
        email = etInicioCorreo.text.toString();
        password = etInicioContrasena.text.toString()
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goMain(email, "email")
                }else {
                    goRegister()
                }
            }
        /*
        fun registrar(v: View) {
            val intent = Intent(this, RegistrarActivity::class.java)
            startActivity(intent)
        }
        fun olvideContrasena(v: View) {
            startActivity(Intent(this, OlvideContrasenaActivity::class.java))
        }
        */

    }

/*

   // Inicio de sesión con Google
    fun callsignInGoogle(view: View){
        signInGoogle()
    }
    private fun signInGoogle(){
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        var googleSignInClient = GoogleSignIn.getClient(this, gso)
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
*/

    private fun goMain(email: String, provider: String) {
        useremail = email
        providerSession = provider
        startActivity(Intent(this, MainActivity::class.java))
    }
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

    fun registrar(view: View){
        startActivity(Intent(this, RegistrarActivity::class.java))
    }
}












