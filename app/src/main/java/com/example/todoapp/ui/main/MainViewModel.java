package com.example.todoapp.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todoapp.data.model.Task;
import com.example.todoapp.data.repository.TaskRepository;
import com.example.todoapp.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel quản lý danh sách công việc, tìm kiếm, lọc và thống kê tiến độ.
 */
public class MainViewModel extends AndroidViewModel {

    public enum FilterType { ALL, PENDING, COMPLETED, OVERDUE }

    private final TaskRepository taskRepository;
    private final MediatorLiveData<List<TaskAdapterItem>> taskAdapterItems = new MediatorLiveData<>();
    private final MutableLiveData<String> progressSummary = new MutableLiveData<>();
    private final MutableLiveData<Integer> progressPercentage = new MutableLiveData<>(0);

    private LiveData<List<Task>> todayTasksLiveData;
    private LiveData<List<Task>> upcomingTasksLiveData;

    private List<Task> currentTodayTasks = new ArrayList<>();
    private List<Task> currentUpcomingTasks = new ArrayList<>();

    private String searchQuery = "";
    private FilterType currentFilter = FilterType.ALL;

    private String todayTitle = "Công việc hôm nay";
    private String upcomingTitle = "Công việc sắp tới";

    public MainViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
    }

    public void initUserTasks(int userId, String todayHeaderTitle, String upcomingHeaderTitle) {
        this.todayTitle = todayHeaderTitle;
        this.upcomingTitle = upcomingHeaderTitle;

        long now = System.currentTimeMillis();
        long startOfDay = DateTimeUtils.getStartOfDay(now);
        long endOfDay = DateTimeUtils.getEndOfDay(now);

        if (todayTasksLiveData != null) {
            taskAdapterItems.removeSource(todayTasksLiveData);
        }
        if (upcomingTasksLiveData != null) {
            taskAdapterItems.removeSource(upcomingTasksLiveData);
        }

        todayTasksLiveData = taskRepository.getTodayTasks(userId, startOfDay, endOfDay);
        upcomingTasksLiveData = taskRepository.getUpcomingTasks(userId, endOfDay);

        taskAdapterItems.addSource(todayTasksLiveData, tasks -> {
            currentTodayTasks = tasks != null ? tasks : new ArrayList<>();
            combineAndEmitList();
        });

        taskAdapterItems.addSource(upcomingTasksLiveData, tasks -> {
            currentUpcomingTasks = tasks != null ? tasks : new ArrayList<>();
            combineAndEmitList();
        });
    }

    public LiveData<List<TaskAdapterItem>> getTaskAdapterItems() {
        return taskAdapterItems;
    }

    public LiveData<String> getProgressSummary() {
        return progressSummary;
    }

    public LiveData<Integer> getProgressPercentage() {
        return progressPercentage;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query.trim().toLowerCase() : "";
        combineAndEmitList();
    }

    public void setFilterType(FilterType filter) {
        this.currentFilter = filter != null ? filter : FilterType.ALL;
        combineAndEmitList();
    }

    private void combineAndEmitList() {
        List<Task> filteredToday = filterTasks(currentTodayTasks);
        List<Task> filteredUpcoming = filterTasks(currentUpcomingTasks);

        List<TaskAdapterItem> items = new ArrayList<>();

        // Mục 1: Công việc hôm nay
        items.add(TaskAdapterItem.createHeader(todayTitle));
        if (filteredToday.isEmpty()) {
            items.add(TaskAdapterItem.createEmpty());
        } else {
            for (Task task : filteredToday) {
                items.add(TaskAdapterItem.createTask(task, false));
            }
        }

        // Mục 2: Công việc sắp tới
        items.add(TaskAdapterItem.createHeader(upcomingTitle));
        if (filteredUpcoming.isEmpty()) {
            items.add(TaskAdapterItem.createEmpty());
        } else {
            for (Task task : filteredUpcoming) {
                items.add(TaskAdapterItem.createTask(task, true));
            }
        }

        calculateProgress();
        taskAdapterItems.setValue(items);
    }

    private List<Task> filterTasks(List<Task> tasks) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            // Kiểm tra từ khóa tìm kiếm
            if (!searchQuery.isEmpty()) {
                boolean matchTitle = task.getTitle().toLowerCase().contains(searchQuery);
                boolean matchNote = task.getNote() != null && task.getNote().toLowerCase().contains(searchQuery);
                boolean matchCategory = task.getCategory() != null && task.getCategory().toLowerCase().contains(searchQuery);
                if (!matchTitle && !matchNote && !matchCategory) {
                    continue;
                }
            }

            // Kiểm tra bộ lọc
            boolean isOverdue = DateTimeUtils.isOverdue(task.getDueTime(), task.isCompleted());
            if (currentFilter == FilterType.PENDING && task.isCompleted()) {
                continue;
            }
            if (currentFilter == FilterType.COMPLETED && !task.isCompleted()) {
                continue;
            }
            if (currentFilter == FilterType.OVERDUE && !isOverdue) {
                continue;
            }

            result.add(task);
        }
        return result;
    }

    private void calculateProgress() {
        int total = currentTodayTasks.size();
        if (total == 0) {
            progressSummary.setValue("Hôm nay chưa có công việc nào");
            progressPercentage.setValue(0);
            return;
        }

        int completed = 0;
        for (Task t : currentTodayTasks) {
            if (t.isCompleted()) {
                completed++;
            }
        }

        int percent = (int) (((float) completed / total) * 100);
        progressSummary.setValue(String.format("Hoàn thành hôm nay: %d/%d công việc (%d%%)", completed, total, percent));
        progressPercentage.setValue(percent);
    }

    public void updateTaskCompleted(int taskId, boolean isCompleted) {
        taskRepository.updateTaskCompleted(taskId, isCompleted);
    }

    public void deleteTask(Task task, TaskRepository.Callback<Boolean> callback) {
        taskRepository.deleteTask(task, callback);
    }
}
