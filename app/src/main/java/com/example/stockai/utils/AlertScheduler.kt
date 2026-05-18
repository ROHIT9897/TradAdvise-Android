package com.example.stockai.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.stockai.data.models.HorizonPredictionResponse
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object AlertScheduler {

    fun scheduleAlert(
        context:    Context,
        prediction: HorizonPredictionResponse
    ) {
        val sdf        = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val notifyDate = sdf.parse(prediction.notifyDateTs) ?: return
        val now        = Date()
        val delayMs    = notifyDate.time - now.time

        if (delayMs <= 0) return

        val data = workDataOf(
            "ticker"      to prediction.ticker,
            "targetPrice" to prediction.targetPrice,
            "stopLoss"    to prediction.stopLoss,
            "targetDate"  to prediction.targetDate,
            "signal"      to prediction.signal,
            "upsidePct"   to prediction.upsidePct,
        )

        val request = OneTimeWorkRequestBuilder<AlertWorker>()
            .setInputData(data)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag("horizon_alert_${prediction.ticker}")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "alert_${prediction.ticker}_${prediction.notifyDateTs}",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }
}

class AlertWorker(
    context:      Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val ticker      = inputData.getString("ticker")      ?: return Result.failure()
        val targetPrice = inputData.getDouble("targetPrice", 0.0)
        val stopLoss    = inputData.getDouble("stopLoss",    0.0)
        val targetDate  = inputData.getString("targetDate")  ?: ""
        val signal      = inputData.getString("signal")      ?: ""
        val upsidePct   = inputData.getDouble("upsidePct",  0.0)

        showNotification(ticker, targetPrice, stopLoss, targetDate, signal, upsidePct)
        return Result.success()
    }

    private fun showNotification(
        ticker:      String,
        targetPrice: Double,
        stopLoss:    Double,
        targetDate:  String,
        signal:      String,
        upsidePct:   Double
    ) {
        val manager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val channel = NotificationChannel(
            "horizon_alerts",
            "Investment Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "TradAdvise investment horizon alerts"
        }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat
            .Builder(applicationContext, "horizon_alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$ticker — Review your position")
            .setContentText(
                "Target ₹$targetPrice (+$upsidePct%) by $targetDate"
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Your $signal position review date is here.\n" +
                            "Target:    ₹$targetPrice (+$upsidePct%)\n" +
                            "Stop Loss: ₹$stopLoss\n" +
                            "Target date: $targetDate\n" +
                            "Open TradAdvise to see current signal."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(ticker.hashCode(), notification)
    }
}