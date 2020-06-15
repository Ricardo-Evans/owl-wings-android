package com.owl.wings;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.owl.downloader.log.Level;
import com.owl.downloader.log.Logger;

public class MainApplication extends Application {
    public static final String PACKAGE = "com.owl.wings";
    public static final String ACTION_CREATE = "create";
    public static final String FROM_FILE = "file";
    public static final String TASK_ID = "task";
    public static final String DEFAULT_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    public static final String VERSION = "Version " + BuildConfig.VERSION_NAME + '(' + BuildConfig.VERSION_CODE + ')';

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PACKAGE, getResources().getString(R.string.name), NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
        }
        Logger.setInstance(new Logger() {
            @Override
            public void log(Level level, String message, Exception exception) {
                switch (level) {
                    case VERBOSE: {
                        Log.v(MainApplication.PACKAGE, message, exception);
                        break;
                    }
                    case DEBUG: {
                        Log.d(MainApplication.PACKAGE, message, exception);
                        break;
                    }
                    case INFO: {
                        Log.i(MainApplication.PACKAGE, message, exception);
                        break;
                    }
                    case WARNING: {
                        Log.w(MainApplication.PACKAGE, message, exception);
                        break;
                    }
                    case ERROR: {
                        Log.e(MainApplication.PACKAGE, message, exception);
                        break;
                    }
                }
            }
        });
    }
}