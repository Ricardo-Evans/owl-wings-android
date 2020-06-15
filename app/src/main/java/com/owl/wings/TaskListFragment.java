package com.owl.wings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.owl.wings.databinding.TaskItemBinding;

public class TaskListFragment extends Fragment {
    private TaskListViewModel viewModel = null;
    private TaskListViewModel.Filter filter = null;

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
        private OnItemLongClickListener onItemLongClickListener = null;
        private LifecycleOwner owner;

        private Adapter(TaskListViewModel viewModel, LifecycleOwner owner) {
            this.viewModel = viewModel;
            this.owner = owner;
        }

        @FunctionalInterface
        public interface OnItemClickListener {
            void onItemClick(View view, int position);
        }

        @FunctionalInterface
        public interface OnItemLongClickListener {
            void onItemLongClick(View view, int position);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TaskItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.task_item, parent, false);
            binding.setLifecycleOwner(owner);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.binding.setTask(viewModel.getTaskData(position));
            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
            });
            holder.itemView.setOnLongClickListener(v -> {
                if (onItemLongClickListener != null)
                    onItemLongClickListener.onItemLongClick(v, position);
                return true;
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

        public OnItemLongClickListener getOnItemLongClickListener() {
            return onItemLongClickListener;
        }

        public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
            this.onItemLongClickListener = onItemLongClickListener;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TaskItemBinding binding;

        public ViewHolder(TaskItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

