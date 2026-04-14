package com.igdtuw.studysync

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TaskActivity : AppCompatActivity() {

    private lateinit var taskContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var addTaskBtn: Button

    private lateinit var completedCount: TextView
    private lateinit var pendingCount: TextView
    private lateinit var missedCount: TextView

    // NEW (Bottom buttons)
    private lateinit var btnYes: Button
    private lateinit var btnNo: Button

    private val tasks = mutableListOf<Task>()

    data class Task(
        var name: String,
        var status: String = "Pending"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        // Views
        taskContainer = findViewById(R.id.taskContainer)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        addTaskBtn = findViewById(R.id.addTaskBtn)

        completedCount = findViewById(R.id.completedCount)
        pendingCount = findViewById(R.id.pendingCount)
        missedCount = findViewById(R.id.missedCount)

        // Bottom buttons
        btnYes = findViewById(R.id.btnYes)
        btnNo = findViewById(R.id.btnNo)

        // Add Task
        addTaskBtn.setOnClickListener {
            showAddTaskDialog()
        }

        // YES button
        btnYes.setOnClickListener {
            Toast.makeText(this, "Great! Keep it up 👍", Toast.LENGTH_SHORT).show()
        }

        // NO button
        btnNo.setOnClickListener {
            Toast.makeText(this, "Try to review tomorrow!", Toast.LENGTH_SHORT).show()
        }
    }

    // Dialog
    private fun showAddTaskDialog() {
        val input = EditText(this)
        input.hint = "Enter task"

        AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val taskName = input.text.toString().trim()
                if (taskName.isNotEmpty()) {
                    addTask(taskName)
                } else {
                    Toast.makeText(this, "Enter valid task", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Add Task View
    private fun addTask(name: String) {
        val view = layoutInflater.inflate(R.layout.item_task, taskContainer, false)

        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val taskName = view.findViewById<TextView>(R.id.taskName)
        val taskStatus = view.findViewById<TextView>(R.id.taskStatus)

        taskName.text = name
        taskStatus.text = "Pending"

        val task = Task(name)
        tasks.add(task)

        // Checkbox logic
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                task.status = "Completed"
                taskStatus.text = "Completed"
                taskStatus.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
                )
            } else {
                task.status = "Pending"   // this = Incomplete
                taskStatus.text = "Pending"
                taskStatus.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_orange_dark)
                )
            }
            updateStats()
        }

        // Long press → Missed
        view.setOnLongClickListener {
            task.status = "Missed"
            taskStatus.text = "Missed"
            taskStatus.setTextColor(
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
            )
            checkBox.isChecked = false
            updateStats()
            true
        }

        taskContainer.addView(view)
        updateStats()
    }

    // Update UI
    private fun updateStats() {
        val total = tasks.size
        val completed = tasks.count { it.status == "Completed" }
        val pending = tasks.count { it.status == "Pending" }
        val missed = tasks.count { it.status == "Missed" }

        completedCount.text = "$completed Completed"
        pendingCount.text = "$pending Pending"
        missedCount.text = "$missed Missed"

        val percent = if (total == 0) 0 else (completed * 100) / total

        progressBar.progress = percent
        progressText.text = "$percent%"
    }
}