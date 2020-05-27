package com.owl.wings;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.core.content.ContextCompat;

import java.io.File;

public final class Util {
    private static final String[] UNITS = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};

    private Util() {
    }

    public static boolean checkPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public static String resolveNameFromURI(Context context, Uri uri) {
        String scheme = uri.getScheme();
        if ("content".equalsIgnoreCase(scheme)) {
            String[] projection = new String[]{OpenableColumns.DISPLAY_NAME};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        } else {
            String path = uri.getPath();
            if (path != null) return new File(path).getName();
        }
        return null;
    }

    public static String humanizeDataSize(long dataSize) {
        int i = 0;
        while (dataSize >= 1024 && i < UNITS.length) {
            dataSize >>= 10;
            ++i;
        }
        return dataSize + UNITS[i];
    }
}
