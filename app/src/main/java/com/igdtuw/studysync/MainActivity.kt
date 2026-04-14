package com.igdtuw.studysync

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Open Profile screen when app starts
        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
        startActivity(intent)

        // Close MainActivity so user can't return here
        finish()
    }
}