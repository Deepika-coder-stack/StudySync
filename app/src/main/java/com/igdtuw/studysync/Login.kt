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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    val auth = FirebaseAuth.getInstance()
    private lateinit var password: EditText
    private lateinit var loginBtn: Button

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        val auth = FirebaseAuth.getInstance()
        val db= FirebaseFirestore.getInstance()


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

        // Firebase Authentication
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.signInWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val userId = auth.currentUser!!.uid

                    // 🔥 CHECK USER DATA IN FIRESTORE
                    db.collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { document ->

                            if (document.exists() && document.getString("course") != null) {
                                // ✅ DATA EXISTS → MAIN SCREEN
                                startActivity(Intent(this, MainScreen::class.java))
                                finish()
                            } else {
                                // ❌ DATA NOT EXISTS → BASIC INFO
                                startActivity(Intent(this, BasicInfoActivity::class.java))
                                finish()
                            }

                        }

                } else {
                    // ❌ LOGIN FAIL → SIGNUP
                    auth.createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener { signupTask ->
                            if (signupTask.isSuccessful) {
                                startActivity(Intent(this, BasicInfoActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
    }
}
