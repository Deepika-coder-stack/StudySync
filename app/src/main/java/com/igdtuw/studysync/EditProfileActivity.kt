package com.igdtuw.studysync

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var course: EditText
    private lateinit var email: EditText
    private lateinit var saveButton: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // View Binding
        name = findViewById(R.id.editName)
        course = findViewById(R.id.editCourse)
        email = findViewById(R.id.editEmail)
        saveButton = findViewById(R.id.saveButton)

        loadCurrentData()

        saveButton.setOnClickListener {
            val courseText = course.text.toString().trim()

            if (courseText.isEmpty()) {
                Toast.makeText(this, "Please enter course", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveDataToFirestore(courseText)
        }
    }

    private fun loadCurrentData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email ?: ""
            email.setText(userEmail)
            
            val nameFromEmail = if (userEmail.contains("@")) {
                userEmail.substringBefore("@")
            } else {
                "User"
            }
            name.setText(nameFromEmail)

            val userId = currentUser.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentCourse = document.getString("course") ?: ""
                        course.setText(currentCourse)
                    }
                }
        }
    }

    private fun saveDataToFirestore(newCourse: String) {
        val userId = auth.currentUser?.uid ?: return
        
        val userMap = hashMapOf<String, Any>(
            "course" to newCourse
        )

        db.collection("users").document(userId)
            .update(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
