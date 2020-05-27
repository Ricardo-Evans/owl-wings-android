package com.owl.wings;

import com.owl.downloader.core.Task;

public class CompletedTaskListFragment extends TaskListFragment {
    private static final TaskListViewModel.Filter FILTER = task -> task.status() == Task.Status.COMPLETED;

    public CompletedTaskListFragment() {
        super(FILTER);
    }

    public CompletedTaskListFragment(int contentLayoutId) {
        super(contentLayoutId, FILTER);
    }
}
