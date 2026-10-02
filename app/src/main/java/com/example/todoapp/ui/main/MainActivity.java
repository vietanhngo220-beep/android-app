package com.example.todoapp.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.todoapp.R;
import com.example.todoapp.data.model.Task;
import com.example.todoapp.ui.addtask.AddTaskActivity;
import com.example.todoapp.ui.login.LoginActivity;
import com.example.todoapp.utils.SessionManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Màn hình Chính (MainActivity) hiển thị thông tin người dùng, tiến độ, bộ lọc và danh sách công việc.
 */
public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private TextView tvFullName;
    private TextView tvUsername;
    private TextView tvProgressText;
    private LinearProgressIndicator progressIndicator;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilter;
    private FloatingActionButton btnAddTask;
    private ImageButton btnLogout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTasks;

    private MainViewModel viewModel;
    private SessionManager sessionManager;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startLoginActivity();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupSearchAndFilter();
        setupListeners();
        loadUserData();
        checkNotificationPermission();
    }

    private void initViews() {
        tvFullName = findViewById(R.id.tvFullName);
        tvUsername = findViewById(R.id.tvUsername);
        tvProgressText = findViewById(R.id.tvProgressText);
        progressIndicator = findViewById(R.id.progressIndicator);
        etSearch = findViewById(R.id.etSearch);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        btnAddTask = findViewById(R.id.btnAddTask);
        btnLogout = findViewById(R.id.btnLogout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvTasks = findViewById(R.id.rvTasks);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        int userId = sessionManager.getUserId();
        String todayTitle = getString(R.string.section_today);
        String upcomingTitle = getString(R.string.section_upcoming);

        viewModel.initUserTasks(userId, todayTitle, upcomingTitle);

        viewModel.getTaskAdapterItems().observe(this, items -> {
            taskAdapter.submitList(items);
            swipeRefresh.setRefreshing(false);
        });

        viewModel.getProgressSummary().observe(this, summary -> {
            if (tvProgressText != null) {
                tvProgressText.setText(summary);
            }
        });

        viewModel.getProgressPercentage().observe(this, percent -> {
            if (progressIndicator != null) {
                progressIndicator.setProgress(percent != null ? percent : 0, true);
            }
        });
    }

    private void setupSearchAndFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipPending) {
                viewModel.setFilterType(MainViewModel.FilterType.PENDING);
            } else if (checkedId == R.id.chipCompleted) {
                viewModel.setFilterType(MainViewModel.FilterType.COMPLETED);
            } else if (checkedId == R.id.chipOverdue) {
                viewModel.setFilterType(MainViewModel.FilterType.OVERDUE);
            } else {
                viewModel.setFilterType(MainViewModel.FilterType.ALL);
            }
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> {
            int userId = sessionManager.getUserId();
            viewModel.initUserTasks(userId, getString(R.string.section_today), getString(R.string.section_upcoming));
        });

        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void loadUserData() {
        tvFullName.setText(sessionManager.getFullName());
        tvUsername.setText(sessionManager.getUsername());
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
        intent.putExtra("EXTRA_TASK_ID", task.getId());
        startActivity(intent);
    }

    @Override
    public void onTaskLongClick(Task task) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    viewModel.deleteTask(task, success -> {
                        if (Boolean.TRUE.equals(success)) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, getString(R.string.task_deleted), Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void onTaskCompletedChanged(Task task, boolean isCompleted) {
        viewModel.updateTaskCompleted(task.getId(), isCompleted);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_logout)
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton(R.string.menu_logout, (dialog, which) -> {
                    sessionManager.clearSession();
                    startLoginActivity();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
