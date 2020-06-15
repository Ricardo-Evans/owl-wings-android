package com.owl.wings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.owl.wings.databinding.FragmentTaskDetailBinding;

public class TaskDetailFragment extends Fragment {
    private TaskViewModel viewModel;

    public TaskDetailFragment() {
    }

    public TaskDetailFragment(int contentLayoutId) {
        super(contentLayoutId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTaskDetailBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_task_detail, container, false);
        return binding.getRoot();
    }
}
