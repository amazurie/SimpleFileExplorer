package com.arnm.syph.simplefileexplorer.notif;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Syph on 17/12/2017.
 */

public class Notification {

    public static void showToast(final Context context, final String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
        Log.d("NOTIFICATION DEBUG", msg);
    }
}
