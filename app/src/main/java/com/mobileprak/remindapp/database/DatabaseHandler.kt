package com.mobileprak.remindapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mobileprak.remindapp.models.Reminder

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "RemindMeDB"
        private const val TABLE_REMINDER = "Reminder"
        private const val ID = "id"
        private const val TITLE = "title"
        private const val DESCRIPTION = "description"
        private const val TIME = "time"
        private const val DATE = "date"
        private const val CREATED_TIME = "createdTime"
        private const val MODIFIED_TIME = "modifiedTime"
        private const val NEW_COLUMN = "new_column"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        createTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE $TABLE_REMINDER ADD COLUMN $NEW_COLUMN TEXT")
        }
    }

    private fun createTable(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_REMINDER("
                + "$ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$TITLE TEXT,"
                + "$DESCRIPTION TEXT,"
                + "$TIME TEXT,"
                + "$DATE TEXT,"
                + "$CREATED_TIME TEXT,"
                + "$MODIFIED_TIME TEXT)"
                + "$NEW_COLUMN TEXT DEFAULT '')")
        db?.execSQL(createTable)
    }




    fun saveReminder(reminder: Reminder): Long {
        val db = this.writableDatabase
        val existingReminder = getReminderById(reminder.id)
        return if (existingReminder.id != 0L) {
            -1
        }else {
            val contentValues = ContentValues().apply {
                put(TITLE, reminder.title)
                put(DESCRIPTION, reminder.description)
                put(TIME, reminder.time)
                put(DATE, reminder.date)
                put(CREATED_TIME, System.currentTimeMillis())
                put(MODIFIED_TIME, System.currentTimeMillis())
                put(NEW_COLUMN, "")  // Atur nilai default kolom baru
            }
            db.insert(TABLE_REMINDER, null, contentValues)
        }

        db.close()
    }

    fun getReminderById(reminderId: Long): Reminder {
        val reminder = Reminder()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_REMINDER WHERE $ID = '$reminderId'"
        val cursor = db.rawQuery(query, null)
        if (cursor.count < 1) {
            cursor.close()
            return reminder
        } else {
            cursor.moveToFirst()

            val id = cursor.getString(0).toLong()
            val title = cursor.getString(1)
            val description = cursor.getString(2)
            val time = cursor.getString(3)
            val date = cursor.getString(4)
            val createdTime = cursor.getLong(5)
            val modifiedTime = cursor.getLong(6)

            reminder.id = id
            reminder.title = title
            reminder.description = description
            reminder.date = date
            reminder.time = time
            reminder.createdTime = createdTime
            reminder.modifiedTime = modifiedTime
        }
        cursor.close()
        db.close()
        return reminder
    }

    fun updateReminder(reminder: Reminder): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(ID, reminder.id)
            put(TITLE, reminder.title)
            put(DESCRIPTION, reminder.description)
            put(DATE, reminder.date)
            put(TIME, reminder.time)
            put(CREATED_TIME, reminder.createdTime)
            put(MODIFIED_TIME, System.currentTimeMillis())
        }
        val success = db.update(TABLE_REMINDER, contentValues, "$ID=?", arrayOf(reminder.id.toString()))
        db.close()
        return success
    }

    fun deleteReminderById(id: Long): Int {
        val db = this.writableDatabase
        val rowId = db.delete(TABLE_REMINDER, "$ID=$id", null)
        db.close()
        return rowId
    }

    fun getAll(): MutableList<Reminder> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_REMINDER ORDER BY $MODIFIED_TIME DESC", null)
        val reminderList = mutableListOf<Reminder>()

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val reminder = Reminder().apply {
                    id = cursor.getString(0).toLong()
                    title = cursor.getString(1)
                    description = cursor.getString(2)
                    time = cursor.getString(3)
                    date = cursor.getString(4)
                    createdTime = cursor.getLong(5)
                    modifiedTime = cursor.getLong(6)
                }

                reminderList.add(reminder)
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return reminderList
    }

    fun saveOrUpdateReminder(reminder: Reminder): Long {
        if (reminder.id != 0L) {
            return updateReminder(reminder).toLong()
        } else {
            return saveReminder(reminder)
        }
    }
}
