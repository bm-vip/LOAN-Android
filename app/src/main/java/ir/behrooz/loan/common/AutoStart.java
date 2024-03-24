package ir.behrooz.loan.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ir.behrooz.loan.model.NotificationModel;

public class AutoStart extends BroadcastReceiver {
    private final AlarmReceiver alarm = new AlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        List<NotificationModel> notificationModelList = new ArrayList<>();
        notificationModelList.add(new NotificationModel(
                3L,
                "AutoStart called",
                "set alarm called", new Date()));
        Utils.showNotification(context, notificationModelList);
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            alarm.setAlarm(context);
        }
    }
}

