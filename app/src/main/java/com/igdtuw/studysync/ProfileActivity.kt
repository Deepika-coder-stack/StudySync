package com.igdtuw.studysync

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: CircleImageView
    private lateinit var cameraIcon: ImageView
    private lateinit var editIcon: ImageView
    private lateinit var openTrackerButton: Button

    private lateinit var nameText: TextView
    private lateinit var courseText: TextView
    private lateinit var subjectText: TextView
    private lateinit var emailText: TextView

    private lateinit var sharedPref: android.content.SharedPreferences

    // IMAGE PICKER
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                profileImage.setImageURI(it)
                sharedPref.edit().putString("profileImage", it.toString()).apply()
            }
        }

    // EDIT PROFILE RESULT
    private val editProfileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val data = result.data

                val name = data?.getStringExtra("name")
                val course = data?.getStringExtra("course")
                val subject = data?.getStringExtra("subject")
                val email = data?.getStringExtra("email")

                name?.let { nameText.text = it }
                course?.let { courseText.text = it }
                subject?.let { subjectText.text = it }
                email?.let { emailText.text = it }

                sharedPref.edit()
                    .putString("name", name)
                    .putString("course", course)
                    .putString("subject", subject)
                    .putString("email", email)
                    .apply()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // VIEW BINDING
        profileImage = findViewById(R.id.profileImage)
        cameraIcon = findViewById(R.id.cameraIcon)
        editIcon = findViewById(R.id.editIcon)
        openTrackerButton = findViewById(R.id.openTrackerButton)

        nameText = findViewById(R.id.nameText)
        courseText = findViewById(R.id.courseText)
        subjectText = findViewById(R.id.subjectText)
        emailText = findViewById(R.id.emailText)

        sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)

        loadProfile()

        // PROFILE IMAGE CLICK
        profileImage.setOnClickListener { openGallery() }
        cameraIcon.setOnClickListener { openGallery() }

        // EDIT PROFILE CLICK
        editIcon.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        // OPEN TRACKER SCREEN
        openTrackerButton.setOnClickListener {
            val intent = Intent(this, TrackerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadProfile() {

        nameText.text = sharedPref.getString("name", "Priya Sharma")
        courseText.text = sharedPref.getString("course", "B.Tech CSE")
        subjectText.text = sharedPref.getString("subject", "Computer Science")
        emailText.text = sharedPref.getString("email", "example@gmail.com")

        val savedImageUri = sharedPref.getString("profileImage", null)

        savedImageUri?.let {
            profileImage.setImageURI(Uri.parse(it))
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }
}