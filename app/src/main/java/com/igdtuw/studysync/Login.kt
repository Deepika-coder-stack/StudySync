package com.igdtuw.studysync

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: Button

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        // Initialize Views
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginBtn)

        // 👇 Add visibility feature
        setupPasswordToggle()

        // Button Click
        loginBtn.setOnClickListener {
            validateLogin()
        }
    }

    private fun setupPasswordToggle() {

        // 🔁 Change icon based on text
        password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    password.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_eye, 0
                    )
                } else {
                    password.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_eye_off, 0
                    )
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 👆 Click on icon to toggle
        password.setOnTouchListener { _, event ->
            val DRAWABLE_END = 2

            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = password.compoundDrawables[DRAWABLE_END]

                if (drawable != null &&
                    event.rawX >= (password.right - drawable.bounds.width())) {

                    isPasswordVisible = !isPasswordVisible

                    if (isPasswordVisible) {
                        password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        password.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                    password.setSelection(password.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun validateLogin() {

        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString().trim()

        // Email validation
        if (emailText.isEmpty()) {
            email.error = "Email required"
            email.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.error = "Invalid email"
            email.requestFocus()
            return
        }

        // Password validation
        if (passwordText.isEmpty()) {
            password.error = "Password required"
            password.requestFocus()
            return
        }

        if (passwordText.length < 6) {
            password.error = "Minimum 6 characters"
            password.requestFocus()
            return
        }

        // Success
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, BasicInfoActivity::class.java)
        startActivity(intent)
        finish()
    }
}