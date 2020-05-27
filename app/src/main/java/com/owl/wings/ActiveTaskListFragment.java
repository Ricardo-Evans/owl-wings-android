package com.owl.wings;

import com.owl.downloader.core.Task;

public class ActiveTaskListFragment extends TaskListFragment {
    private static final TaskListViewModel.Filter FILTER = task -> task.status() == Task.Status.ACTIVE;

    public ActiveTaskListFragment() {
        super(FILTER);
    }

    public ActiveTaskListFragment(int contentLayoutId) {
        super(contentLayoutId, FILTER);
    }
}
