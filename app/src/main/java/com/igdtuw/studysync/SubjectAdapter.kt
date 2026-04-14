package com.igdtuw.studysync

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class SubjectAdapter(private val list: List<Subject>, val onAddTopicClick: (Int) -> Unit,
    val onEditTopicClick: (Int, Topic) -> Unit,
    val onDeleteTopicClick: (Int, Topic) -> Unit ):
    RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {

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
        
        holder.topicContainer.removeAllViews()
        for (topic in subject.topics) {
            val tv = TextView(holder.itemView.context)
            tv.text = "• ${topic.name} (${topic.time})"
            tv.setTextColor(Color.parseColor("#4A90E2"))
            tv.setOnClickListener {
                onEditTopicClick(position, topic)
            }

            tv.setOnLongClickListener {
                onDeleteTopicClick(position, topic)
                true
            }
            tv.textSize = 12f
            holder.topicContainer.addView(tv)
        }

        holder.addTopicBtn.setOnClickListener {
            onAddTopicClick(position)
        }
        
        holder.itemView.setOnClickListener {
            Toast.makeText(it.context, "Opening ${subject.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = list.size
}
