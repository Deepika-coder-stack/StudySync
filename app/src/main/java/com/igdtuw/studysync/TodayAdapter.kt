package com.igdtuw.studysync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TodayAdapter(
    private val taskList: MutableList<Task>
) : RecyclerView.Adapter<TodayAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val taskName = view.findViewById<TextView>(R.id.taskName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskName.text = task.name
        holder.checkBox.isChecked = task.status == "completed"

        val context = holder.itemView.context
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // checkbox -> complete / pending
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val newStatus = if (isChecked) "completed" else "pending"
            db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(task.id)
                .update("status", newStatus)
        }

        // long press -> missed
        holder.itemView.setOnLongClickListener {
            db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(task.id)
                .update("status", "missed")

            Toast.makeText(context, "Marked Missed", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun getItemCount() = taskList.size
}
