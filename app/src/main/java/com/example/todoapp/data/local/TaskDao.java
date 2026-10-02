package com.example.todoapp.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.todoapp.data.model.Task;

import java.util.List;

/**
 * Data Access Object (DAO) làm việc với bảng tasks.
 */
@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Update
    int update(Task task);

    @Delete
    int delete(Task task);

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    Task getById(int taskId);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND dueTime >= :startOfDay AND dueTime <= :endOfDay ORDER BY isCompleted ASC, dueTime ASC")
    LiveData<List<Task>> getTodayTasks(int userId, long startOfDay, long endOfDay);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND dueTime > :endOfDay ORDER BY isCompleted ASC, dueTime ASC")
    LiveData<List<Task>> getUpcomingTasks(int userId, long endOfDay);

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    void updateCompleted(int taskId, boolean isCompleted);
}
