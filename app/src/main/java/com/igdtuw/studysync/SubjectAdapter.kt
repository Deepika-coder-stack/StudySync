package com.igdtuw.studysync

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SubjectAdapter(
    private val list: MutableList<Subject>,
    val onAddTopicClick: (Int) -> Unit,
    val onEditTopicClick: (Int, Topic) -> Unit,
    val onDeleteTopicClick: (Int, Topic) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.subjectName)
        val addTopicBtn: TextView = itemView.findViewById(R.id.addTopicBtn)
        val topicContainer: LinearLayout = itemView.findViewById(R.id.topicContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = list[position]
        
        holder.name.text = subject.name
        
        holder.itemView.setOnLongClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Delete Subject")
                .setMessage("Are you sure you want to delete this subject?")
                .setPositiveButton("Delete") { _, _ ->
                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseFirestore.getInstance()
                    val currentUser = auth.currentUser
                    
                    if (currentUser != null) {
                        val userId = currentUser.uid
                        val subjectId = subject.id

                        if (subjectId.isNotEmpty()) {
                            db.collection("users")
                                .document(userId)
                                .collection("subjects")
                                .document(subjectId)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                    val currentPos = holder.adapterPosition
                                    if (currentPos != RecyclerView.NO_POSITION) {
                                        list.removeAt(currentPos)
                                        notifyItemRemoved(currentPos)
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
        
        holder.topicContainer.removeAllViews()
        for (topic in subject.topics) {
            val tv = TextView(holder.itemView.context)
            tv.text = "• ${topic.name} (${topic.time})"
            tv.setTextColor(Color.parseColor("#4A90E2"))
            tv.setOnClickListener {
                onEditTopicClick(holder.adapterPosition, topic)
            }

            tv.setOnLongClickListener {
                onDeleteTopicClick(holder.adapterPosition, topic)
                true
            }
            tv.textSize = 12f
            holder.topicContainer.addView(tv)
        }

        holder.addTopicBtn.setOnClickListener {
            onAddTopicClick(holder.adapterPosition)
        }
        
        holder.itemView.setOnClickListener {
            Toast.makeText(it.context, "Opening ${subject.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = list.size
}
