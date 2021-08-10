package com.example.fossilalarm.screens.alarm.entity

data class TimerPayload(
    var currentCountingDownInMs: Long = 0,
    var totalDurationInMs: Long = 50,
    var isRunning: Boolean = false,
    var isOver: Boolean = false,
    var timeToDisplay: String = "00:00:00"
)