package com.igdtuw.studysync

import android.app.AlertDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Task(
    var id: String = "",
    var name: String = "",
    var status: String = "pending",
    var date: String = "",
    var subject: String = "",
    var subjectId: String = "",
    var revise: Boolean = false,
    var time: String = ""
)

class TaskActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var addTaskBtn: Button

    private lateinit var completedCount: TextView
    private lateinit var pendingCount: TextView
    private lateinit var missedCount: TextView

    private lateinit var btnYes: Button
    private lateinit var btnNo: Button

    private val list = mutableListOf<Task>()
    private lateinit var adapter: TodayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        
        val recyclerView = findViewById<RecyclerView>(R.id.todayRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TodayAdapter(list)
        recyclerView.adapter = adapter

        // Views
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        addTaskBtn = findViewById(R.id.addTaskBtn)
        
        // Hide Add Task button as requested
        addTaskBtn.visibility = View.GONE

        completedCount = findViewById(R.id.completedCount)
        pendingCount = findViewById(R.id.pendingCount)
        missedCount = findViewById(R.id.missedCount)

        btnYes = findViewById(R.id.btnYes)
        btnNo = findViewById(R.id.btnNo)

        loadTasks()

        // YES button (Sunday Revision Logic)
        btnYes.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val db = FirebaseFirestore.getInstance()

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val nextSunday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            db.collection("users").document(userId).collection("tasks")
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener {
                    for (doc in it) {
                        val task = doc.toObject(Task::class.java)
                        val newTask = hashMapOf(
                            "name" to task.name,
                            "subject" to task.subject,
                            "subjectId" to task.subjectId,
                            "date" to nextSunday,
                            "status" to "pending",
                            "revise" to true,
                            "time" to task.time
                        )
                        db.collection("users").document(userId).collection("tasks").add(newTask)
                    }
                    Toast.makeText(this, "Revision tasks scheduled for next Sunday!", Toast.LENGTH_SHORT).show()
                }
        }

        btnNo.setOnClickListener {
            Toast.makeText(this, "Keep going!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTasks() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                list.clear()
                for (doc in value!!) {
                    val task = doc.toObject(Task::class.java)
                    task.id = doc.id
                    list.add(task)
                }
                adapter.notifyDataSetChanged()
                updateStatsFromList(list)
            }
    }

    private fun updateStatsFromList(list: List<Task>) {
        val total = list.size
        val completed = list.count { it.status.lowercase() == "completed" }
        val pending = list.count { it.status.lowercase() == "pending" }
        val missed = list.count { it.status.lowercase() == "missed" }

        completedCount.text = "$completed Completed"
        pendingCount.text = "$pending Pending"
        missedCount.text = "$missed Missed"

        val percent = if (total == 0) 0 else (completed * 100) / total
        progressBar.progress = percent
        progressText.text = "$percent%"
    }
}
