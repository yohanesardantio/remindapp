package com.mobileprak.remindapp.adapters

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobileprak.remindapp.R
import com.mobileprak.remindapp.models.Reminder
import com.mobileprak.remindapp.utils.Util

class RemindAdapter(private val itemClick: OnItemClickListener) : RecyclerView.Adapter<RemindAdapter.ViewHolder>() {

    var reminderList = mutableListOf<Reminder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.item_reminder, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return reminderList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(reminderList[position], position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindItems(reminder: Reminder, position: Int) {

            val serialTV = itemView.findViewById<TextView>(R.id.serialTV)
            val descriptionTV = itemView.findViewById<TextView>(R.id.descriptionTV)
            val reminderTV = itemView.findViewById<TextView>(R.id.reminderTV)
            val timeTV = itemView.findViewById<TextView>(R.id.timeTV)
            val dateTV = itemView.findViewById<TextView>(R.id.dateTV)

            serialTV.text = "${position + 1}."
            descriptionTV.text = reminder.description

            val reminderDate = Util.getFormattedDate(reminder.date + " " + reminder.time, "dd/MM/yyyy HH:mm")

            val currentTimeMillis = System.currentTimeMillis()
            if (reminderDate.time < currentTimeMillis) {

                descriptionTV.paintFlags = descriptionTV.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {

                descriptionTV.paintFlags = descriptionTV.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }


            reminderTV.text = reminder.title
            timeTV.text = reminder.time
            dateTV.text = reminder.date

            itemView.findViewById<View>(R.id.more).setOnClickListener {
                itemClick.onItemClick(reminder, it, adapterPosition)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(
            reminder: Reminder,
            view: View,
            position: Int
        )
    }
}

