package me.rtfeng.wirelessadb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import eu.chainfire.libsuperuser.Shell;

public class MainService extends Service{
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private NotificationManager mNM;
    private int NOTIFICATION = R.string.service_started;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.e(LOG_TAG, "Received start id " + startId + ": " + intent);
//        Log.e(LOG_TAG, "Wireless ADB continue");
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        CharSequence text = getText(R.string.service_started);
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.app_name))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        mNM.notify(NOTIFICATION, notification);
        startForeground(110, notification);
        enableAdb();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        stopForeground(true);
        // Tell the user we stopped.
        Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void enableAdb() {
        final String[] commands = {"setprop service.adb.tcp.port 5555", "stop adbd", "start adbd"};
        Thread runSu = new Thread(new Runnable() {
            @Override
            public void run() {
//                Log.e(LOG_TAG, "Before Su cmd");
                Shell.SU.run(commands);
//                Log.e(LOG_TAG, "After Su cmd");
            }
        });
        runSu.start();
        try {
            runSu.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e(LOG_TAG, "Su cmd");
    }
}
