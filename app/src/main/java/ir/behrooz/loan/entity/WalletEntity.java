package ir.behrooz.loan.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

import ir.behrooz.loan.common.DateUtil;

/**
 * Created by Behrooz Mohamadi on 16/10/29.
 */
@Entity(nameInDb = "Wallet")
public class WalletEntity {

    @Id(autoincrement = true)
    private Long id;
    private Long value;
    private Date date;
    private Long personId;
    private String description;
    private boolean status;//true=payment, false=receive
    private Long cashId;

    @Generated(hash = 413468292)
    public WalletEntity(Long id, Long value, Date date, Long personId,
            String description, boolean status, Long cashId) {
        this.id = id;
        this.value = value;
        this.date = date;
        this.personId = personId;
        this.description = description;
        this.status = status;
        this.cashId = cashId;
    }

    @Generated(hash = 1363662176)
    public WalletEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getValue() {
        return this.value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = DateUtil.truncate(date);
    }

    public Long getPersonId() {
        return this.personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Long getCashId() {
        return this.cashId;
    }

    public void setCashId(Long cashId) {
        this.cashId = cashId;
    }
}