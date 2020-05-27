package com.owl.wings;

import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.owl.downloader.core.Session;
import com.owl.downloader.core.Task;
import com.owl.downloader.event.Dispatcher;
import com.owl.downloader.event.Event;
import com.owl.downloader.event.EventHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaskListViewModel extends ViewModel implements Runnable, EventHandler {
    private static final Map<Task.Status, Integer> statusMap = new HashMap<>();
    private final List<TaskData> dataList = new LinkedList<>();
    private final ReentrantReadWriteLock dataLock = new ReentrantReadWriteLock();
    private OnInsertListener onInsertListener = null;
    private OnRemoveListener onRemoveListener = null;
    private OnChangeListener onChangeListener = null;
    private Filter filter;
    private final Handler updater = new Handler();
    private volatile boolean cleared = false;

    static {
        statusMap.put(Task.Status.ACTIVE, R.string.active);
        statusMap.put(Task.Status.WAITING, R.string.waiting);
        statusMap.put(Task.Status.PAUSED, R.string.paused);
        statusMap.put(Task.Status.COMPLETED, R.string.completed);
        statusMap.put(Task.Status.ERROR, R.string.error);
    }

    @Override
    public boolean handle(Event event, Task task, Exception e) {
        switch (event) {
            case INSERT: {
                dataLock.writeLock().lock();
                try {
                    dataList.add(calculate(task));
                    if (onInsertListener != null)
                        onInsertListener.onInsert(dataList.size() - 1);
                    break;
                } finally {
                    dataLock.writeLock().unlock();
                }
            }
            case REMOVE: {
                dataLock.writeLock().lock();
                try {
                    int i = 0;
                    Iterator<TaskData> iterator = dataList.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().task == task) {
                            iterator.remove();
                            if (onRemoveListener != null) onRemoveListener.onRemove(i);
                            break;
                        }
                        ++i;
                    }
                    break;
                } finally {
                    dataLock.writeLock().unlock();
                }
            }
            default: {
                dataLock.readLock().lock();
                try {
                    for (TaskData data : dataList) {
                        if (data.task == task) {
                            data.running.postValue(task.status() == Task.Status.ACTIVE || task.status() == Task.Status.WAITING);
                            data.status.postValue(statusMap.get(task.status()));
                            break;
                        }
                    }
                } finally {
                    dataLock.readLock().unlock();
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
        MutableLiveData<Boolean> running = new MutableLiveData<>(true);
        MutableLiveData<Double> progress = new MutableLiveData<>(0.0);
        MutableLiveData<String> download = new MutableLiveData<>(null);
        MutableLiveData<String> upload = new MutableLiveData<>(null);
        MutableLiveData<Integer> status = new MutableLiveData<>(R.string.waiting);
    }

    private TaskData calculate(Task task) {
        TaskData data = new TaskData();
        data.task = task;
        data.content.setValue(task.name());
        data.running.setValue(task.status() == Task.Status.ACTIVE || task.status() == Task.Status.WAITING);
        data.progress.setValue(100.0 * data.task.downloadedLength() / (data.task.totalLength() + 0.001)); // Avoid divide by zero
        data.download.setValue(Util.humanizeDataSize(data.task.downloadSpeed()) + "/s");
        data.upload.setValue(Util.humanizeDataSize(data.task.uploadSpeed()) + "/s");
        data.status.setValue(statusMap.get(task.status()));
        return data;
    }

    public TaskListViewModel() {
        this(null);
    }

    public TaskListViewModel(Filter filter) {
        this.filter = filter;
        Dispatcher.getInstance().attach(this);
        dataLock.writeLock().lock();
        try {
            for (Task task : Session.getInstance().getTasks()) {
                if (filter == null || filter.filter(task)) dataList.add(calculate(task));
            }
        } finally {
            dataLock.writeLock().unlock();
        }
        updater.post(this);
    }

    @Override
    public void run() {
        dataLock.readLock().lock();
        try {
            for (TaskData data : dataList)
                if (data.task.status() == Task.Status.ACTIVE) update(data);
        } finally {
            dataLock.readLock().unlock();
        }
        if (!cleared) updater.postDelayed(this, 1000);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Dispatcher.getInstance().detach(this);
        cleared = true;
    }

    public Task getTask(int index) {
        dataLock.readLock().lock();
        try {
            return dataList.get(index).task;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public LiveData<String> getContent(int index) {
        dataLock.readLock().lock();
        try {
            return dataList.get(index).content;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public LiveData<Boolean> getRunning(int index) {
        dataLock.readLock().lock();
        try {
            return dataList.get(index).running;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public LiveData<Double> getProgress(int index) {
        dataLock.readLock().lock();
        try {
            return dataList.get(index).progress;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public LiveData<String> getUpload(int index) {
        dataLock.readLock().lock();
        try {
            return dataList.get(index).upload;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public LiveData<String> getDownload(int index) {
        dataLock.readLock().lock();
        try {
            return dataList.get(index).download;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public LiveData<Integer> getStatus(int index) {
        dataLock.readLock().lock();
        try {
            return dataList.get(index).status;
        } finally {
            dataLock.readLock().unlock();
        }
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
        dataLock.writeLock().lock();
        try {
            dataList.clear();
            for (Task task : Session.getInstance().getTasks())
                if (filter == null || filter.filter(task)) dataList.add(calculate(task));
            if (onChangeListener != null) onChangeListener.onChange();
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public OnChangeListener getOnChangeListener() {
        return onChangeListener;
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public int size() {
        dataLock.readLock().lock();
        try {
            return dataList.size();
        } finally {
            dataLock.readLock().unlock();
        }
    }

    private void update(TaskData data) {
        data.progress.postValue(100.0 * data.task.downloadedLength() / (data.task.totalLength() + 0.001)); // Avoid divide by zero
        data.download.postValue(Util.humanizeDataSize(data.task.downloadSpeed()) + "/s");
        data.upload.postValue(Util.humanizeDataSize(data.task.uploadSpeed()) + "/s");
    }
}
