package ir.behrooz.loan.model;

import android.content.Intent;

import java.util.Map;

public class MenuModel {
    private Class activity;
    private Map<String,Long> extera;
    private String title;
    private int icon;
    private Intent intent;

    public MenuModel(Class activity, Map<String, Long> extera, String title, int icon) {
        this.activity = activity;
        this.extera = extera;
        this.title = title;
        this.icon = icon;
    }

    public MenuModel(Intent intent, String title, int icon) {
        this.intent = intent;
        this.title = title;
        this.icon = icon;
    }

    public Class getActivity() {
        return activity;
    }

    public void setActivity(Class activity) {
        this.activity = activity;
    }

    public Map<String, Long> getExtera() {
        return extera;
    }

    public void setExtera(Map<String, Long> extera) {
        this.extera = extera;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }
}
