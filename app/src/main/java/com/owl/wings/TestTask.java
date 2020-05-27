package com.owl.wings;

import com.owl.downloader.core.BaseTask;
import com.owl.downloader.core.FileData;

import java.util.List;
import java.util.Random;

public class TestTask extends BaseTask {
    private long downloadedLength = 0;
    private long downloadSpeed = 0;
    private long totalLength;
    private Random random = new Random();

    public TestTask(String name) {
        super(name);
        totalLength = random.nextInt(1 << 29) + (1 << 28);
    }

    @Override
    public long downloadSpeed() {
        return downloadSpeed;
    }

    @Override
    public long uploadSpeed() {
        return 0;
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
        return totalLength;
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
                downloadSpeed = random.nextInt(1 << 22) + (1 << 23);
                downloadedLength += downloadSpeed;
                if (downloadedLength >= totalLength()) {
                    downloadedLength = totalLength();
                    changeStatus(Status.COMPLETED);
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                changeStatus(Status.ERROR, e);
            }
        }
    }
}
