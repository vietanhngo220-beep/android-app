package com.example.todoapp.ui.addtask;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todoapp.data.model.Task;
import com.example.todoapp.data.repository.TaskRepository;
import com.example.todoapp.utils.NotificationUtils;

/**
 * ViewModel quản lý việc thêm mới hoặc chỉnh sửa công việc.
 */
public class AddTaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final MutableLiveData<Task> existingTask = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AddTaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
    }

    public LiveData<Task> getExistingTask() {
        return existingTask;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadTask(int taskId) {
        repository.getTaskById(taskId, existingTask::postValue);
    }

    public void saveTask(int userId, int taskId, String title, long dueTime, String note, String category, boolean isCompleted) {
        if (taskId > 0) {
            Task task = new Task(userId, title, dueTime, note, category, isCompleted, System.currentTimeMillis());
            task.setId(taskId);
            repository.updateTask(task, success -> {
                if (Boolean.TRUE.equals(success)) {
                    NotificationUtils.scheduleTaskReminder(getApplication(), taskId, title, note, dueTime);
                    saveSuccess.postValue(true);
                } else {
                    errorMessage.postValue("Cập nhật công việc thất bại");
                }
            });
        } else {
            Task task = new Task(userId, title, dueTime, note, category, false, System.currentTimeMillis());
            repository.insertTask(task, success -> {
                if (Boolean.TRUE.equals(success)) {
                    saveSuccess.postValue(true);
                } else {
                    errorMessage.postValue("Lưu công việc mới thất bại");
                }
            });
        }
    }
}
