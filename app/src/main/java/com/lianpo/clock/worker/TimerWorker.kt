package com.lianpo.clock.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TimerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_DURATION = "duration"
        const val KEY_TIMER_TYPE = "timer_type"

        fun createInputData(duration: Int, timerType: String): Data {
            return Data.Builder()
                .putInt(KEY_DURATION, duration)
                .putString(KEY_TIMER_TYPE, timerType)
                .build()
        }
    }

    override suspend fun doWork(): Result {
        val duration = inputData.getInt(KEY_DURATION, 0)
        val timerType = inputData.getString(KEY_TIMER_TYPE) ?: return Result.failure()

        return try {
            // Perform background timer work here
            // This could include sending notifications, updating database, etc.
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}