package com.owl.wings;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.owl.downloader.core.Session;
import com.owl.downloader.core.Task;
import com.owl.downloader.event.Dispatcher;
import com.owl.downloader.event.Event;
import com.owl.downloader.event.EventHandler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TaskListViewModel extends ViewModel implements Runnable, EventHandler {
    private final List<TaskData> dataList = Collections.synchronizedList(new LinkedList<>());
    private OnInsertListener onInsertListener = null;
    private OnRemoveListener onRemoveListener = null;
    private OnChangeListener onChangeListener = null;
    private Filter filter;
    private final Handler updater = new Handler();
    private volatile boolean cleared = false;

    @Override
    public boolean handle(Event event, Task task, Exception e) {
        switch (event) {
            case INSERT: {
                if (filter == null || filter.filter(task)) insert(task);
                break;
            }
            case REMOVE: {
                if (filter == null || filter.filter(task)) remove(task);
                break;
            }
            default: {
                int i = index(task);
                if (i == -1) {
                    if (filter == null || filter.filter(task)) insert(task);
                } else {
                    if (filter != null && !filter.filter(task)) remove(task);
                    else update(dataList.get(i));
                }
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface OnInsertListener {
        void onInsert(int position);
    }

    @FunctionalInterface
    public interface OnRemoveListener {
        void onRemove(int position);
    }

    @FunctionalInterface
    public interface OnChangeListener {
        void onChange();
    }

    @FunctionalInterface
    public interface Filter {
        boolean filter(Task task);
    }

    private static final class TaskData {
        Task task = null;
        MutableLiveData<String> content = new MutableLiveData<>(null);
        MutableLiveData<Double> progress = new MutableLiveData<>(0.0);
        MutableLiveData<String> download = new MutableLiveData<>(null);
        MutableLiveData<String> upload = new MutableLiveData<>(null);
        MutableLiveData<Task.Status> status = new MutableLiveData<>(Task.Status.WAITING);
    }

    private TaskData calculate(Task task) {
        TaskData data = new TaskData();
        data.task = task;
        data.content.postValue(task.name());
        data.progress.postValue(100.0 * data.task.downloadedLength() / (data.task.totalLength() + 0.001)); // Avoid divide by zero
        data.download.postValue(Util.humanizeDataSize(data.task.downloadSpeed()) + "/s");
        data.upload.postValue(Util.humanizeDataSize(data.task.uploadSpeed()) + "/s");
        data.status.postValue(task.status());
        return data;
    }

    private void insert(Task task) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (index(task) != -1) return;
            List<Task> tasks = Session.getInstance().getTasks();
            int index = 0;
            for (Task task0 : tasks) {
                if (task0 == task || index >= dataList.size()) {
                    dataList.add(index, calculate(task));
                    if (onInsertListener != null) onInsertListener.onInsert(index);
                    return;
                }
                if (getTask(index) == task0) ++index;
            }
        });
    }

    private void remove(Task task) {
        new Handler(Looper.getMainLooper()).post(() -> {
            int i = index(task);
            if (i != -1) {
                dataList.remove(i);
                if (onRemoveListener != null) onRemoveListener.onRemove(i);
            }
        });
    }

    public int index(Task task) {
        for (int i = 0; i < dataList.size(); ++i)
            if (dataList.get(i).task == task) return i;
        return -1;
    }

    public TaskListViewModel() {
        this(null);
    }

    public TaskListViewModel(Filter filter) {
        this.filter = filter;
        Dispatcher.getInstance().attach(this);
        for (Task task : Session.getInstance().getTasks()) {
            if (filter == null || filter.filter(task)) dataList.add(calculate(task));
        }
        updater.post(this);
    }

    @Override
    public void run() {
        for (TaskData data : dataList)
            if (data.task.status() == Task.Status.ACTIVE) update(data);
        if (!cleared) updater.postDelayed(this, 1000);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Dispatcher.getInstance().detach(this);
        cleared = true;
    }

    public Task getTask(int index) {
        return dataList.get(index).task;
    }

    public LiveData<String> getContent(int index) {
        return dataList.get(index).content;
    }

    public LiveData<Double> getProgress(int index) {
        return dataList.get(index).progress;
    }

    public LiveData<String> getUpload(int index) {
        return dataList.get(index).upload;
    }

    public LiveData<String> getDownload(int index) {
        return dataList.get(index).download;
    }

    public LiveData<Task.Status> getStatus(int index) {
        return dataList.get(index).status;
    }

    public OnInsertListener getOnInsertListener() {
        return onInsertListener;
    }

    public void setOnInsertListener(OnInsertListener onInsertListener) {
        this.onInsertListener = onInsertListener;
    }

    public OnRemoveListener getOnRemoveListener() {
        return onRemoveListener;
    }

    public void setOnRemoveListener(OnRemoveListener onRemoveListener) {
        this.onRemoveListener = onRemoveListener;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
        new Handler(Looper.getMainLooper()).post(() -> {
            dataList.clear();
            for (Task task : Session.getInstance().getTasks())
                if (filter == null || filter.filter(task)) dataList.add(calculate(task));
            if (onChangeListener != null) onChangeListener.onChange();
        });
    }

    public OnChangeListener getOnChangeListener() {
        return onChangeListener;
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public int size() {
        return dataList.size();
    }

    private void update(TaskData data) {
        data.progress.postValue(100.0 * data.task.downloadedLength() / (data.task.totalLength() + 0.001)); // Avoid divide by zero
        data.download.postValue(Util.humanizeDataSize(data.task.downloadSpeed()) + "/s");
        data.upload.postValue(Util.humanizeDataSize(data.task.uploadSpeed()) + "/s");
        data.status.postValue(data.task.status());
    }
}
