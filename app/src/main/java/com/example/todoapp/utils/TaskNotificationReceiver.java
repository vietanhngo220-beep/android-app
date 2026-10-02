package com.example.todoapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver nhận tín hiệu báo thức và hiển thị thông báo nhắc nhở công việc.
 */
public class TaskNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        int taskId = intent.getIntExtra("EXTRA_TASK_ID", 0);
        String title = intent.getStringExtra("EXTRA_TASK_TITLE");
        String note = intent.getStringExtra("EXTRA_TASK_NOTE");

        NotificationUtils.showNotification(context, taskId, title, note);
    }
}
