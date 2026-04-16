package com.igdtuw.studysync

import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

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

    private val handler = Handler()

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

        loadStudyData()

        startStudyButton.setOnClickListener {

            if (!isRunning) {

                // START TIMER
                startTime = System.currentTimeMillis()
                handler.post(timerRunnable)

                startStudyButton.text = "Stop Study"
                isRunning = true

            } else {

                // STOP TIMER
                handler.removeCallbacks(timerRunnable)

                val minutes = ((System.currentTimeMillis() - startTime) / 60000).toInt()

                saveStudyTime(minutes)

                loadStudyData()

                studyTimer.text = "00:00"
                startStudyButton.text = "Start Study"

                isRunning = false
            }
        }
    }

    private fun loadStudyData() {

        val pref = getSharedPreferences("StudyTime", MODE_PRIVATE)

        val mon = pref.getInt("2", 0)
        val tue = pref.getInt("3", 0)
        val wed = pref.getInt("4", 0)
        val thu = pref.getInt("5", 0)
        val fri = pref.getInt("6", 0)
        val sat = pref.getInt("7", 0)
        val sun = pref.getInt("1", 0)

        monBar.progress = mon
        tueBar.progress = tue
        wedBar.progress = wed
        thuBar.progress = thu
        friBar.progress = fri
        satBar.progress = sat
        sunBar.progress = sun

        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayMinutes = pref.getInt(day.toString(), 0)

        todayTime.text = "Time Today: $todayMinutes mins"
        updateWeeklyStats()
    }

    private fun saveStudyTime(minutes: Int) {

        val pref = getSharedPreferences("StudyTime", MODE_PRIVATE)
        val editor = pref.edit()

        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        val previous = pref.getInt(day.toString(), 0)

        editor.putInt(day.toString(), previous + minutes)

        editor.apply()
    }

    private fun updateWeeklyStats() {

        val pref = getSharedPreferences("StudyTime", MODE_PRIVATE)

        var totalMinutes = 0
        var daysWithStudy = 0

        for (i in 1..7) {
            val minutes = pref.getInt(i.toString(), 0)
            totalMinutes += minutes

            if (minutes > 0) daysWithStudy++
        }

        val avg = if (daysWithStudy > 0) totalMinutes / daysWithStudy else 0

        // 👇 dummy logic for now
        val completed = totalMinutes / 60   // assume 1 hr = 1 task
        val pending = 10 - completed
        val missed = if (pending > 0) pending / 2 else 0

        tasksCompleted.text = "$completed"
        revisionTasks.text = "$pending"
        tasksMissed.text = "$missed"
        avgStudy.text = "${avg / 60.0}"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
