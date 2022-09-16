package ir.behrooz.loan.model;

import java.util.Date;

public class NotificationModel {
    private Long id;
    private String title;
    private String message;
    private Date date;

    public NotificationModel(long id, String title, String message,Date date) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
