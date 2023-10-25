package com.example.proyectolzctransporta

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class RegistrarActivity : ComponentActivity() {

    private lateinit var nombre: EditText
    private lateinit var apellidos: EditText
    private lateinit var numeroTelefono: EditText
    private lateinit var etInicioCorreo: EditText
    private lateinit var etInicioContrasena: EditText
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrarme)
        etInicioCorreo = findViewById(R.id.etRegistrarCorreo)
        etInicioContrasena = findViewById(R.id.etRegistrarContraseña)
        nombre = findViewById(R.id.etRegistrarNombre)
        apellidos = findViewById(R.id.etRegistrarApellido)
        numeroTelefono = findViewById(R.id.etRegistrarTelefono)
        mAuth = FirebaseAuth.getInstance()


        manageButtonLogin()
        etInicioCorreo.doOnTextChanged{text,start,before,count -> manageButtonLogin()}
        etInicioContrasena.doOnTextChanged{text,start,before,count -> manageButtonLogin()}

    }

    fun register(view: View) {
        if (validateFields()) {
            registerUser()
        }
    }

    private fun validateFields(): Boolean {
        val email = etInicioCorreo.text.toString().trim()
        val password = etInicioContrasena.text.toString().trim()
        val nombreText = nombre.text.toString().trim()
        val apellidosText = apellidos.text.toString().trim()
        val numeroTelefonoText = numeroTelefono.text.toString().trim()
        if (email.isEmpty() || password.isEmpty() || nombreText.isEmpty() ||
            apellidosText.isEmpty() || numeroTelefonoText.isEmpty()
        ) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }
        // Puedes agregar más validaciones según tus necesidades, por ejemplo, verificar el formato del email.
        return true
    }

    private fun registerUser() {
        val email = etInicioCorreo.text.toString().trim()
        val password = etInicioContrasena.text.toString().trim()

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
                    val dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("users").document(email).set(
                        hashMapOf(
                            "nombre" to nombre.text.toString(),
                            "apellidos" to apellidos.text.toString(),
                            "numeroTelefono" to numeroTelefono.text.toString(),
                            "user" to email,
                            "dataRegister" to dateRegister
                        )
                    )
                    goLogin()
                } else {
                    Toast.makeText(this, "Algo no ha funcionado", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goLogin() {
        startActivity(Intent(this, LoginAppActivity::class.java))
    }


    private fun manageButtonLogin(){
        var btnRegistrar = findViewById<TextView>(R.id.btnRegistrar)
        var email = etInicioCorreo.text.toString()
        var password = etInicioContrasena.text.toString()


        if (TextUtils.isEmpty(password) || !ValidatorEmail.isEmail(email) || password.length < 8){

            btnRegistrar.setBackgroundColor(ContextCompat.getColor(this, R.color.botonDeshabilitado))
            btnRegistrar.isEnabled = false
        }
        else{
            btnRegistrar.setBackgroundColor(ContextCompat.getColor(this, R.color.botonHabilitado))
            btnRegistrar.isEnabled = true
        }
    }

}



