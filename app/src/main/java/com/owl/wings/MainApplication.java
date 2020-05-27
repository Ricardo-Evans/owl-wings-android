package com.owl.wings;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Environment;

public class MainApplication extends Application {
    public static final String PACKAGE = "com.owl.wings";
    public static final String ACTION_CREATE = "create";
    public static final String FROM_FILE = "file";
    public static final String TASK_ID = "task";
    public static final String DEFAULT_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PACKAGE, getResources().getString(R.string.name), NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}