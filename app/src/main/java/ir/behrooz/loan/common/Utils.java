package ir.behrooz.loan.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.mojtaba.materialdatetimepicker.utils.LanguageUtils;

import java.util.List;
import java.util.Locale;

import ir.behrooz.loan.DebitCreditActivity;
import ir.behrooz.loan.R;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.model.NotificationModel;

import static ir.behrooz.loan.common.Constants.VERSION;


public class Utils {

    public static String getVersion(Context context) {
        try {
            PackageInfo info = getPackageInfo(context);
            return String.format("%s.%s", info.versionName, info.versionCode);
        } catch (PackageManager.NameNotFoundException ex) {
            return VERSION;
        }
    }

    public static PackageInfo getPackageInfo(Context context) throws PackageManager.NameNotFoundException {
        PackageManager manager = context.getPackageManager();
        return manager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
    }

    public static int getVersionCode(Context context) {
        try {
            return Integer.parseInt(getPackageInfo(context).versionName.replace(".", ""));
        } catch (PackageManager.NameNotFoundException ex) {
            return Integer.parseInt(VERSION.split("\\.")[1] + VERSION.split("\\.")[2]);
        }
    }

    public static boolean landScape(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        return rotation != 0;
    }

    public void setLocale(Context ctx, String lang, String currentLang) {
        Locale myLocale = new Locale(lang);
        Resources res = ctx.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        if (lang == "fa" && currentLang.equals("en")) {
            ctx.startActivity(new Intent(ctx, ctx.getClass()));
        }
        if (lang == "en" && currentLang.equals("fa")) {
            ctx.startActivity(new Intent(ctx, ctx.getClass()));
        }
    }

    public static void setLocale(String lang, Resources resources) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static boolean askForPermission(Context context, String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should show an explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode + 1);
            }
            return false;
        }
        return true;
    }

    public static boolean loadPermissions(Context context, String perm, int requestCode) {
        if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, perm)) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{perm}, requestCode);
                return true;
            }
        }
        return false;
    }

    public static void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void showNotification(Context context, List<NotificationModel> list) {
        String CHANNEL_ID = context.getPackageName().concat(".channel");
        String channelName = context.getString(R.string.app_name);
        String channelGroup = context.getPackageName().concat(".group");

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            //channel.setGroup(channelGroup);
            CashtEntity cashtEntity = new CashtEntity(context);
            channel.setDescription(cashtEntity.getName());
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        for (NotificationModel notificationModel : list) {
            Intent paymentIntent = new Intent(context, DebitCreditActivity.class);
            paymentIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            paymentIntent.putExtra("notification_debitCreditId", notificationModel.getId() + "");
            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, notificationModel.getId().intValue(), paymentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent actionSendIntent = new Intent(Intent.ACTION_SEND);
            actionSendIntent.setType("text/plain");
            actionSendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, notificationModel.getTitle());
            String date = DateUtil.toPersianString(notificationModel.getDate(), "l j F Y");
            actionSendIntent.putExtra(android.content.Intent.EXTRA_TEXT, date.concat("\n").concat(notificationModel.getMessage()));
            PendingIntent pendingNotificationIntent2 = PendingIntent.getActivity(context, notificationModel.getId().intValue(), actionSendIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                    .setShowWhen(false)
                    .setContentTitle(notificationModel.getTitle()) // title for notification
                    .setContentText(notificationModel.getMessage())// message for notification
                    .setSubText(date)
                    .setCategory(Notification.CATEGORY_REMINDER)
                    .setGroup(channelGroup) // group notification
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationModel.getMessage()))
                    .setContentIntent(pendingNotificationIntent)
                    .addAction(R.drawable.ic_payment, context.getString(R.string.payment), pendingNotificationIntent)
                    .addAction(R.drawable.ic_menu_send, context.getString(R.string.send), pendingNotificationIntent2)
                    .setAutoCancel(true)
                    .build();// clear notification after click
            notificationManager.notify(notificationModel.getId().intValue(), notification);
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N && list.size() > 1) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//            inboxStyle.setBigContentTitle("2 new messages")
            inboxStyle.setSummaryText(context.getString(R.string.notifyDayOfLoan));
            for (NotificationModel notificationModel : list) {
                inboxStyle.addLine(notificationModel.getTitle());
            }

            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(String.format("%s %s %s", LanguageUtils.getPersianNumbers(list.size() + ""), context.getString(R.string.installments), context.getString(R.string.unpaid)))
                    //.setContentText("Two new messages")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setStyle(inboxStyle)
                    .setGroup(channelGroup)
                    .setGroupSummary(true).build();
            notificationManager.notify(0, notification);
        }
    }

    public static boolean isServiceRunning(Context context, Class serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
