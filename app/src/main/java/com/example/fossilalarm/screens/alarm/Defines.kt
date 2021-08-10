package com.example.fossilalarm.screens.alarm

import com.example.fossilalarm.screens.alarm.entity.TimerPayload
import io.reactivex.subjects.BehaviorSubject

val timerSubject = BehaviorSubject.createDefault(
    TimerPayload(
        currentCountingDownInMs = 5000L,
        totalDurationInMs = 5000L,
        isRunning = false,
        timeToDisplay = "00:00:5"
    )
)

fun Long.convertMilisecondToDurationHHMMSS(): String {
    val seconds = this / 1000
    return String.format(
        "%02d:%02d:%02d.%04d",
        seconds / 3600,
        (seconds % 3600) / 60,
        seconds % 60,
        this % 1000
    )
}