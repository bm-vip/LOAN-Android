package ir.behrooz.loan.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

import ir.behrooz.loan.common.DateUtil;

/**
 * Created by Behrooz Mohamadi on 16/10/29.
 */
@Entity(nameInDb = "DebitCredit")
public class DebitCreditEntity {

    @Id(autoincrement = true)
    private Long id;
    private boolean payStatus;//{true:paid, false:unpaid}
    private Long value;
    private Date date;
    private Long personId;
    private Long loanId;
    private String description;
    private Long cashId;
    @Generated(hash = 1868302997)
    public DebitCreditEntity(Long id, boolean payStatus, Long value, Date date,
            Long personId, Long loanId, String description, Long cashId) {
        this.id = id;
        this.payStatus = payStatus;
        this.value = value;
        this.date = date;
        this.personId = personId;
        this.loanId = loanId;
        this.description = description;
        this.cashId = cashId;
    }
    @Generated(hash = 1251550817)
    public DebitCreditEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public boolean getPayStatus() {
        return this.payStatus;
    }
    public void setPayStatus(boolean payStatus) {
        this.payStatus = payStatus;
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
    public Long getLoanId() {
        return this.loanId;
    }
    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Long getCashId() {
        return this.cashId;
    }
    public void setCashId(Long cashId) {
        this.cashId = cashId;
    }

}