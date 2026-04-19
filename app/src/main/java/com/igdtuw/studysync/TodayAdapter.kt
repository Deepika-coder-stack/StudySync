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
        val taskStatus = view.findViewById<TextView>(R.id.taskStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskName.text = task.name
        
        // Remove listener before setting checked state to avoid trigger loop
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = task.status == "completed"
        
        // Set initial status text
        holder.taskStatus.text = task.status.replaceFirstChar { it.uppercase() }

        val context = holder.itemView.context
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // checkbox -> complete / pending
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val newStatus = if (isChecked) "completed" else "pending"
            
            // Update UI locally first for instant feedback
            task.status = newStatus
            holder.taskStatus.text = newStatus.replaceFirstChar { it.uppercase() }
            
            db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(task.id)
                .update("status", newStatus)
        }

        // long press -> missed
        holder.itemView.setOnLongClickListener {
            val newStatus = "missed"
            task.status = newStatus
            holder.taskStatus.text = "Missed"
            holder.checkBox.isChecked = false
            
            db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(task.id)
                .update("status", newStatus)

            Toast.makeText(context, "Marked Missed", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun getItemCount() = taskList.size
}
