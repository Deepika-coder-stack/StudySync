package com.igdtuw.studysync

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var course: EditText
    private lateinit var subject: EditText
    private lateinit var email: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // View Binding
        name = findViewById(R.id.editName)
        course = findViewById(R.id.editCourse)
        subject = findViewById(R.id.editSubject)
        email = findViewById(R.id.editEmail)
        saveButton = findViewById(R.id.saveButton)

        // SharedPreferences
        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)

        // Load saved data
        name.setText(sharedPref.getString("name", ""))
        course.setText(sharedPref.getString("course", ""))
        subject.setText(sharedPref.getString("subject", ""))
        email.setText(sharedPref.getString("email", ""))

        saveButton.setOnClickListener {

            val nameText = name.text.toString().trim()
            val courseText = course.text.toString().trim()
            val subjectText = subject.text.toString().trim()
            val emailText = email.text.toString().trim()

            // Validation
            if (nameText.isEmpty() || courseText.isEmpty() || subjectText.isEmpty() || emailText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save data in SharedPreferences
            val editor = sharedPref.edit()
            editor.putString("name", nameText)
            editor.putString("course", courseText)
            editor.putString("subject", subjectText)
            editor.putString("email", emailText)
            editor.apply()

            // Send data back to ProfileActivity
            val resultIntent = Intent()
            resultIntent.putExtra("name", nameText)
            resultIntent.putExtra("course", courseText)
            resultIntent.putExtra("subject", subjectText)
            resultIntent.putExtra("email", emailText)

            setResult(Activity.RESULT_OK, resultIntent)

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}