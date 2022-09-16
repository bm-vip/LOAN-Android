package ir.behrooz.loan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import ir.behrooz.loan.common.AlarmReceiver;
import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.CashtEntityDao;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntityDao;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.entity.WalletEntityDao;

import static ir.behrooz.loan.common.Constants.IRANSANS_LT;

public class CashActivity extends BaseActivity {

    private EditText name, currencyType;
    private Switch withDeposit, checkCashRemain, affectNext, notifyDayOfLoan;
    private Long cashId;
    private CashtEntityDao cashtEntityDao;
    private PersonEntityDao personEntityDao;
    private LoanEntityDao loanEntityDao;
    private WalletEntityDao walletEntityDao;
    private DebitCreditEntityDao debitCreditEntityDao;
    private AlarmReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash);
        alarmReceiver = new AlarmReceiver();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#3F51B5"));
        }

        cashtEntityDao = DBUtil.getWritableInstance(this).getCashtEntityDao();
        personEntityDao = DBUtil.getWritableInstance(this).getPersonEntityDao();
        loanEntityDao = DBUtil.getWritableInstance(this).getLoanEntityDao();
        walletEntityDao = DBUtil.getWritableInstance(this).getWalletEntityDao();
        debitCreditEntityDao = DBUtil.getWritableInstance(this).getDebitCreditEntityDao();
        name = findViewById(R.id.name);
        currencyType = findViewById(R.id.currencyType);
        withDeposit = findViewById(R.id.withDeposit);
        checkCashRemain = findViewById(R.id.checkCashRemain);
        affectNext = findViewById(R.id.affectNext);
        notifyDayOfLoan = findViewById(R.id.notifyDayOfLoan);
        notifyDayOfLoan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    alarmReceiver.setAlarm(context);
                } else {
                    alarmReceiver.cancelAlarm(context);
                }
            }
        });
        withDeposit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    withDeposit.setText(getString(R.string.withDeposit));
                    checkCashRemain.setChecked(true);
                    checkCashRemain.setEnabled(true);
                } else {
                    withDeposit.setText(getString(R.string.withoutDeposit));
                    checkCashRemain.setChecked(false);
                    checkCashRemain.setEnabled(false);
                }
            }
        });
        if (getIntent().hasExtra("cashId")) {
            cashId = getIntent().getExtras().getLong("cashId");
            loadForm();
        } else {
            cashId = null;
        }
    }

    private void loadForm() {
        CashtEntity cashtEntity = cashtEntityDao.load(cashId);
        name.setText(cashtEntity.getName());
        currencyType.setText(cashtEntity.getCurrencyType());
        withDeposit.setChecked(cashtEntity.getWithDeposit());
        checkCashRemain.setChecked(cashtEntity.getCheckCashRemain());
        affectNext.setChecked(cashtEntity.getAffectNext());
        notifyDayOfLoan.setChecked(cashtEntity.getNotifyDayOfLoan());
    }

    public void saveCash(View view) {
        name.setError(null);
        currencyType.setError(null);

        View focusView = null;
        boolean cancel = focusView != null;

        if (TextUtils.isEmpty(name.getText())) {
            name.setError(getString(R.string.error_field_required));
            focusView = name;
            cancel = true;
        } else if (TextUtils.isEmpty(currencyType.getText())) {
            currencyType.setError(getString(R.string.error_field_required));
            focusView = currencyType;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            CashtEntity cashEntity;
            if (cashId == null) {
                cashEntity = new CashtEntity();
            } else
                cashEntity = cashtEntityDao.load(cashId);

            cashEntity.setName(name.getText().toString());
            cashEntity.setCurrencyType(currencyType.getText().toString());
            cashEntity.setWithDeposit(withDeposit.isChecked());
            cashEntity.setCheckCashRemain(checkCashRemain.isChecked());
            cashEntity.setNotifyDayOfLoan(notifyDayOfLoan.isChecked());
            cashEntity.setAffectNext(affectNext.isChecked());

            cashtEntityDao.save(cashEntity);
            finish();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (cashId != null) {
            getMenuInflater().inflate(R.menu.delete_bar_view, menu);
            return super.onCreateOptionsMenu(menu);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.appDeleteBar:
                if (cashId != null) {
                    if (cashtEntityDao.count() == 1L) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.cannotDeleteThisItem), Snackbar.LENGTH_LONG).show();
                        return true;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.areYouSure));
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteCash(cashId);
                            dialog.dismiss();
                            finish();
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    ViewGroup viewGroup = (ViewGroup) dialog.findViewById(android.R.id.content);
                    new FontChangeCrawler(context.getAssets(), IRANSANS_LT).replaceFonts(viewGroup);
                }
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    private void deleteCash(long cashId) {
        personEntityDao.queryBuilder().where(PersonEntityDao.Properties.CashId.eq(cashId)).buildDelete().executeDeleteWithoutDetachingEntities();
        loanEntityDao.queryBuilder().where(LoanEntityDao.Properties.CashId.eq(cashId)).buildDelete().executeDeleteWithoutDetachingEntities();
        walletEntityDao.queryBuilder().where(WalletEntityDao.Properties.CashId.eq(cashId)).buildDelete().executeDeleteWithoutDetachingEntities();
        debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.CashId.eq(cashId)).buildDelete().executeDeleteWithoutDetachingEntities();
        cashtEntityDao.deleteByKey(cashId);
    }
}
