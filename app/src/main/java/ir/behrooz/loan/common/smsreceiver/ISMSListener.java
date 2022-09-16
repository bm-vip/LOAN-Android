package ir.behrooz.loan.common.smsreceiver;

public interface ISMSListener {
    public void onReceive(String senderNo, String text);
}
