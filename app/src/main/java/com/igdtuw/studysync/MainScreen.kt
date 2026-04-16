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


        val dateText = findViewById<TextView>(R.id.dateText)
        val addSubjectBtn = findViewById<Button>(id.addSubjectBtn)
        addSubjectBtn.setOnClickListener { showAddSubjectDialog() }
// current date + day
        val calendar = Calendar.getInstance()

        val day = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)

// set text
        dateText.text = "$day, $date"

        recycler = findViewById(id.subjectRecycler)

        adapter = SubjectAdapter(
            subjectList,
            // Add
                { position ->
                showAddTopicDialog(subjectList[position], adapter)
            },
            // Edit
            { position, topic ->
                showEditTopicDialog(subjectList[position], topic, adapter)
            },
            // Delete
            { position, topic ->
                showDeleteDialog(subjectList[position], topic, adapter)
            }
        )

        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = adapter

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
                    for (document in result) {
                        val name = document.getString("name") ?: ""
                       val id=document.id
                        val subject = Subject(name, mutableListOf(),id)
                        subjectList.add(subject)
                    }
                    adapter.notifyDataSetChanged()
                }
        }

        val profile = findViewById<TextView>(id.profileIcon)
        profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        val tracker = findViewById<TextView>(id.trackerIcon)
        tracker.setOnClickListener {
            startActivity(Intent(this, TrackerActivity::class.java))
        }
        val task = findViewById<TextView>(id.taskIcon)
        task.setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        
        scheduleDailyReminder(16, 0, "Reminder", "Complete today's tasks")
        scheduleDailyReminder(20, 0, "Progress Check", "Check today's progress")
        scheduleSundayReminder(10, 0)

        val signOut = findViewById<TextView>(id.signOutBtn)
        signOut.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(string.sign_out))
                .setMessage(getString(string.sign_out_message))
                .setPositiveButton(getString(string.yes)) { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    finish()
                }
                .setNegativeButton(getString(string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
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
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
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

                val subjectName = editText.text.toString()

                if (subjectName.isEmpty()) {
                    Toast.makeText(this, "Enter subject", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val auth = FirebaseAuth.getInstance()
                val db = FirebaseFirestore.getInstance()
                val userId = auth.currentUser!!.uid

                val subjectMap = hashMapOf(
                    "name" to subjectName
                )

                db.collection("users")
                    .document(userId)
                    .collection("subjects")
                    .add(subjectMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Subject Added", Toast.LENGTH_SHORT).show()

                        // 🔥 refresh UI
                        recreate()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()    }

    private fun showAddTopicDialog(subject: Subject, adapter: SubjectAdapter) {
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
        var selectedHour = 0
        var selectedMinute = 0

        timeBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, h, m ->
                selectedHour = h
                selectedMinute = m
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                timeBtn.text = getString(string.selected_time, selectedTime)
            }, hour, minute, true).show()
        }

        builder.setView(layout)

        builder.setPositiveButton(getString(string.save)) { _, _ ->
            val name = topicInput.text.toString()
            if (name.isNotEmpty() && selectedHour != -1) {
                subject.topics.add(Topic(name, selectedTime))
                adapter.notifyDataSetChanged()

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val timeInMillis = calendar.timeInMillis

                scheduleNotification(
                    timeInMillis,
                    "Start Study",
                    "Start: $name"
                )
            }
        }

        builder.setNegativeButton(getString(string.cancel), null)
        builder.show()
    }

    fun showEditTopicDialog(subject: Subject, topic: Topic, adapter: SubjectAdapter) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_topic, null)

        val topicInput = dialogView.findViewById<EditText>(R.id.topicInput)
        val timeBtn = dialogView.findViewById<TextView>(R.id.timeBtn)

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
                topic.name = topicInput.text.toString()
                topic.time = selectedTime
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showDeleteDialog(subject: Subject, topic: Topic, adapter: SubjectAdapter) {
        AlertDialog.Builder(this)
            .setTitle("Delete Topic")
            .setMessage("Are you sure you want to delete?")
            .setPositiveButton("Yes") { _, _ ->
                subject.topics.remove(topic)
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("No", null)
            .show()
    }

    fun scheduleNotification(timeInMillis: Long, title: String, message: String) {
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
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }

    fun scheduleDailyReminder(hour: Int, minute: Int, title: String, message: String) {
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
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun scheduleSundayReminder(hour: Int, minute: Int) {
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
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
    }
}
