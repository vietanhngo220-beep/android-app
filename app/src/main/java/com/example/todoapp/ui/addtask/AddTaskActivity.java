package com.example.todoapp.ui.addtask;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.todoapp.R;
import com.example.todoapp.utils.DateTimeUtils;
import com.example.todoapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

/**
 * Màn hình Thêm mới / Chỉnh sửa công việc (AddTaskActivity).
 */
public class AddTaskActivity extends AppCompatActivity {

    private MaterialToolbar toolbarAddTask;
    private TextInputLayout tilTaskTitle;
    private TextInputLayout tilDueTime;
    private AutoCompleteTextView actCategory;
    private TextInputEditText etTaskTitle;
    private TextInputEditText etDueTime;
    private TextInputEditText etNote;
    private MaterialButton btnSaveTask;
    private MaterialButton btnCancelTask;

    private AddTaskViewModel viewModel;
    private SessionManager sessionManager;

    private int taskId = -1;
    private long selectedDueTimeMillis = -1;
    private boolean isCompleted = false;
    private boolean isFormModified = false;

    private static final String[] CATEGORIES = new String[]{"Công việc", "Cá nhân", "Học tập", "Khác"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        sessionManager = new SessionManager(this);

        if (getIntent() != null && getIntent().hasExtra("EXTRA_TASK_ID")) {
            taskId = getIntent().getIntExtra("EXTRA_TASK_ID", -1);
        }

        initViews();
        setupToolbar();
        setupCategoryDropdown();
        setupViewModel();
        setupListeners();
        setupBackPressed();

        etTaskTitle.requestFocus();
        showKeyboard(etTaskTitle);
    }

    private void initViews() {
        toolbarAddTask = findViewById(R.id.toolbarAddTask);
        tilTaskTitle = findViewById(R.id.tilTaskTitle);
        tilDueTime = findViewById(R.id.tilDueTime);
        actCategory = findViewById(R.id.actCategory);
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etDueTime = findViewById(R.id.etDueTime);
        etNote = findViewById(R.id.etNote);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnCancelTask = findViewById(R.id.btnCancelTask);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarAddTask);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (taskId > 0) {
            toolbarAddTask.setTitle(R.string.title_edit_task);
        } else {
            toolbarAddTask.setTitle(R.string.title_add_task);
        }
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        actCategory.setAdapter(adapter);
        actCategory.setText(CATEGORIES[0], false);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AddTaskViewModel.class);

        if (taskId > 0) {
            viewModel.loadTask(taskId);
        }

        viewModel.getExistingTask().observe(this, task -> {
            if (task != null) {
                etTaskTitle.setText(task.getTitle());
                etNote.setText(task.getNote());
                if (!TextUtils.isEmpty(task.getCategory())) {
                    actCategory.setText(task.getCategory(), false);
                }
                selectedDueTimeMillis = task.getDueTime();
                etDueTime.setText(DateTimeUtils.formatDateTime(selectedDueTimeMillis));
                isCompleted = task.isCompleted();
                isFormModified = false;
            }
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, getString(R.string.task_saved), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (!TextUtils.isEmpty(message)) {
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        TextWatcher changeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isFormModified = true;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etTaskTitle.addTextChangedListener(changeWatcher);
        etNote.addTextChangedListener(changeWatcher);

        etDueTime.setOnClickListener(v -> showDateTimePicker());
        tilDueTime.setEndIconOnClickListener(v -> showDateTimePicker());

        btnSaveTask.setOnClickListener(v -> performSaveTask());

        btnCancelTask.setOnClickListener(v -> handleBackOrCancel());

        etTaskTitle.setOnClickListener(v -> showKeyboard(etTaskTitle));
        etNote.setOnClickListener(v -> showKeyboard(etNote));
    }

    private void showKeyboard(View view) {
        if (view != null) {
            view.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);
        }
    }

    private void setupBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackOrCancel();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackOrCancel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDueTimeMillis > 0) {
            calendar.setTimeInMillis(selectedDueTimeMillis);
        }

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày hoàn thành")
                .setSelection(calendar.getTimeInMillis())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTimeInMillis(selection);

            showTimePicker(selectedCal);
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(Calendar selectedCal) {
        int hour = selectedCal.get(Calendar.HOUR_OF_DAY);
        int minute = selectedCal.get(Calendar.MINUTE);

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Chọn giờ hoàn thành")
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            selectedCal.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            selectedCal.set(Calendar.MINUTE, timePicker.getMinute());
            selectedCal.set(Calendar.SECOND, 0);

            selectedDueTimeMillis = selectedCal.getTimeInMillis();
            etDueTime.setText(DateTimeUtils.formatDateTime(selectedDueTimeMillis));
            tilDueTime.setError(null);
            isFormModified = true;
        });

        timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void performSaveTask() {
        tilTaskTitle.setError(null);
        tilDueTime.setError(null);

        String title = etTaskTitle.getText() != null ? etTaskTitle.getText().toString().trim() : "";
        String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";
        String category = actCategory.getText() != null ? actCategory.getText().toString().trim() : CATEGORIES[0];

        boolean isValid = true;

        if (TextUtils.isEmpty(title)) {
            tilTaskTitle.setError(getString(R.string.err_title_empty));
            isValid = false;
        }

        if (selectedDueTimeMillis <= 0) {
            tilDueTime.setError(getString(R.string.err_due_time_empty));
            isValid = false;
        } else if (selectedDueTimeMillis < System.currentTimeMillis() - 60000 && taskId <= 0) {
            tilDueTime.setError(getString(R.string.err_past_due_time));
            isValid = false;
        }

        if (isValid) {
            int userId = sessionManager.getUserId();
            viewModel.saveTask(userId, taskId, title, selectedDueTimeMillis, note, category, isCompleted);
        }
    }

    private void handleBackOrCancel() {
        if (isFormModified) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.cancel_confirm_title)
                    .setMessage(R.string.cancel_confirm_message)
                    .setPositiveButton(R.string.btn_discard, (dialog, which) -> finish())
                    .setNegativeButton(R.string.btn_stay, null)
                    .show();
        } else {
            finish();
        }
    }
}
