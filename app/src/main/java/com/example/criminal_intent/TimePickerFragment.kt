package com.example.criminal_intent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date

private const val ARG_TIME = "time"
private const val NEW_TIME = "new_time"

class TimePickerFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val timeListener = TimePickerDialog.OnTimeSetListener {
                _: TimePicker, hours: Int, minutes: Int ->

            val result = Bundle().apply {
                putInt("hours", hours)
                putInt("minutes", minutes)
            }
            parentFragmentManager.setFragmentResult(NEW_TIME, result)
        }
        val time =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                arguments?.getSerializable(ARG_TIME, Date::class.java) as Date
            else
                arguments?.getSerializable(ARG_TIME) as Date
        val calendar = Calendar.getInstance().apply {
            this.time = time
        }
        val hours = calendar.get(Calendar.HOUR)
        val minutes = calendar.get(Calendar.MINUTE)
        return TimePickerDialog(
            requireContext(),
            timeListener,
            hours,
            minutes,
            true
        )
    }

    companion object {

        fun newInstance(time: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TIME, time)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }

    }

}