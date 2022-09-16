package ir.behrooz.loan.common.smsreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
    private ISMSListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                if (pdu_Objects != null) {
                    for (Object aObject : pdu_Objects) {
                        SmsMessage currentSMS = getIncomingMessage(aObject, bundle);
                        String senderNo = currentSMS.getDisplayOriginatingAddress();
                        String message = currentSMS.getDisplayMessageBody();
                        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
                        listener = (ISMSListener)context;
                        listener.onReceive(senderNo,message);
                    }
                    this.abortBroadcast();
                    // End of loop
                }
            }
        } // bundle null

    }
    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }
        return currentSMS;
    }
}
