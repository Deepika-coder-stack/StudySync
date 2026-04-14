package com.igdtuw.studysync

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
// Make sure to import your Button, EditText, etc.

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is the line that connects to your XML file
        setContentView(R.layout.activity_login)
        setContentView(R.layout.activity_basic_info)





    }
}