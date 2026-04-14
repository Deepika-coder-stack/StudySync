package com.igdtuw.studysync

import android.app.AlertDialog
import android.content.Intent   // ✅ ADD THIS
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BasicInfoActivity : AppCompatActivity() {

    private lateinit var subjectsContainer: LinearLayout
    private lateinit var btnAdd: Button
    private lateinit var btnContinue: Button
    private lateinit var etCourseName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_basic_info)

        subjectsContainer = findViewById(R.id.subjectsContainer)
        btnAdd = findViewById(R.id.btnAdd)
        btnContinue = findViewById(R.id.btnContinue)
        etCourseName = findViewById(R.id.etCourseName)

        // Add Subject
        btnAdd.setOnClickListener {
            showInputDialog()
        }

        // ✅ Continue Button (UPDATED)
        btnContinue.setOnClickListener {

            val course = etCourseName.text.toString()
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

            // Optional: show data
            Toast.makeText(
                this,
                "Course: $course\nSubjects: $subjects",
                Toast.LENGTH_SHORT
            ).show()

            // 🚀 MOVE TO TASK SCREEN
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
            finish()
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