package com.owl.wings;

import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.owl.downloader.core.Task;

public class TaskViewModel extends ViewModel implements Runnable {
    private final Task task;
    private MutableLiveData<String> downloadSpeed = new MutableLiveData<>();
    private MutableLiveData<String> uploadSpeed = new MutableLiveData<>();
    private MutableLiveData<String> downloadedLength = new MutableLiveData<>();
    private MutableLiveData<String> uploadedLength = new MutableLiveData<>();
    private MutableLiveData<String> totalLength = new MutableLiveData<>();
    private final Handler updater = new Handler();
    private volatile boolean cleared = false;

    public TaskViewModel(Task task) {
        this.task = task;
        updater.post(this);
    }

    public Task getTask() {
        return task;
    }

    public LiveData<String> getDownloadSpeed() {
        return downloadSpeed;
    }

    public LiveData<String> getUploadSpeed() {
        return uploadSpeed;
    }

    public LiveData<String> getDownloadedLength() {
        return downloadedLength;
    }

    public LiveData<String> getUploadedLength() {
        return uploadedLength;
    }

    public LiveData<String> getTotalLength() {
        return totalLength;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cleared = true;
    }

    @Override
    public void run() {
        downloadSpeed.postValue(Util.humanizeDataSize(task.downloadSpeed()) + "/s");
        uploadSpeed.postValue(Util.humanizeDataSize(task.downloadSpeed()) + "/s");
        downloadedLength.postValue(Util.humanizeDataSize(task.downloadedLength()));
        uploadedLength.postValue(Util.humanizeDataSize(task.uploadedLength()));
        totalLength.postValue(Util.humanizeDataSize(task.totalLength()));
        if (!cleared) updater.postDelayed(this, 1000);
    }
}
