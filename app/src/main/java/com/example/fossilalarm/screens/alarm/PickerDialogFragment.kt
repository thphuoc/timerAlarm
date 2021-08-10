package com.example.fossilalarm.screens.alarm

import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialogFragment

open class PickerDialogFragment : TimeDurationPickerDialogFragment() {
    private var onSelectedDuration:(duration: Long) -> Unit = {}

    override fun setTimeUnits(): Int {
        return TimeDurationPicker.MM_SS
    }

    override fun onDurationSet(view: TimeDurationPicker?, duration: Long) {
        onSelectedDuration(duration)
    }

    fun show(fragmentManager: android.app.FragmentManager, onSelectedDuration: (duration: Long) -> Unit) {
        this.show(fragmentManager, "tag")
        this.onSelectedDuration = onSelectedDuration
    }
}