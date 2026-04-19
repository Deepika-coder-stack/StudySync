package com.igdtuw.studysync

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igdtuw.studysync.R.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainScreen : AppCompatActivity() {
    private val subjectList = mutableListOf<Subject>()
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: SubjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_main_screen)

        val dateText = findViewById<TextView>(id.dateText)
        val addSubjectBtn = findViewById<Button>(id.addSubjectBtn)
        addSubjectBtn.setOnClickListener { showAddSubjectDialog() }

        val calendar = Calendar.getInstance()
        val day = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)

        dateText.text = "$day, $date"

        recycler = findViewById(id.subjectRecycler)

        adapter = SubjectAdapter(
            subjectList,
            // Add
            { position ->
                if (position != RecyclerView.NO_POSITION) {
                    showAddTopicDialog(subjectList[position])
                }
            },
            // Edit
            { position, topic ->
                if (position != RecyclerView.NO_POSITION) {
                    showEditTopicDialog(subjectList[position], topic)
                }
            },
            // Delete
            { position, topic ->
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteDialog(subjectList[position], topic)
                }
            }
        )

        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = adapter

        loadSubjects()

        findViewById<TextView>(id.profileIcon).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<TextView>(id.trackerIcon).setOnClickListener {
            startActivity(Intent(this, TrackerActivity::class.java))
        }
        findViewById<TextView>(id.taskIcon).setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        
        scheduleDailyReminder(16, 0, "Reminder", "Complete today's tasks")
        scheduleDailyReminder(20, 0, "Progress Check", "Check today's progress")
        scheduleSundayReminder(10, 0)

        findViewById<TextView>(id.signOutBtn).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(string.sign_out))
                .setMessage(getString(string.sign_out_message))
                .setPositiveButton(getString(string.yes)) { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    finish()
                }
                .setNegativeButton(getString(string.no), null)
                .show()
        }

        val menuBtn = findViewById<TextView>(id.menuIcon)
        val dashboard = findViewById<LinearLayout>(id.dashboardLayout)

        menuBtn.setOnClickListener {
            if (dashboard.isGone) {
                dashboard.visibility = View.VISIBLE
                dashboard.alpha = 0f
                dashboard.animate().alpha(1f).setDuration(300)
            } else {
                dashboard.animate().alpha(0f).setDuration(300)
                    .withEndAction { dashboard.visibility = View.GONE }
            }
        }

        val mainLayout = findViewById<FrameLayout>(id.main_layout_root)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadSubjects() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser
        
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("users")
                .document(userId)
                .collection("subjects")
                .get()
                .addOnSuccessListener { result ->
                    subjectList.clear()
                    if (result.isEmpty) {
                        adapter.notifyDataSetChanged()
                        return@addOnSuccessListener
                    }

                    var loadedCount = 0
                    for (document in result) {
                        val name = document.getString("name") ?: ""
                        val id = document.id
                        val subject = Subject(name = name, id = id)
                        subjectList.add(subject)

                        db.collection("users")
                            .document(userId)
                            .collection("tasks")
                            .whereEqualTo("subjectId", id)
                            .get()
                            .addOnSuccessListener { taskResult ->
                                val topics = mutableListOf<Topic>()
                                for (taskDoc in taskResult) {
                                    val task = taskDoc.toObject(Task::class.java)
                                    val topic = Topic(
                                        id = taskDoc.id,
                                        name = task.name,
                                        subject = task.subject,
                                        time = task.time,
                                        status = task.status
                                    )
                                    topics.add(topic)
                                }
                                subject.topics = topics
                                loadedCount++
                                if (loadedCount == result.size()) {
                                    adapter.notifyDataSetChanged()
                                }
                            }
                    }
                }
        }
    }

    private fun showAddSubjectDialog() {
        val editText = EditText(this)
        editText.hint = "Enter subject"

        AlertDialog.Builder(this)
            .setTitle("Add Subject")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val subjectName = editText.text.toString().trim()
                if (subjectName.isEmpty()) {
                    Toast.makeText(this, "Enter subject", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser ?: return@setPositiveButton
                val userId = currentUser.uid
                val db = FirebaseFirestore.getInstance()

                val subjectMap = hashMapOf("name" to subjectName)

                db.collection("users")
                    .document(userId)
                    .collection("subjects")
                    .add(subjectMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Subject Added", Toast.LENGTH_SHORT).show()
                        loadSubjects() 
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddTopicDialog(subject: Subject) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(string.add_topic))

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val topicInput = EditText(this)
        topicInput.hint = getString(string.topic_name_hint)
        layout.addView(topicInput)

        val timeBtn = Button(this)
        timeBtn.text = getString(string.select_time)
        layout.addView(timeBtn)

        var selectedTime = ""
        var selectedHour = -1
        var selectedMinute = -1

        timeBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                selectedHour = h
                selectedMinute = m
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                timeBtn.text = getString(string.selected_time, selectedTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        builder.setView(layout)

        builder.setPositiveButton(getString(string.save)) { _, _ ->
            val name = topicInput.text.toString().trim()
            if (name.isNotEmpty() && selectedHour != -1) {
                val auth = FirebaseAuth.getInstance()
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                val db = FirebaseFirestore.getInstance()

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())

                val taskMap = hashMapOf(
                    "name" to name,
                    "time" to selectedTime,
                    "subject" to subject.name,
                    "subjectId" to subject.id,
                    "status" to "pending",
                    "date" to today,
                    "revise" to false
                )

                db.collection("users")
                    .document(userId)
                    .collection("tasks")
                    .add(taskMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Topic Added Permanently", Toast.LENGTH_SHORT).show()
                        loadSubjects()
                    }

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                scheduleNotification(calendar.timeInMillis, "Start Study", "Start: $name")
            } else {
                Toast.makeText(this, "Please enter name and select time", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(getString(string.cancel), null)
        builder.show()
    }

    private fun showEditTopicDialog(subject: Subject, topic: Topic) {
        val dialogView = layoutInflater.inflate(layout.dialog_add_topic, null)

        val topicInput = dialogView.findViewById<EditText>(id.topicInput)
        val timeBtn = dialogView.findViewById<TextView>(id.timeBtn)

        topicInput.setText(topic.name)
        timeBtn.text = topic.time

        var selectedTime = topic.time

        timeBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                timeBtn.text = selectedTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Topic")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newName = topicInput.text.toString().trim()
                val auth = FirebaseAuth.getInstance()
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                val db = FirebaseFirestore.getInstance()

                db.collection("users")
                    .document(userId)
                    .collection("tasks")
                    .document(topic.id)
                    .update("name", newName, "time", selectedTime)
                    .addOnSuccessListener {
                        loadSubjects()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(subject: Subject, topic: Topic) {
        AlertDialog.Builder(this)
            .setTitle("Delete Topic")
            .setMessage("Are you sure you want to delete?")
            .setPositiveButton("Yes") { _, _ ->
                val auth = FirebaseAuth.getInstance()
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                val db = FirebaseFirestore.getInstance()

                db.collection("users")
                    .document(userId)
                    .collection("tasks")
                    .document(topic.id)
                    .delete()
                    .addOnSuccessListener {
                        loadSubjects()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun scheduleNotification(timeInMillis: Long, title: String, message: String) {
        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("message", message)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

    private fun scheduleDailyReminder(hour: Int, minute: Int, title: String, message: String) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("message", message)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            hour * 100 + minute,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    private fun scheduleSundayReminder(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra("title", "Revision Time")
        intent.putExtra("message", "Revise your topics")

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
    }
}
