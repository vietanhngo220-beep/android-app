package com.example.todoapp.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.todoapp.data.model.Task;
import com.example.todoapp.data.model.User;
import com.example.todoapp.utils.PasswordUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lớp Quản lý Cơ sở dữ liệu Room theo mẫu Singleton (Double-Checked Locking).
 */
@Database(entities = {User.class, Task.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "todo_app_db.db";
    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public abstract UserDao userDao();
    public abstract TaskDao taskDao();

    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback chèn dữ liệu tài khoản mẫu khi cơ sở dữ liệu được tạo lần đầu.
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                UserDao dao = INSTANCE.userDao();
                if (dao.countUsers() == 0) {
                    // Tạo tài khoản mẫu doc123 / 123456
                    String salt = PasswordUtils.generateSalt();
                    String hash = PasswordUtils.hashPassword("123456", salt);
                    User sampleUser = new User("doc123", hash, salt, "Nguyễn Văn A");
                    dao.insert(sampleUser);
                }
            });
        }
    };
}
