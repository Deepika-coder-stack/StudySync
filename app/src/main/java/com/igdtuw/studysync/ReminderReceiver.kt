package com.igdtuw.studysync
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: ""
        Log.d("TEST","Receiver triggered")

        NotificationHelper.showNotification(context, title, message)
    }
}