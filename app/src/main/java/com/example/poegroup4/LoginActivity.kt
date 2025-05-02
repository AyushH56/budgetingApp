package com.example.poegroup4

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterNow: TextView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Find IDs
        etEmail = findViewById(R.id.loginEmail)
        etPassword = findViewById(R.id.loginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterNow = findViewById(R.id.tvRegisterNow)

        // Login Button Logic
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validation
            if (!validateEmail(email)) {
                Toast.makeText(this, "Invalid Email Address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!validatePassword(password)) {
                Toast.makeText(this, "Password must be at least 8 characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Authentication
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login Failed, please try again", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Register Now Logic
        tvRegisterNow.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Validate email format
    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validate password length
    private fun validatePassword(password: String): Boolean {
        return password.isNotEmpty() && password.length >= 8
    }
}