package com.example.todoapp.ui.main;

import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.R;
import com.example.todoapp.data.model.Task;
import com.example.todoapp.utils.DateTimeUtils;
import com.google.android.material.checkbox.MaterialCheckBox;

/**
 * Adapter RecyclerView hiển thị danh sách công việc đa loại view type (Header, Task, Empty).
 */
public class TaskAdapter extends ListAdapter<TaskAdapterItem, RecyclerView.ViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
        void onTaskCompletedChanged(Task task, boolean isCompleted);
    }

    private final OnTaskClickListener listener;

    public TaskAdapter(OnTaskClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<TaskAdapterItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<TaskAdapterItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull TaskAdapterItem oldItem, @NonNull TaskAdapterItem newItem) {
            if (oldItem.getViewType() != newItem.getViewType()) return false;
            if (oldItem.getViewType() == TaskAdapterItem.TYPE_HEADER) {
                return TextUtils.equals(oldItem.getHeaderTitle(), newItem.getHeaderTitle());
            }
            if (oldItem.getViewType() == TaskAdapterItem.TYPE_TASK) {
                return oldItem.getTask() != null && newItem.getTask() != null &&
                        oldItem.getTask().getId() == newItem.getTask().getId();
            }
            return true;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskAdapterItem oldItem, @NonNull TaskAdapterItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TaskAdapterItem.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == TaskAdapterItem.TYPE_EMPTY) {
            View view = inflater.inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskAdapterItem item = getItem(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item.getHeaderTitle());
        } else if (holder instanceof TaskViewHolder && item.getTask() != null) {
            ((TaskViewHolder) holder).bind(item.getTask(), item.isUpcomingSection(), listener);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSectionTitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tvSectionTitle);
        }

        void bind(String title) {
            tvSectionTitle.setText(title);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCheckBox cbTaskCompleted;
        private final TextView tvTaskCategory;
        private final TextView tvTaskTitle;
        private final TextView tvTaskDueTime;
        private final TextView tvTaskNote;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbTaskCompleted = itemView.findViewById(R.id.cbTaskCompleted);
            tvTaskCategory = itemView.findViewById(R.id.tvTaskCategory);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDueTime = itemView.findViewById(R.id.tvTaskDueTime);
            tvTaskNote = itemView.findViewById(R.id.tvTaskNote);
        }

        void bind(Task task, boolean isUpcomingSection, OnTaskClickListener listener) {
            tvTaskTitle.setText(task.getTitle());

            if (tvTaskCategory != null) {
                if (!TextUtils.isEmpty(task.getCategory())) {
                    tvTaskCategory.setVisibility(View.VISIBLE);
                    tvTaskCategory.setText(task.getCategory());
                } else {
                    tvTaskCategory.setVisibility(View.GONE);
                }
            }

            if (!TextUtils.isEmpty(task.getNote())) {
                tvTaskNote.setVisibility(View.VISIBLE);
                tvTaskNote.setText(task.getNote());
            } else {
                tvTaskNote.setVisibility(View.GONE);
            }

            String timeText;
            if (isUpcomingSection) {
                timeText = DateTimeUtils.formatDateTime(task.getDueTime());
            } else {
                timeText = DateTimeUtils.formatTimeOnly(task.getDueTime());
            }

            boolean isOverdue = DateTimeUtils.isOverdue(task.getDueTime(), task.isCompleted());
            if (isOverdue) {
                tvTaskDueTime.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_overdue));
                tvTaskDueTime.setText(itemView.getContext().getString(R.string.overdue_label, timeText));
            } else {
                tvTaskDueTime.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                tvTaskDueTime.setText(timeText);
            }

            cbTaskCompleted.setOnCheckedChangeListener(null);
            cbTaskCompleted.setChecked(task.isCompleted());

            if (task.isCompleted()) {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskTitle.setAlpha(0.5f);
                tvTaskNote.setAlpha(0.5f);
            } else {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTaskTitle.setAlpha(1.0f);
                tvTaskNote.setAlpha(1.0f);
            }

            cbTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskCompletedChanged(task, isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTaskLongClick(task);
                }
                return true;
            });
        }
    }
}
