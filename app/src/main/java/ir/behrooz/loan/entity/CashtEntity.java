package ir.behrooz.loan.entity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.sql.DBUtil;

/**
 * Created by Behrooz Mohamadi on 16/10/29.
 */
@Entity(nameInDb = "Cash")
public class CashtEntity {

    @Id(autoincrement = true)
    private Long id;
    private String name;
    private String currencyType;
    private boolean withDeposit;
    private boolean checkCashRemain;
    private boolean affectNext;
    private boolean notifyDayOfLoan;

    public CashtEntity(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        CashtEntityDao cashtEntityDao = DBUtil.getReadableInstance(context).getCashtEntityDao();
        CashtEntity cashtEntity = cashtEntityDao.load(Long.valueOf(preferences.getString("CASH_LIST", "1")));
        if (cashtEntity == null && cashtEntityDao.count() > 0L) {
            cashtEntity = cashtEntityDao.queryBuilder().orderAsc(CashtEntityDao.Properties.Id).limit(1).offset(0).unique();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("CASH_LIST", cashtEntity.getId().toString());
        }
        if (cashtEntity == null) {
            this.name = context.getString(R.string.app_name);
            this.currencyType = context.getString(R.string.toman);
            this.withDeposit = true;
            this.checkCashRemain = true;
            this.affectNext = true;
            this.notifyDayOfLoan = true;
        } else {
            this.id = cashtEntity.getId();
            this.name = cashtEntity.getName();
            this.currencyType = cashtEntity.getCurrencyType();
            this.withDeposit = cashtEntity.getWithDeposit();
            this.checkCashRemain = cashtEntity.getCheckCashRemain();
            this.affectNext = cashtEntity.getAffectNext();
            this.notifyDayOfLoan = cashtEntity.getNotifyDayOfLoan();
        }
    }

    @Generated(hash = 1663509423)
    public CashtEntity(Long id, String name, String currencyType,
                       boolean withDeposit, boolean checkCashRemain, boolean affectNext,
                       boolean notifyDayOfLoan) {
        this.id = id;
        this.name = name;
        this.currencyType = currencyType;
        this.withDeposit = withDeposit;
        this.checkCashRemain = checkCashRemain;
        this.affectNext = affectNext;
        this.notifyDayOfLoan = notifyDayOfLoan;
    }

    @Generated(hash = 2124985435)
    public CashtEntity() {
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

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public boolean getWithDeposit() {
        return this.withDeposit;
    }

    public void setWithDeposit(boolean withDeposit) {
        this.withDeposit = withDeposit;
    }

    public boolean getCheckCashRemain() {
        return this.checkCashRemain;
    }

    public void setCheckCashRemain(boolean checkCashRemain) {
        this.checkCashRemain = checkCashRemain;
    }

    public boolean getAffectNext() {
        return this.affectNext;
    }

    public void setAffectNext(boolean affectNext) {
        this.affectNext = affectNext;
    }

    public boolean getNotifyDayOfLoan() {
        return this.notifyDayOfLoan;
    }

    public void setNotifyDayOfLoan(boolean notifyDayOfLoan) {
        this.notifyDayOfLoan = notifyDayOfLoan;
    }
}