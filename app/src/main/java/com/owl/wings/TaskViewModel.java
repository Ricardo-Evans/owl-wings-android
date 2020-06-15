package com.owl.wings;

import androidx.lifecycle.ViewModel;

import com.owl.downloader.core.Task;

public class TaskViewModel extends ViewModel {
    private Task task;

    public TaskViewModel() {
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
