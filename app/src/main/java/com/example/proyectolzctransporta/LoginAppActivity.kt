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
                }
            }

    }

    private fun goMain(email: String, provider: String) {
        useremail = email
        providerSession = provider
        startActivity(Intent(this, MapActivity::class.java))
    }

    fun registrar(view: View){
        startActivity(Intent(this, RegistrarActivity::class.java))
    }




}


