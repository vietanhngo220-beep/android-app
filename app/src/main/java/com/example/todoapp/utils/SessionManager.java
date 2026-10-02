package com.example.todoapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Quản lý phiên làm việc của người dùng bằng SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "todo_app_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "full_name";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Lưu thông tin đăng nhập vào SharedPreferences.
     */
    public void saveSession(int userId, String username, String fullName, boolean rememberMe) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.apply();
    }

    /**
     * Kiểm tra người dùng đã đăng nhập hay chưa.
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Kiểm tra người dùng có bật tùy chọn "Ghi nhớ đăng nhập" hay không.
     */
    public boolean isRemembered() {
        return pref.getBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Lấy ID người dùng hiện tại.
     */
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    /**
     * Lấy tên đăng nhập của người dùng.
     */
    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    /**
     * Lấy họ tên người dùng.
     */
    public String getFullName() {
        return pref.getString(KEY_FULL_NAME, "");
    }

    /**
     * Xóa thông tin phiên đăng nhập (Đăng xuất).
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
