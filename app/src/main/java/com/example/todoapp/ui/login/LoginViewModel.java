package com.example.todoapp.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todoapp.data.model.User;
import com.example.todoapp.data.repository.UserRepository;
import com.example.todoapp.utils.PasswordUtils;

/**
 * ViewModel quản lý trạng thái và logic kinh doanh cho Đăng nhập & Đăng ký.
 */
public class LoginViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final MutableLiveData<User> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public LiveData<User> getLoginResult() {
        return loginResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    /**
     * Xử lý xác thực đăng nhập.
     */
    public void login(String username, String password) {
        isLoading.setValue(true);
        repository.getUserByUsername(username, user -> {
            isLoading.postValue(false);
            if (user == null) {
                errorMessage.postValue("Sai tên đăng nhập hoặc mật khẩu");
                return;
            }
            boolean isValid = PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash());
            if (isValid) {
                loginResult.postValue(user);
            } else {
                errorMessage.postValue("Sai tên đăng nhập hoặc mật khẩu");
            }
        });
    }

    /**
     * Xử lý tạo tài khoản mới.
     */
    public void register(String username, String fullName, String password) {
        isLoading.setValue(true);
        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(password, salt);

        if (passwordHash == null) {
            isLoading.setValue(false);
            errorMessage.setValue("Lỗi mã hóa mật khẩu");
            return;
        }

        User newUser = new User(username, passwordHash, salt, fullName);
        repository.registerUser(newUser, success -> {
            isLoading.postValue(false);
            if (success) {
                registerSuccess.postValue(true);
            } else {
                errorMessage.postValue("Tên đăng nhập đã tồn tại hoặc xảy ra lỗi");
            }
        });
    }
}
