package com.example.todoapp.ui.main;

import com.example.todoapp.data.model.Task;

import java.util.Objects;

/**
 * Mẫu dữ liệu bọc (Wrapper Item) hỗ trợ Multiple View Types cho TaskAdapter.
 */
public class TaskAdapterItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TASK = 1;
    public static final int TYPE_EMPTY = 2;

    private final int viewType;
    private final String headerTitle;
    private final Task task;
    private final boolean isUpcomingSection;

    public static TaskAdapterItem createHeader(String title) {
        return new TaskAdapterItem(TYPE_HEADER, title, null, false);
    }

    public static TaskAdapterItem createTask(Task task, boolean isUpcomingSection) {
        return new TaskAdapterItem(TYPE_TASK, null, task, isUpcomingSection);
    }

    public static TaskAdapterItem createEmpty() {
        return new TaskAdapterItem(TYPE_EMPTY, null, null, false);
    }

    private TaskAdapterItem(int viewType, String headerTitle, Task task, boolean isUpcomingSection) {
        this.viewType = viewType;
        this.headerTitle = headerTitle;
        this.task = task;
        this.isUpcomingSection = isUpcomingSection;
    }

    public int getViewType() {
        return viewType;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public Task getTask() {
        return task;
    }

    public boolean isUpcomingSection() {
        return isUpcomingSection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskAdapterItem that = (TaskAdapterItem) o;
        if (viewType != that.viewType) return false;
        if (isUpcomingSection != that.isUpcomingSection) return false;
        if (!Objects.equals(headerTitle, that.headerTitle)) return false;

        if (task == null && that.task == null) return true;
        if (task == null || that.task == null) return false;

        return task.getId() == that.task.getId() &&
                task.isCompleted() == that.task.isCompleted() &&
                task.getDueTime() == that.task.getDueTime() &&
                Objects.equals(task.getTitle(), that.task.getTitle()) &&
                Objects.equals(task.getNote(), that.task.getNote());
    }

    @Override
    public int hashCode() {
        return Objects.hash(viewType, headerTitle, task, isUpcomingSection);
    }
}
