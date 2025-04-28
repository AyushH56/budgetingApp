package com.example.poegroup4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etForgotEmail: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etForgotEmail = findViewById(R.id.forgotEmail)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val emailInput = etForgotEmail.text.toString().trim()

            if (emailInput.isEmpty()) {
                Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPref = getSharedPreferences("UserData", MODE_PRIVATE)
            val savedEmail = sharedPref.getString("email", "")

            if (emailInput == savedEmail) {
                val newPassword = generateRandomPassword()

                // Save new password
                with(sharedPref.edit()) {
                    putString("password", newPassword)
                    apply()
                }

                // Debugging log
                Log.d("ForgotPasswordActivity", "Password reset successful. New password: $newPassword")

                // TODO: Later send an email with the new password (for now, it's displayed in Toast)
                Toast.makeText(this, "Password reset successful!\nNew Password: $newPassword", Toast.LENGTH_LONG).show()

                Log.d("ForgotPasswordActivity", "Redirecting to ChangePasswordActivity.")

                // Redirect to ChangePasswordActivity
                val intent = Intent(this, ChangePasswordActivity::class.java)
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "Email not found! Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto-Generate Password
    private fun generateRandomPassword(length: Int = 8): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
