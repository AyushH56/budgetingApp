package com.example.poegroup4

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Find IDs
        etEmail = findViewById(R.id.registerEmail)
        etPassword = findViewById(R.id.registerPassword)
        etConfirmPassword = findViewById(R.id.registerConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)

        // Sign Up Button Logic
        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validation
            if (!validateEmail(email)) {
                Toast.makeText(this, "Invalid Email Address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!validatePassword(password)) {
                Toast.makeText(this, "Password must be at least 8 characters and include letters and numbers!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Registration
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Validate email format
    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validate password length and content
    private fun validatePassword(password: String): Boolean {
        return password.isNotEmpty() && password.length >= 8 &&
                password.matches(Regex(".*[a-zA-Z].*")) && password.matches(Regex(".*[0-9].*"))
    }
}