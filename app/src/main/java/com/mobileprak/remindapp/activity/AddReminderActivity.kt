package com.mobileprak.remindapp.activity

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mobileprak.remindapp.R
import com.mobileprak.remindapp.database.DatabaseHandler
import com.mobileprak.remindapp.services.ReminderService
import com.mobileprak.remindapp.services.AlarmReceiver
import com.mobileprak.remindapp.models.Reminder
import com.mobileprak.remindapp.utils.Util
import java.util.*

class AddReminderActivity : AppCompatActivity() {

    private lateinit var alarmManager: AlarmManager
    private lateinit var databaseHandler: DatabaseHandler
    private val myCalendar = Calendar.getInstance()
    private var date: DatePickerDialog.OnDateSetListener? = null
    private var reminderSaved = Reminder()

    private lateinit var titleET: EditText
    private lateinit var descriptionET: EditText
    private lateinit var dateTV: TextView
    private lateinit var timeTV: TextView
    private lateinit var selectDateButton: Button
    private lateinit var selectTimeButton: Button
    private lateinit var saveBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        titleET = findViewById(R.id.titleET)
        descriptionET = findViewById(R.id.descriptionET)
        dateTV = findViewById(R.id.dateTV)
        timeTV = findViewById(R.id.timeTV)
        selectDateButton = findViewById(R.id.selectDateButton)
        selectTimeButton = findViewById(R.id.selectTimeButton)
        saveBtn = findViewById(R.id.saveBtn)

        databaseHandler = DatabaseHandler(this)

        if (intent.hasExtra("reminder")) {
            reminderSaved = intent.getSerializableExtra("reminder") as Reminder
        }

        date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDate()
        }

        if (reminderSaved.id != 0L) {
            titleET.setText(reminderSaved.title)
            descriptionET.setText(reminderSaved.description)
            dateTV.text = reminderSaved.date
            timeTV.text = reminderSaved.time

            val split = reminderSaved.date.split("/")
            val date = split[0]
            val month = split[1]
            val year = split[2]

            val split1 = reminderSaved.time.split(":")
            val hour = split1[0]
            val minute = split1[1]

            myCalendar.set(Calendar.YEAR, year.toInt())
            myCalendar.set(Calendar.MONTH, month.toInt())
            myCalendar.set(Calendar.DAY_OF_MONTH, date.toInt())

            myCalendar.set(Calendar.HOUR_OF_DAY, hour.toInt())
            myCalendar.set(Calendar.MINUTE, minute.toInt())
            myCalendar.set(Calendar.SECOND, 0)

            saveBtn.text = getString(R.string.update)
        } else {
            updateDate()
            saveBtn.text = getString(R.string.save)
        }

        selectDateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this, date, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = myCalendar.timeInMillis
            datePickerDialog.show()
        }

        selectTimeButton.setOnClickListener {
            val timePickerDialog =
                TimePickerDialog(
                    this,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        myCalendar.set(Calendar.MINUTE, minute)
                        myCalendar.set(Calendar.SECOND, 0)
                        updateTime(hourOfDay, minute)
                    }, myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true
                )
            timePickerDialog.show()
        }

        saveBtn.setOnClickListener {
            if (titleET.text?.isEmpty() == true) {
                Util.showToastMessage(this, "Please select title")
            } else if (timeTV.text == getString(R.string.time)) {
                Util.showToastMessage(this, "Please select time")
            } else {
                val title = titleET.text.toString()
                val description = descriptionET.text.toString()
                val time = timeTV.text.toString()
                val date = dateTV.text.toString()

                val reminder = Reminder()

                reminder.title = title
                reminder.description = description
                reminder.time = time
                reminder.date = date

                val saveReminderId = databaseHandler.saveOrUpdateReminder(reminder)
                if (saveReminderId != 0L) {
                    Log.d("AlarmTime", "Hour: ${myCalendar.get(Calendar.HOUR_OF_DAY)}")
                    Log.d("AlarmTime", "Min: ${myCalendar.get(Calendar.MINUTE)}")
                    setRemainderAlarm(saveReminderId)
                } else {
                    Util.showToastMessage(this, "Failed to save reminder")
                }
            }
        }
    }

    private fun updateDate() {
        val formattedDate = Util.getFormattedDateInString(myCalendar.timeInMillis, "dd/MM/yyyy")
        dateTV.text = formattedDate
    }

    @SuppressLint("SetTextI18n")
    private fun updateTime(hour: Int, minute: Int) {
        timeTV.text = String.format("%02d:%02d", hour, minute)
    }

    private fun setRemainderAlarm(savedReminderId: Long) {
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderService = ReminderService()
        val reminderReceiverIntent = Intent(this, AlarmReceiver::class.java)

        reminderReceiverIntent.putExtra("reminderId", savedReminderId)
        reminderReceiverIntent.putExtra("isServiceRunning", isServiceRunning(reminderService))
        val pendingIntent = PendingIntent.getBroadcast(
            this, savedReminderId.toInt(), reminderReceiverIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val formattedDate =
            Util.getFormattedDateInString(myCalendar.timeInMillis, "dd/MM/yyyy HH:mm")
        Log.d("TimeSetInMillis:", formattedDate)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, myCalendar.timeInMillis, pendingIntent
            )
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, myCalendar.timeInMillis, pendingIntent)
        }

        Util.showToastMessage(this, "Alarm is set at: $formattedDate")
        finish()
    }

    @Suppress("DEPRECATION")
    private fun isServiceRunning(reminderService: ReminderService): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (reminderService.javaClass.name == service.service.className) {
                Log.i("isMyServiceRunning?", true.toString() + "")
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString() + "")
        return false
    }
}
