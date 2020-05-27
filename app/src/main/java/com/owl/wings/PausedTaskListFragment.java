package com.owl.wings;

import com.owl.downloader.core.Task;

public class PausedTaskListFragment extends TaskListFragment {
    private static final TaskListViewModel.Filter FILTER = task -> task.status() == Task.Status.PAUSED;

    public PausedTaskListFragment() {
        super(FILTER);
    }

    public PausedTaskListFragment(int contentLayoutId) {
        super(contentLayoutId, FILTER);
    }
}
