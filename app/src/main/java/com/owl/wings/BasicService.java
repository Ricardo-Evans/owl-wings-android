package com.owl.wings;

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
import androidx.core.app.NotificationCompat;

import com.owl.downloader.core.Session;

import java.io.IOException;

public class BasicService extends Service {
    private Exception exception = null;
    private static final int NOTIFICATION_ID = 1;

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
        } catch (IOException e) {
            exception = e;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainApplication.PACKAGE);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.icon);
        builder.setLargeIcon(icon).setSmallIcon(R.mipmap.icon);
        builder.setContentTitle(getResources().getString(R.string.name)).setContentText(getResources().getString(R.string.service_text));
        builder.setOngoing(true).setAutoCancel(false);
        builder.setContentIntent(PendingIntent.getActivity(this, -1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
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
