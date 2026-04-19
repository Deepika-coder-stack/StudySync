package com.igdtuw.studysync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: CircleImageView
    private lateinit var cameraIcon: ImageView
    private lateinit var editIcon: ImageView
    private lateinit var openTrackerButton: Button

    private lateinit var nameText: TextView
    private lateinit var courseText: TextView
    private lateinit var emailText: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                profileImage.setImageURI(it)
                saveProfileImage(it.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImage = findViewById(R.id.profileImage)
        cameraIcon = findViewById(R.id.cameraIcon)
        editIcon = findViewById(R.id.editIcon)
        openTrackerButton = findViewById(R.id.openTrackerButton)

        nameText = findViewById(R.id.nameText)
        courseText = findViewById(R.id.courseText)
        emailText = findViewById(R.id.emailText)

        loadProfileData()

        profileImage.setOnClickListener { openGallery() }
        cameraIcon.setOnClickListener { openGallery() }

        editIcon.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        openTrackerButton.setOnClickListener {
            val intent = Intent(this, TrackerActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData() // Refresh data when coming back from EditProfile
    }

    private fun loadProfileData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email ?: ""
            emailText.text = email
            
            val nameFromEmail = if (email.contains("@")) {
                email.substringBefore("@")
            } else {
                "User"
            }
            nameText.text = nameFromEmail

            val userId = currentUser.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val course = document.getString("course") ?: "Not Set"
                        courseText.text = course
                        
                        val profileImgUri = document.getString("profileImage")
                        if (!profileImgUri.isNullOrEmpty()) {
                            profileImage.setImageURI(Uri.parse(profileImgUri))
                        }
                    }
                }
        }
    }

    private fun saveProfileImage(uriString: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .update("profileImage", uriString)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Picture Updated", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }
}
