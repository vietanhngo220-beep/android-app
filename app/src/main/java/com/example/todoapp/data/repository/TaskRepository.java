package com.example.todoapp.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.todoapp.data.local.AppDatabase;
import com.example.todoapp.data.local.TaskDao;
import com.example.todoapp.data.model.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository xử lý các thao tác dữ liệu liên quan đến công việc (Task).
 */
public class TaskRepository {

    private final TaskDao taskDao;
    private final ExecutorService executor;

    public interface Callback<T> {
        void onResult(T result);
    }

    public TaskRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        taskDao = db.taskDao();
        executor = AppDatabase.getDatabaseWriteExecutor();
    }

    public LiveData<List<Task>> getTodayTasks(int userId, long startOfDay, long endOfDay) {
        return taskDao.getTodayTasks(userId, startOfDay, endOfDay);
    }

    public LiveData<List<Task>> getUpcomingTasks(int userId, long endOfDay) {
        return taskDao.getUpcomingTasks(userId, endOfDay);
    }

    public void getTaskById(int taskId, Callback<Task> callback) {
        executor.execute(() -> {
            Task task = taskDao.getById(taskId);
            callback.onResult(task);
        });
    }

    public void insertTask(Task task, Callback<Boolean> callback) {
        executor.execute(() -> {
            long id = taskDao.insert(task);
            if (callback != null) {
                callback.onResult(id > 0);
            }
        });
    }

    public void updateTask(Task task, Callback<Boolean> callback) {
        executor.execute(() -> {
            int rows = taskDao.update(task);
            if (callback != null) {
                callback.onResult(rows > 0);
            }
        });
    }

    public void deleteTask(Task task, Callback<Boolean> callback) {
        executor.execute(() -> {
            int rows = taskDao.delete(task);
            if (callback != null) {
                callback.onResult(rows > 0);
            }
        });
    }

    public void updateTaskCompleted(int taskId, boolean isCompleted) {
        executor.execute(() -> taskDao.updateCompleted(taskId, isCompleted));
    }
}
