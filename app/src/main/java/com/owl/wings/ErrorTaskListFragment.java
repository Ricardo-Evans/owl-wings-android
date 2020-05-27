package com.owl.wings;

import com.owl.downloader.core.Task;

public class ErrorTaskListFragment extends TaskListFragment {
    private static final TaskListViewModel.Filter FILTER = task -> task.status() == Task.Status.ERROR;

    public ErrorTaskListFragment() {
        super(FILTER);
    }

    public ErrorTaskListFragment(int contentLayoutId) {
        super(contentLayoutId, FILTER);
    }
}
