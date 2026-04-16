package com.igdtuw.studysync

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BasicInfoActivity : AppCompatActivity() {

    private lateinit var subjectsContainer: LinearLayout

    private lateinit var btnAdd: Button
    private lateinit var btnContinue: Button
    private lateinit var etCourseName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_basic_info)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        subjectsContainer = findViewById(R.id.subjectsContainer)
        btnAdd = findViewById(R.id.btnAdd)
        btnContinue = findViewById(R.id.btnContinue)
        etCourseName = findViewById(R.id.etCourseName)

        // Add Subject
        btnAdd.setOnClickListener {
            showInputDialog()
        }

        // Continue Button
        btnContinue.setOnClickListener {
            val course = etCourseName.text.toString().trim()
            val subjects = getSubjects()

            if (course.isEmpty()) {
                etCourseName.error = "Enter course name"
                etCourseName.requestFocus()
                return@setOnClickListener
            }

            if (subjects.isEmpty()) {
                Toast.makeText(this, "Add at least one subject", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val userMap = hashMapOf("course" to course)

                db.collection("users")
                    .document(userId)
                    .set(userMap)
                    .addOnSuccessListener {
                        // Add each subject to the sub-collection
                        subjects.forEach { subjectName ->
                            val subjectMap = hashMapOf("name" to subjectName)
                            db.collection("users")
                                .document(userId)
                                .collection("subjects")
                                .add(subjectMap)
                        }
                        
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainScreen::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showInputDialog() {
        val editText = EditText(this)
        editText.hint = "Enter subject"

        AlertDialog.Builder(this)
            .setTitle("Add Subject")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val text = editText.text.toString().trim()
                if (text.isNotEmpty()) {
                    addSubject(text)
                } else {
                    Toast.makeText(this, "Enter valid subject", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addSubject(name: String) {
        val view = layoutInflater.inflate(R.layout.activity_item_subject, subjectsContainer, false)

        val tvName = view.findViewById<TextView>(R.id.tvSubjectName)
        val deleteBtn = view.findViewById<ImageView>(R.id.btnDelete)

        tvName.text = name

        deleteBtn.setOnClickListener {
            subjectsContainer.removeView(view)
        }

        subjectsContainer.addView(view)
    }

    private fun getSubjects(): List<String> {
        val list = mutableListOf<String>()

        for (i in 0 until subjectsContainer.childCount) {
            val view = subjectsContainer.getChildAt(i)
            val tv = view.findViewById<TextView>(R.id.tvSubjectName)
            list.add(tv.text.toString())
        }

        return list
    }
}
