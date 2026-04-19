package com.igdtuw.studysync

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TrackerActivity : AppCompatActivity() {

    private lateinit var monBar: ProgressBar
    private lateinit var tueBar: ProgressBar
    private lateinit var wedBar: ProgressBar
    private lateinit var thuBar: ProgressBar
    private lateinit var friBar: ProgressBar
    private lateinit var satBar: ProgressBar
    private lateinit var sunBar: ProgressBar

    private lateinit var startStudyButton: Button
    private lateinit var studyTimer: TextView
    private lateinit var todayTime: TextView
    private lateinit var tasksCompleted: TextView
    private lateinit var revisionTasks: TextView
    private lateinit var tasksMissed: TextView
    private lateinit var avgStudy: TextView

    private var startTime: Long = 0
    private var isRunning = false

    private val handler = Handler(Looper.getMainLooper())
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val timerRunnable = object : Runnable {
        override fun run() {
            val time = System.currentTimeMillis() - startTime
            val seconds = (time / 1000) % 60
            val minutes = (time / 60000)
            studyTimer.text = String.format("%02d:%02d", minutes, seconds)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Progress bars
        monBar = findViewById(R.id.monBar)
        tueBar = findViewById(R.id.tueBar)
        wedBar = findViewById(R.id.wedBar)
        thuBar = findViewById(R.id.thuBar)
        friBar = findViewById(R.id.friBar)
        satBar = findViewById(R.id.satBar)
        sunBar = findViewById(R.id.sunBar)
        
        tasksCompleted = findViewById(R.id.tasksCompleted)
        revisionTasks = findViewById(R.id.revisionTasks)
        tasksMissed = findViewById(R.id.tasksMissed)
        avgStudy = findViewById(R.id.avgStudy)

        // Timer UI
        startStudyButton = findViewById(R.id.startStudyButton)
        studyTimer = findViewById(R.id.studyTimer)
        todayTime = findViewById(R.id.todayTime)

        loadWeeklyData()

        startStudyButton.setOnClickListener {
            if (!isRunning) {
                startTime = System.currentTimeMillis()
                handler.post(timerRunnable)
                startStudyButton.text = "Stop Study"
                isRunning = true
            } else {
                handler.removeCallbacks(timerRunnable)
                val durationMillis = System.currentTimeMillis() - startTime
                val minutes = (durationMillis / 60000).toInt()
                saveStudyTime(minutes)
                studyTimer.text = "00:00"
                startStudyButton.text = "Start Study"
                isRunning = false
            }
        }
    }

    private fun loadWeeklyData() {
        val userId = auth.currentUser?.uid ?: return
        
        val calendar = Calendar.getInstance()
        // Get start of week (Sunday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val startOfWeek = calendar.time

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Fetch all tasks for the week to calculate stats
        db.collection("users").document(userId).collection("tasks")
            .whereGreaterThanOrEqualTo("date", sdf.format(startOfWeek))
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                var completed = 0
                var pending = 0
                var missed = 0
                
                value?.forEach { doc ->
                    val status = doc.getString("status")?.lowercase()
                    when (status) {
                        "completed" -> completed++
                        "pending" -> pending++
                        "missed" -> missed++
                    }
                }
                
                tasksCompleted.text = completed.toString()
                revisionTasks.text = pending.toString()
                tasksMissed.text = missed.toString()
            }

        // Fetch study time for the week
        db.collection("users").document(userId).collection("study_time")
            .whereGreaterThanOrEqualTo("date", sdf.format(startOfWeek))
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                val weekTimeMap = mutableMapOf<Int, Int>() // DayOfWeek to Minutes
                var totalMinutes = 0
                var daysCount = 0

                value?.forEach { doc ->
                    val dateStr = doc.getString("date") ?: ""
                    val mins = doc.getLong("minutes")?.toInt() ?: 0
                    
                    val date = sdf.parse(dateStr)
                    val cal = Calendar.getInstance()
                    if (date != null) {
                        cal.time = date
                        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                        weekTimeMap[dayOfWeek] = (weekTimeMap[dayOfWeek] ?: 0) + mins
                        totalMinutes += mins
                    }
                }

                updateBars(weekTimeMap)
                
                val todayCal = Calendar.getInstance()
                val todayMins = weekTimeMap[todayCal.get(Calendar.DAY_OF_WEEK)] ?: 0
                todayTime.text = "Time Today: $todayMins mins"

                val avgHrs = (totalMinutes / 60.0) / 7.0
                avgStudy.text = String.format("%.1f hrs", avgHrs)
            }
    }

    private fun updateBars(weekTimeMap: Map<Int, Int>) {
        monBar.progress = weekTimeMap[Calendar.MONDAY] ?: 0
        tueBar.progress = weekTimeMap[Calendar.TUESDAY] ?: 0
        wedBar.progress = weekTimeMap[Calendar.WEDNESDAY] ?: 0
        thuBar.progress = weekTimeMap[Calendar.THURSDAY] ?: 0
        friBar.progress = weekTimeMap[Calendar.FRIDAY] ?: 0
        satBar.progress = weekTimeMap[Calendar.SATURDAY] ?: 0
        sunBar.progress = weekTimeMap[Calendar.SUNDAY] ?: 0
    }

    private fun saveStudyTime(minutes: Int) {
        val userId = auth.currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val docRef = db.collection("users").document(userId).collection("study_time").document(today)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentMins = if (snapshot.exists()) snapshot.getLong("minutes") ?: 0L else 0L
            transaction.set(docRef, hashMapOf(
                "date" to today,
                "minutes" to currentMins + minutes
            ))
        }.addOnSuccessListener {
            Toast.makeText(this, "Study time saved!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
