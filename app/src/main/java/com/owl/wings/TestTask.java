package com.owl.wings;

import com.owl.downloader.core.BaseTask;
import com.owl.downloader.core.FileData;

import java.util.List;

public class TestTask extends BaseTask {
    private long downloadedLength = 0;

    public TestTask(String name) {
        super(name);
    }

    @Override
    public long downloadSpeed() {
        return 1000000000;
    }

    @Override
    public long uploadSpeed() {
        return 1000000000;
    }

    @Override
    public long downloadedLength() {
        return downloadedLength;
    }

    @Override
    public long uploadedLength() {
        return 0;
    }

    @Override
    public long totalLength() {
        return 1 << 20;
    }

    public void setDownloadedLength(long downloadedLength) {
        this.downloadedLength = downloadedLength;
    }

    @Override
    public List<FileData> files() {
        return null;
    }

    @Override
    public void run() {
        while (status() == Status.ACTIVE && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
                downloadedLength += 10240;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
