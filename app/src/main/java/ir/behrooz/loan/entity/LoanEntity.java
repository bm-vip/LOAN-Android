package ir.behrooz.loan.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

import ir.behrooz.loan.common.DateUtil;

/**
 * Created by Behrooz Mohamadi on 16/10/29.
 */
@Entity(nameInDb = "Loan")
public class LoanEntity {

    @Id(autoincrement = true)
    private Long id;
    private String name;
    private Long personId;
    private int dayInMonth;
    private Date date;
    private Long value;
    private int installment;
    private Long installmentAmount;
    private boolean settled;
    private Date winDate;
    private Long cashId;
    @Generated(hash = 1981962375)
    public LoanEntity(Long id, String name, Long personId, int dayInMonth,
            Date date, Long value, int installment, Long installmentAmount,
            boolean settled, Date winDate, Long cashId) {
        this.id = id;
        this.name = name;
        this.personId = personId;
        this.dayInMonth = dayInMonth;
        this.date = date;
        this.value = value;
        this.installment = installment;
        this.installmentAmount = installmentAmount;
        this.settled = settled;
        this.winDate = winDate;
        this.cashId = cashId;
    }
    @Generated(hash = 1330899884)
    public LoanEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getPersonId() {
        return this.personId;
    }
    public void setPersonId(Long personId) {
        this.personId = personId;
    }
    public int getDayInMonth() {
        return this.dayInMonth;
    }
    public void setDayInMonth(int dayInMonth) {
        this.dayInMonth = dayInMonth;
    }
    public Date getDate() {
        return this.date;
    }
    public void setDate(Date date) {
        this.date = DateUtil.truncate(date);
    }
    public Long getValue() {
        return this.value;
    }
    public void setValue(Long value) {
        this.value = value;
    }
    public int getInstallment() {
        return this.installment;
    }
    public void setInstallment(int installment) {
        this.installment = installment;
    }
    public Long getInstallmentAmount() {
        return this.installmentAmount;
    }
    public void setInstallmentAmount(Long installmentAmount) {
        this.installmentAmount = installmentAmount;
    }
    public boolean getSettled() {
        return this.settled;
    }
    public void setSettled(boolean settled) {
        this.settled = settled;
    }
    public Date getWinDate() {
        return this.winDate;
    }
    public void setWinDate(Date winDate) {
        this.winDate = DateUtil.truncate(winDate);
    }
    public Long getCashId() {
        return this.cashId;
    }
    public void setCashId(Long cashId) {
        this.cashId = cashId;
    }
   }