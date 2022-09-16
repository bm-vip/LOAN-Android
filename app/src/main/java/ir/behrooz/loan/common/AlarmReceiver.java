package ir.behrooz.loan.common;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.DebitCreditEntity;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntity;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.model.NotificationModel;

import static ir.behrooz.loan.common.DateUtil.truncate;
import static ir.behrooz.loan.common.StringUtil.moneySeparator;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM_RECEIVER = "AlarmReceiver";
    public static final Integer REQUEST_CODE = 1001;
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        // Put here YOUR code.

        DebitCreditEntityDao debitCreditEntityDao = DBUtil.getReadableInstance(context).getDebitCreditEntityDao();
        CashtEntity cashtEntity = new CashtEntity(context);
        List<DebitCreditEntity> list = debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.CashId.eq(cashtEntity.getId()),DebitCreditEntityDao.Properties.Date.le(truncate(new Date())), DebitCreditEntityDao.Properties.PayStatus.eq(false)).list();
        if (list != null && list.size() > 0) {
            List<NotificationModel> notificationModelList = new ArrayList<>();
            for (DebitCreditEntity debitCreditEntity : list) {
                PersonEntity personEntity = DBUtil.getReadableInstance(context).getPersonEntityDao().load(debitCreditEntity.getPersonId());
                LoanEntity loanEntity = DBUtil.getReadableInstance(context).getLoanEntityDao().load(debitCreditEntity.getLoanId());

                notificationModelList.add(new NotificationModel(
                        debitCreditEntity.getId(),
                        String.format("%s %s", personEntity.getName(), personEntity.getFamily()),
                        String.format("%s %s %s %s %s",
                                context.getString(R.string.loanPayTime),
                                loanEntity.getName(),
                                context.getString(R.string.withAmount),
                                moneySeparator(context, debitCreditEntity.getValue()),
                                context.getString(R.string.isComming)), debitCreditEntity.getDate()));
            }

            Utils.showNotification(context, notificationModelList);
        }


        wl.release();
    }

    public void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_ALARM_RECEIVER);//my custom string action name
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pendingIntent); // Millis * Second * Minute
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
        return  (PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE) != null);//just changed the flag
    }
}
