package com.mobileprak.remindapp.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract.Reminders
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobileprak.remindapp.R
import com.mobileprak.remindapp.adapters.RemindAdapter
import com.mobileprak.remindapp.database.DatabaseHandler
import com.mobileprak.remindapp.models.Reminder
import com.mobileprak.remindapp.services.AlarmReceiver
import com.mobileprak.remindapp.services.ReminderService
import com.mobileprak.remindapp.utils.Util
import com.mobileprak.remindapp.utils.putReminderExtra
import java.util.Locale

class MainActivity : AppCompatActivity(), RemindAdapter.OnItemClickListener {

    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var adapter: RemindAdapter
    private var reminderList = mutableListOf<Reminder>()

    private lateinit var recycler_view_reminder: RecyclerView
    private lateinit var addReminderButton: FloatingActionButton
    private lateinit var searchET: SearchView
    private lateinit var noData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view_reminder = findViewById(R.id.recycler_view_reminder)
        addReminderButton = findViewById(R.id.addReminderButton)
        searchET = findViewById(R.id.searchET)
        noData = findViewById(R.id.noData)

        initAdds()
        databaseHandler = DatabaseHandler(this)
        adapter = RemindAdapter(this)
        recycler_view_reminder.adapter = adapter

        getAllRemindersFromDB()

        addReminderButton.setOnClickListener {
            val reminderIntent = Intent(this, AddReminderActivity::class.java)
            startActivity(reminderIntent)
        }

        val from = intent.getStringExtra("from")
        if (from == "Notification") {
            val reminderId = intent.getLongExtra("reminderId", 0)
            val reminderById = databaseHandler.getReminderById(reminderId)
            showReminderAlert(reminderById)
        }

        searchET.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterData(newText)
                return false
            }
        })
    }

    private fun initAdds() {
        MobileAds.initialize(this) { }
    }

    private fun filterData(query: String) {
        val finalList = if (query.isEmpty()) reminderList else reminderList.filter {
            it.title.toLowerCase(Locale.getDefault())
                .contains(query.toLowerCase(Locale.getDefault())) ||
                    it.description.toLowerCase(Locale.getDefault()).contains(
                        query.toLowerCase(
                            Locale.getDefault()
                        )
                    )
        }
        if (finalList.isNotEmpty()) {
            updateList(finalList.toMutableList())
        }
    }

    private fun updateList(finalList: MutableList<Reminder>) {
        adapter.reminderList = finalList
        adapter.notifyDataSetChanged()

        if (reminderList.size > 0) {
            recycler_view_reminder.visibility = View.VISIBLE
            noData.visibility = View.GONE
        } else {
            recycler_view_reminder.visibility = View.GONE
            noData.visibility = View.VISIBLE
        }
    }

    private fun getAllRemindersFromDB() {
        reminderList = databaseHandler.getAll()
        updateList(reminderList)
    }

    override fun onResume() {
        super.onResume()
        getAllRemindersFromDB()
    }

    private fun showReminderAlert(reminder: Reminder) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(reminder.title)
        builder.setMessage(reminder.description)
        builder.setPositiveButton("STOP ALARM") { dialog, _ ->
            Util.showToastMessage(this, "Your alarm has been stopped")
            dialog.dismiss()
            stopAlarm()
            stopReminderService()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun stopAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }

    private fun stopReminderService() {
        val reminderService = Intent(this, ReminderService::class.java)
        stopService(reminderService)
    }

    override fun onItemClick(
        reminder: Reminder,
        view: View,
        position: Int
    ) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            if (it.title == getString(R.string.update)) {
                startActivity(
                    Intent(this, AddReminderActivity::class.java)
                        .putReminderExtra("reminder", reminder)
                )
            } else if (it.title == getString(R.string.delete)) {
                databaseHandler.deleteReminderById(reminder.id)
                getAllRemindersFromDB()
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }
    
}
