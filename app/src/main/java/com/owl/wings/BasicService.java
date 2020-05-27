package com.owl.wings;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.owl.downloader.core.Session;
import com.owl.downloader.core.Task;
import com.owl.downloader.event.Dispatcher;
import com.owl.downloader.event.Event;
import com.owl.downloader.event.EventHandler;

import java.io.IOException;

public class BasicService extends Service implements EventHandler {
    private Exception exception = null;
    private static final int NOTIFICATION_ID = 1;

    @Override
    public boolean handle(Event event, Task task, Exception e) {
        if (e != null) Log.e(MainApplication.PACKAGE, "an underlying exception happened", e);
        return false;
    }

    public static class Binder extends android.os.Binder {
        private Exception exception;

        private Binder(Exception exception) {
            this.exception = exception;
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public Binder(@Nullable String descriptor, Exception exception) {
            super(descriptor);
            this.exception = exception;
        }

        public Exception exception() {
            return exception;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Session.getInstance().start();
            Session.getInstance().setDirectory(MainApplication.DEFAULT_PATH);
            Dispatcher.getInstance().attach(this);
        } catch (IOException e) {
            exception = e;
        }
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, MainApplication.PACKAGE);
        } else builder = new Notification.Builder(this);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.icon);
        builder.setLargeIcon(icon).setSmallIcon(R.mipmap.icon);
        builder.setContentTitle(getResources().getString(R.string.name)).setContentText(getResources().getString(R.string.service_text));
        Intent intent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        startForeground(NOTIFICATION_ID, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder(exception);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Session.getInstance().stop();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(MainApplication.PACKAGE, "stop background service failed", e);
        }
    }
}
