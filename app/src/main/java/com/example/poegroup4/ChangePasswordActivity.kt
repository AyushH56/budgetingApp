package com.example.poegroup4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etTempPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        etEmail = findViewById(R.id.etEmail)
        etTempPassword = findViewById(R.id.etTempPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        btnChangePassword.setOnClickListener {
            val emailInput = etEmail.text.toString().trim()
            val tempPasswordInput = etTempPassword.text.toString().trim()
            val newPasswordInput = etNewPassword.text.toString().trim()
            val confirmPasswordInput = etConfirmPassword.text.toString().trim()

            val sharedPref = getSharedPreferences("UserData", MODE_PRIVATE)
            val savedEmail = sharedPref.getString("email", "")
            val savedTempPassword = sharedPref.getString("password", "")

            if (emailInput.isEmpty() || tempPasswordInput.isEmpty() || newPasswordInput.isEmpty() || confirmPasswordInput.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (emailInput != savedEmail) {
                Toast.makeText(this, "Email does not match our records!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tempPasswordInput != savedTempPassword) {
                Toast.makeText(this, "Temporary password is incorrect!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPasswordInput != confirmPasswordInput) {
                Toast.makeText(this, "New passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the new password
            with(sharedPref.edit()) {
                putString("password", newPasswordInput)
                apply()
            }

            Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()

            // Redirect back to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
