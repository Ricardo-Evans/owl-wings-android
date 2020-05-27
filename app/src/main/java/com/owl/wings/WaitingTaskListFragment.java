package com.owl.wings;

import com.owl.downloader.core.Task;

public class WaitingTaskListFragment extends TaskListFragment {
    private static final TaskListViewModel.Filter FILTER = task -> task.status() == Task.Status.WAITING;

    public WaitingTaskListFragment() {
        super(FILTER);
    }

    public WaitingTaskListFragment(int contentLayoutId) {
        super(contentLayoutId, FILTER);
    }
}
