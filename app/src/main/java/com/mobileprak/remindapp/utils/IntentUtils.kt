package com.mobileprak.remindapp.utils

import android.content.Intent
import com.mobileprak.remindapp.models.Reminder

fun Intent.putReminderExtra(key: String, reminder: Reminder): Intent {
    putExtra(key, reminder)
    return this
}

fun putExtra(key: String, reminder: Reminder) {
    return Intent().putExtra(key, reminder)
}

private fun Intent.putExtra(key: String, reminder: Reminder) {

}
