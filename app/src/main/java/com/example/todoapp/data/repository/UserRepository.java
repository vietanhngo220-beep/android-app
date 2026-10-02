package com.example.todoapp.data.repository;

import android.content.Context;

import com.example.todoapp.data.local.AppDatabase;
import com.example.todoapp.data.local.UserDao;
import com.example.todoapp.data.model.User;

import java.util.concurrent.ExecutorService;

/**
 * Repository xử lý các thao tác dữ liệu liên quan đến người dùng.
 */
public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executor;

    public interface Callback<T> {
        void onResult(T result);
    }

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        executor = AppDatabase.getDatabaseWriteExecutor();
    }

    /**
     * Lấy thông tin người dùng theo tên đăng nhập trên luồng nền.
     */
    public void getUserByUsername(String username, Callback<User> callback) {
        executor.execute(() -> {
            User user = userDao.getByUsername(username);
            callback.onResult(user);
        });
    }

    /**
     * Đăng ký người dùng mới trên luồng nền.
     */
    public void registerUser(User user, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                User existing = userDao.getByUsername(user.getUsername());
                if (existing != null) {
                    callback.onResult(false);
                    return;
                }
                long id = userDao.insert(user);
                callback.onResult(id > 0);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult(false);
            }
        });
    }
}
