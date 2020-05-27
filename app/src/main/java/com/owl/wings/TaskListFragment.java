package com.owl.wings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.owl.downloader.core.Task;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskListFragment extends Fragment {
    private static final Map<Task.Status, Integer> statusMap = new HashMap<>();
    private TaskListViewModel viewModel = null;
    private TaskListViewModel.Filter filter = null;

    static {
        statusMap.put(Task.Status.ACTIVE, R.string.active);
        statusMap.put(Task.Status.WAITING, R.string.waiting);
        statusMap.put(Task.Status.PAUSED, R.string.paused);
        statusMap.put(Task.Status.COMPLETED, R.string.completed);
        statusMap.put(Task.Status.ERROR, R.string.error);
    }

    public TaskListFragment() {
    }

    public TaskListFragment(int contentLayoutId) {
        super(contentLayoutId);
    }

    public TaskListFragment(TaskListViewModel.Filter filter) {
        this.filter = filter;
    }

    public TaskListFragment(int contentLayoutId, TaskListViewModel.Filter filter) {
        super(contentLayoutId);
        this.filter = filter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (viewModel == null) viewModel = new ViewModelProvider(this).get(TaskListViewModel.class);
        viewModel.setFilter(filter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_task_list, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view_task_list);
        Adapter adapter = new Adapter(viewModel, getViewLifecycleOwner());
        viewModel.setOnInsertListener(adapter::notifyItemInserted);
        viewModel.setOnRemoveListener(adapter::notifyItemRemoved);
        viewModel.setOnChangeListener(adapter::notifyDataSetChanged);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((view, position) -> {
            Intent intent = new Intent(getContext(), TaskActivity.class);
            intent.putExtra(MainApplication.ACTION_CREATE, false);
            intent.putExtra(MainApplication.TASK_ID, viewModel.getTask(position).hashCode());
            startActivity(intent);
        });
        return root;
    }

    protected TaskListViewModel getViewModel() {
        return viewModel;
    }

    protected void setViewModel(TaskListViewModel viewModel) {
        this.viewModel = viewModel;
    }

    private static class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private final TaskListViewModel viewModel;
        private OnItemClickListener onItemClickListener = null;
        private LifecycleOwner owner;

        private Adapter(TaskListViewModel viewModel, LifecycleOwner owner) {
            this.viewModel = viewModel;
            this.owner = owner;
        }

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
            });
            Task task = viewModel.getTask(position);
            holder.start.setOnClickListener(v -> {
                if (task.status() == Task.Status.PAUSED || task.status() == Task.Status.ERROR)
                    task.start();
            });
            holder.pause.setOnClickListener(v -> {
                if (task.status() == Task.Status.ACTIVE || task.status() == Task.Status.WAITING)
                    task.pause();
            });
            viewModel.getContent(position).observe(owner, holder.content::setText);
            viewModel.getStatus(position).observe(owner, status -> {
                Integer resource = statusMap.get(status);
                assert resource != null;
                holder.status.setText(resource);
                if (status == Task.Status.ACTIVE || status == Task.Status.WAITING) {
                    holder.pause.setVisibility(View.VISIBLE);
                    holder.start.setVisibility(View.GONE);
                } else if (status == Task.Status.PAUSED || status == Task.Status.ERROR) {
                    holder.start.setVisibility(View.VISIBLE);
                    holder.pause.setVisibility(View.GONE);
                } else {
                    holder.start.setVisibility(View.GONE);
                    holder.pause.setVisibility(View.GONE);
                }
            });
            viewModel.getDownload(position).observe(owner, holder.downloadSpeed::setText);
            viewModel.getUpload(position).observe(owner, holder.uploadSpeed::setText);
            viewModel.getProgress(position).observe(owner, progress -> {
                holder.progress.setText(String.format(Locale.getDefault(), "%.2f%%", progress));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    holder.progressBar.setProgress((int) (double) progress, true);
                else holder.progressBar.setProgress((int) (double) progress);
            });
        }

        @Override
        public int getItemCount() {
            return viewModel.size();
        }

        public OnItemClickListener getOnItemClickListener() {
            return onItemClickListener;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView content;
        private TextView status;
        private ImageButton pause;
        private ImageButton start;
        private ProgressBar progressBar;
        private TextView progress;
        private TextView downloadSpeed;
        private TextView uploadSpeed;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.task_item_content);
            status = itemView.findViewById(R.id.task_item_status);
            start = itemView.findViewById(R.id.task_item_start);
            pause = itemView.findViewById(R.id.task_item_pause);
            progressBar = itemView.findViewById(R.id.task_item_progress);
            progress = itemView.findViewById(R.id.task_item_progress_text);
            downloadSpeed = itemView.findViewById(R.id.task_item_download);
            uploadSpeed = itemView.findViewById(R.id.task_item_upload);
        }
    }
}

