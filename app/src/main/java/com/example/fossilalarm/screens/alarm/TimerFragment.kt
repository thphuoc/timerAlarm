package com.example.fossilalarm.screens.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import com.example.fossilalarm.R
import com.example.fossilalarm.screens.alarm.backgroundServices.TimerService
import com.example.fossilalarm.screens.alarm.entity.TimerPayload
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_home.*

class TimerFragment : BaseFragment() {
    private lateinit var timerPayload: TimerPayload

    private val timeOverDialog by lazy {
        MaterialDialog(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timerSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ timerPayload ->
                onTimeChanged((timerPayload))
            }, {})
    }

    private fun onTimeChanged(timerPayload: TimerPayload) {
        this.timerPayload = timerPayload
        progressTimer.progressMax = timerPayload.totalDurationInMs.toFloat()
        val remainingProgress =
            (timerPayload.totalDurationInMs - timerPayload.currentCountingDownInMs)
        progressTimer.progress = remainingProgress.toFloat()

        if (timerPayload.isRunning) {
            btnPauseOrStart.setText(R.string.btn_pause)
        } else {
            btnPauseOrStart.setText(R.string.btn_start)
            if (timerPayload.isOver) {
                showTimeOverDialog()
            }
        }
        tvTimer.text = timerPayload.timeToDisplay
    }

    private fun showTimeOverDialog() {
        if (!timeOverDialog.isShowing) {
            timeOverDialog.show {
                message(res = R.string.msg_time_over)
                positiveButton(res = R.string.btn_ok) {
                    it.dismiss()
                    stopTimerService()
                }
            }
        }
    }

    private fun stopTimerService() {
        val intent = Intent(activity, TimerService::class.java)
        intent.putExtra("totalDuration", timerPayload.totalDurationInMs)
        intent.putExtra("currentCountingDown", timerPayload.totalDurationInMs)
        startService(intent)
        progressTimer.progress = 0f
    }

    private fun pauseTimerService() {
        val intent = Intent(activity, TimerService::class.java)
        intent.putExtra("totalDuration", timerPayload.totalDurationInMs)
        intent.putExtra("currentCountingDown", timerPayload.currentCountingDownInMs)
        startService(intent)
    }

    private fun startTimerService() {
        val intent = Intent(activity, TimerService::class.java)
        intent.putExtra("state", "running")
        intent.putExtra("totalDuration", timerPayload.totalDurationInMs)
        intent.putExtra("currentCountingDown", timerPayload.currentCountingDownInMs)
        startService(intent)
    }

    private fun startService(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(intent)
        } else {
            activity?.startService(intent)
        }
    }

    @OnClick(R.id.btnPauseOrStart)
    fun onClickPauseOrStart() {
        if (timerPayload.isRunning) {
            btnPauseOrStart.setText(R.string.btn_start)
            pauseTimerService()
        } else {
            btnPauseOrStart.setText(R.string.btn_pause)
            startTimerService()
        }
    }

    @OnClick(R.id.btnCancel)
    fun onClickCancel() {
        stopTimerService()
    }

    @OnClick(R.id.imgAdd)
    fun onClickAddTimer() {
        showDialogAddDuration()
    }

    private fun showDialogAddDuration() {
        PickerDialogFragment().show(
            activity?.fragmentManager!!,
            onSelectedDuration = { durationInMs ->
                timerPayload.totalDurationInMs = durationInMs
                timerPayload.currentCountingDownInMs = timerPayload.totalDurationInMs
                tvTimer.text = timerPayload.totalDurationInMs.convertMilisecondToDurationHHMMSS()
                startTimerService()
            })
    }
}