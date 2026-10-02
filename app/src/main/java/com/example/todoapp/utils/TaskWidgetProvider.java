package com.example.todoapp.utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.todoapp.R;
import com.example.todoapp.data.local.AppDatabase;
import com.example.todoapp.ui.main.MainActivity;

/**
 * App Widget hiển thị công việc hôm nay ở màn hình chính.
 */
public class TaskWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_today_tasks);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);

        SessionManager sessionManager = new SessionManager(context);
        int userId = sessionManager.getUserId();

        if (userId > 0) {
            AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                views.setTextViewText(R.id.tvWidgetTitle, "To-Do List Widget");
                views.setTextViewText(R.id.tvWidgetCount, "Xin chào " + sessionManager.getFullName());
                views.setTextViewText(R.id.tvWidgetContent, "Chạm vào đây để mở danh sách công việc");
                appWidgetManager.updateAppWidget(appWidgetId, views);
            });
        } else {
            views.setTextViewText(R.id.tvWidgetCount, "Chưa đăng nhập");
            views.setTextViewText(R.id.tvWidgetContent, "Mở ứng dụng để đăng nhập");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
