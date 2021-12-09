package com.example.iotclock

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hour: Int, minute: Int) {
        var hourFmt = hour.toString()
        var minuteFmt = minute.toString()
        if (minute < 10)
        {
            minuteFmt = "0$minuteFmt"
        }
        if (hour < 10)
        {
            hourFmt = "0$hourFmt"
        }

        if (MainActivity.setClock) {
            (activity as MainActivity).publish("t/time", "$hourFmt:$minuteFmt:00")
        }
        else {
            (activity as MainActivity).publish("t/alarm", "$hourFmt:$minuteFmt:00")
        }
    }
}