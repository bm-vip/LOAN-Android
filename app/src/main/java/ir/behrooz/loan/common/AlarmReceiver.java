package ir.behrooz.loan.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ir.behrooz.loan.DebitCreditActivity;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM_RECEIVER = "AlarmReceiver";
    public static final int REQUEST_CODE = 1001;
    private PendingIntent pendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        // Put here YOUR code.
        Utils.showNotification(context, DebitCreditActivity.getAllUnpaidInstallment(context));
        wl.release();
    }

    public void setAlarm(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WAKE_LOCK}, REQUEST_CODE);
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SET_ALARM) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.SET_ALARM}, REQUEST_CODE);
            }
        }
        _setAlarm(context);
    }

    private void _setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),  AlarmManager.INTERVAL_DAY, getPendingIntent(context)); // Millis * Second * Minute
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_ALARM_RECEIVER);//my custom string action name
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    public boolean isAlarmRunning(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);//the same as up
        intent.setAction(ACTION_ALARM_RECEIVER);//the same as up
        return  (PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null);
    }
    private PendingIntent getPendingIntent(Context context) {
        if(pendingIntent != null)
            return pendingIntent;
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.setAction(ACTION_ALARM_RECEIVER);//my custom string action name
        pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, alarmIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
