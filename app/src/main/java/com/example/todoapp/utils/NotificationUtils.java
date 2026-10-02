package com.example.todoapp.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.todoapp.R;
import com.example.todoapp.ui.main.MainActivity;

/**
 * Tiện ích quản lý thông báo nhắc nhở công việc (AlarmManager & NotificationManager).
 */
public class NotificationUtils {

    public static final String CHANNEL_ID = "task_reminder_channel";
    public static final String CHANNEL_NAME = "Nhắc nhở công việc";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh thông báo nhắc nhở deadline công việc");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void scheduleTaskReminder(Context context, int taskId, String taskTitle, String taskNote, long dueTimeMillis) {
        if (dueTimeMillis <= System.currentTimeMillis()) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskNotificationReceiver.class);
        intent.putExtra("EXTRA_TASK_ID", taskId);
        intent.putExtra("EXTRA_TASK_TITLE", taskTitle);
        intent.putExtra("EXTRA_TASK_NOTE", taskNote);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueTimeMillis, pendingIntent);
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dueTimeMillis, pendingIntent);
        }
    }

    public static void showNotification(Context context, int taskId, String title, String note) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check)
                .setContentTitle("Nhắc nhở: " + title)
                .setContentText(note != null && !note.isEmpty() ? note : "Đã đến thời gian hoàn thành công việc này!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(taskId, builder.build());
        }
    }
}
