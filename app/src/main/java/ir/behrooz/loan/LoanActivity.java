package ir.behrooz.loan;

import static ir.behrooz.loan.common.Constants.IRANSANS_LT;
import static ir.behrooz.loan.common.DateUtil.addMonth;
import static ir.behrooz.loan.common.DateUtil.addZero;
import static ir.behrooz.loan.common.DateUtil.set;
import static ir.behrooz.loan.common.DateUtil.toGregorian;
import static ir.behrooz.loan.common.DateUtil.toPersianString;
import static ir.behrooz.loan.common.NumberUtil.getLong;
import static ir.behrooz.loan.common.NumberUtil.isNullOrZero;
import static ir.behrooz.loan.common.NumberUtil.round;
import static ir.behrooz.loan.common.StringUtil.fixWeakCharacters;
import static ir.behrooz.loan.common.StringUtil.moneySeparator;
import static ir.behrooz.loan.common.StringUtil.onChangedEditText;
import static ir.behrooz.loan.common.StringUtil.removeSeparator;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.CashId;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.Date;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.Id;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.LoanId;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.PayStatus;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.Value;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.mojtaba.materialdatetimepicker.date.DatePickerDialog;
import com.mojtaba.materialdatetimepicker.utils.PersianCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.LanguageUtils;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.common.sql.Oprator;
import ir.behrooz.loan.common.sql.WhereCondition;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.DebitCreditEntity;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntity;
import ir.behrooz.loan.entity.LoanEntityDao;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.entity.WalletEntityDao;
import ir.behrooz.loan.fragment.PersonSearchFragment;
import ir.behrooz.loan.model.PersonModel;

public class LoanActivity extends BaseActivity {

    private EditText fullName, loanName, dayOfMonth, value, installment, firstInstallmentAmount, otherInstallmentAmount, date, winDate;
    private TextView lblDate;
    private ImageButton fullNameBtn;
    private LinearLayout winDateLinearLayout;
    private Long loanId;
    private Button saveLoanBtn;
    public ProgressBar progressBar;
    private DateListener dateListener;
    private WinDateListener winDateListener;
    private LoanEntityDao loanEntityDao;
    private PersonEntityDao personEntityDao;
    private DebitCreditEntityDao debitCreditEntityDao;
    private List<PersonModel> personModels;
    CashtEntity cashtEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan);
        cashtEntity = new CashtEntity(this);
//        titleBar.setText(getString(R.string.title_activity_loan));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#283593")));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#283593"));
        }

        loanEntityDao = DBUtil.getReadableInstance(this).getLoanEntityDao();
        personEntityDao = DBUtil.getReadableInstance(this).getPersonEntityDao();
        debitCreditEntityDao = DBUtil.getWritableInstance(this).getDebitCreditEntityDao();
        fullNameBtn = findViewById(R.id.fullNameBtn);
        progressBar = findViewById(R.id.loan_progress);
        fullName = findViewById(R.id.fullNameValue);
        saveLoanBtn = findViewById(R.id.saveLoan);
        date = findViewById(R.id.date);
        lblDate = findViewById(R.id.lbl_date);
        loanName = findViewById(R.id.loanName);
        dayOfMonth = findViewById(R.id.dayOfMonth);
        value = findViewById(R.id.value);
        installment = findViewById(R.id.installment);
        firstInstallmentAmount = findViewById(R.id.firstInstallmentAmount);
        otherInstallmentAmount = findViewById(R.id.otherInstallmentAmount);
        winDate = findViewById(R.id.winDate);
        winDateLinearLayout = findViewById(R.id.layout_winDate);

        PersianCalendar now = new PersianCalendar();
        dateListener = new DateListener();
        final DatePickerDialog dpd = DatePickerDialog.newInstance(
                dateListener,
                now.getPersianYear(),
                now.getPersianMonth(),
                now.getPersianDay()
        );
        dpd.vibrate(true);
        dpd.setTitle(getString(R.string.date));
        date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && dpd != null) {
                    dpd.show(getSupportFragmentManager(), "dateDialog");
                    if (cashtEntity.getWithDeposit())
                        saveLoanBtn.requestFocus();
                }
            }
        });

        winDateListener = new WinDateListener();
        final DatePickerDialog wdpd = DatePickerDialog.newInstance(
                winDateListener,
                now.getPersianYear(),
                now.getPersianMonth(),
                now.getPersianDay()
        );
        wdpd.vibrate(true);
        wdpd.setTitle(getString(R.string.winDate));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wdpd.setAccentColor(getColor(R.color.colorPrimaryDark));
            dpd.setAccentColor(getColor(R.color.colorPrimaryDark));
        }
        winDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && wdpd != null) {
                    wdpd.show(getSupportFragmentManager(), "winDateDialog");
                    if (!cashtEntity.getWithDeposit())
                        saveLoanBtn.requestFocus();
                }
            }
        });

        value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onChangedEditText(context, this, value, charSequence, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b)
                    calcInstallment();
            }
        });
        installment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b)
                    calcInstallment();
            }
        });
        firstInstallmentAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onChangedEditText(context, this, otherInstallmentAmount, charSequence, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        otherInstallmentAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onChangedEditText(context, this, otherInstallmentAmount, charSequence, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        Button saveLoan = findViewById(R.id.saveLoan);
        saveLoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLoan(view);
            }
        });
        if (getIntent().hasExtra("loanId")) {
            loanId = getIntent().getExtras().getLong("loanId");
            loadForm();
        } else {
            loanId = null;
        }
        if (getIntent().hasExtra("personId")) {
            PersonEntity personEntity = personEntityDao.load(getIntent().getExtras().getLong("personId"));
            personModels = new ArrayList<>();
            personModels.add(PersonModel.getModel(personEntity));
            fullName.setText(String.format("%s %s", personEntity.getName(), personEntity.getFamily()));

        }
        date.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveLoan(v);
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void calcInstallment() {
        Long val = removeSeparator(context, value.getText().toString());
        Long installmentLong = getLong(installment.getText().toString());
        if (val > 0L && installmentLong > 0L) {
            long amount = val / installmentLong;
            amount = round(amount, 10000L);
            Long first = val - (amount * (installmentLong - 1));
            if (first > 0)
                firstInstallmentAmount.setText(moneySeparator(context, first));
            else firstInstallmentAmount.setText(moneySeparator(context, amount));
            otherInstallmentAmount.setText(moneySeparator(context, amount));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag("dateDialog");
        if (dpd != null) dpd.setOnDateSetListener(dateListener);
        if (cashtEntity.getWithDeposit()) {
            lblDate.setText(context.getString(R.string.receiveDate));
            date.setHint(context.getString(R.string.receiveDate));
            winDateLinearLayout.setVisibility(View.GONE);
        } else {
            lblDate.setText(context.getString(R.string.subscribeDate));
            date.setHint(context.getString(R.string.subscribeDate));
            winDateLinearLayout.setVisibility(View.VISIBLE);
            DatePickerDialog wdpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag("winDateDialog");
            if (wdpd != null) wdpd.setOnDateSetListener(winDateListener);
        }
    }

    public class DateListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String d = year + "/" + addZero(++monthOfYear) + "/" + addZero(dayOfMonth);
            date.setText(LanguageUtils.getPersianNumbers(fixWeakCharacters(d)));
        }
    }

    public class WinDateListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String d = year + "/" + addZero(++monthOfYear) + "/" + addZero(dayOfMonth);
            winDate.setText(LanguageUtils.getPersianNumbers(fixWeakCharacters(d)));
        }
    }


    private void loadForm() {
        LoanEntity loanEntity = loanEntityDao.load(loanId);

        loanName.setText(loanEntity.getName());
        dayOfMonth.setText(LanguageUtils.getPersianNumbers(loanEntity.getDayInMonth() + ""));
        installment.setText(LanguageUtils.getPersianNumbers(loanEntity.getInstallment() + ""));
        date.setText(toPersianString(loanEntity.getDate(), false));
        winDate.setText(toPersianString(loanEntity.getWinDate(), false));

        long first = loanEntity.getValue() - (loanEntity.getInstallmentAmount() * (loanEntity.getInstallment() - 1));
        firstInstallmentAmount.setText(moneySeparator(context, first));
        long other = 0L;
        if (loanEntity.getValue() - first > 0)
            other = loanEntity.getInstallmentAmount();
        otherInstallmentAmount.setText(moneySeparator(context, other));

        value.setText(moneySeparator(context, loanEntity.getValue()));

        PersonEntity personEntity = personEntityDao.load(loanEntity.getPersonId());
        fullName.setText(String.format("%s %s", personEntity.getName(), personEntity.getFamily()));
        personModels = new ArrayList<>();
        personModels.add(PersonModel.getModel(personEntity));

        if (loanEntity.getSettled()) {
            fullNameBtn.setEnabled(false);
            loanName.setEnabled(false);
            dayOfMonth.setEnabled(false);
            installment.setEnabled(false);
            firstInstallmentAmount.setEnabled(false);
            otherInstallmentAmount.setEnabled(false);
            value.setEnabled(false);
            date.setEnabled(false);
            winDate.setEnabled(false);
        }
    }

    public void saveLoan(View view) {
        loanName.setError(null);
        firstInstallmentAmount.setError(null);
        otherInstallmentAmount.setError(null);
        installment.setError(null);
        dayOfMonth.setError(null);
        value.setError(null);

        View focusView = null;
        boolean cancel = focusView != null;

        if (TextUtils.isEmpty(fullName.getText())) {
            fullName.setError(getString(R.string.error_field_required));
            focusView = fullName;
            cancel = true;
        } else if (TextUtils.isEmpty(date.getText())) {
            date.setError(getString(R.string.error_field_required));
            focusView = date;
            cancel = true;
        } else if (TextUtils.isEmpty(loanName.getText())) {
            loanName.setError(getString(R.string.error_field_required));
            focusView = loanName;
            cancel = true;
        } else if (TextUtils.isEmpty(firstInstallmentAmount.getText())) {
            firstInstallmentAmount.setError(getString(R.string.error_field_required));
            focusView = firstInstallmentAmount;
            cancel = true;
        } else if (TextUtils.isEmpty(otherInstallmentAmount.getText())) {
            otherInstallmentAmount.setError(getString(R.string.error_field_required));
            focusView = otherInstallmentAmount;
            cancel = true;
        } else if (TextUtils.isEmpty(installment.getText())) {
            installment.setError(getString(R.string.error_field_required));
            focusView = installment;
            cancel = true;
        } else if (TextUtils.isEmpty(dayOfMonth.getText())) {
            dayOfMonth.setError(getString(R.string.error_field_required));
            focusView = dayOfMonth;
            cancel = true;
        } else if (TextUtils.isEmpty(value.getText())) {
            value.setError(getString(R.string.error_field_required));
            focusView = value;
            cancel = true;
        } else if (Integer.parseInt(dayOfMonth.getText().toString()) > 31) {
            dayOfMonth.setError(getString(R.string.error_field_invalid));
            focusView = dayOfMonth;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            long unpaidSum = DBUtil.sum(this, Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(PayStatus, "0", Oprator.EQUAL, "AND"), new WhereCondition(CashId, cashtEntity.getId().toString(), Oprator.EQUAL));
            long walletSum = DBUtil.sum(this, WalletEntityDao.Properties.Value, WalletEntityDao.TABLENAME, new WhereCondition(WalletEntityDao.Properties.Status, "1", Oprator.EQUAL, "AND"), new WhereCondition(CashId, cashtEntity.getId().toString(), Oprator.EQUAL));
            walletSum -= DBUtil.sum(this, WalletEntityDao.Properties.Value, WalletEntityDao.TABLENAME, new WhereCondition(WalletEntityDao.Properties.Status, "0", Oprator.EQUAL, "AND"), new WhereCondition(WalletEntityDao.Properties.CashId, cashtEntity.getId().toString(), Oprator.EQUAL));
            long cashRemain = walletSum - unpaidSum;

            for (PersonModel personModel : personModels) {
                LoanEntity loanEntity;

                long dif;
                long newValue = removeSeparator(context, value.getText().toString());
                if (loanId == null) {
                    loanEntity = new LoanEntity();
                    loanEntity.setCashId(cashtEntity.getId());
                    dif = newValue;
                } else {
                    loanEntity = loanEntityDao.load(loanId);
                    dif = newValue - loanEntity.getValue();
                }
                if (cashtEntity.getCheckCashRemain() && dif > cashRemain) {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_cash_not_enouf), Snackbar.LENGTH_LONG).show();
                    return;
                }

                Integer oldDayInMonth = loanEntity.getDayInMonth();
                loanEntity.setPersonId(personModel.getId());
                loanEntity.setName(loanName.getText().toString());
                loanEntity.setDayInMonth(Integer.parseInt(dayOfMonth.getText().toString()));
                loanEntity.setInstallment(Integer.parseInt(installment.getText().toString()));
                loanEntity.setValue(newValue);
                loanEntity.setDate(toGregorian(date.getText().toString()));
                loanEntity.setWinDate(toGregorian(winDate.getText().toString()));
                loanEntity.setInstallmentAmount(removeSeparator(context, otherInstallmentAmount.getText().toString()));

                loanEntityDao.save(loanEntity);
                List<Long> debitCreditIds = new ArrayList<>();
                for (int i = 1; i <= loanEntity.getInstallment(); i++) {
                    DebitCreditEntity debitCreditEntity = new DebitCreditEntity();
                    debitCreditEntity.setCashId(cashtEntity.getId());
                    Date installmentDate;
                    boolean duplicateFound = false;
                    if (isNullOrZero(oldDayInMonth)) {
                        installmentDate = set(loanEntity.getDate(), Calendar.DAY_OF_MONTH, loanEntity.getDayInMonth());
                        installmentDate = addMonth(installmentDate, i);
                    } else {
                        installmentDate = set(loanEntity.getDate(), Calendar.DAY_OF_MONTH, oldDayInMonth);
                        installmentDate = addMonth(installmentDate, i);

                        List<DebitCreditEntity> list = debitCreditEntityDao.queryBuilder().where(LoanId.eq(loanEntity.getId()), Date.eq(installmentDate)).list();
                        if (list != null && list.size() > 0) {
                            duplicateFound = true;
                            for (int j = 0; j < list.size(); j++) {
                                DebitCreditEntity creditEntity = list.get(j);
                                creditEntity.setDate(installmentDate);
                                creditEntity.setPersonId(loanEntity.getPersonId());
                                if (i == 1)
                                    creditEntity.setValue(removeSeparator(context, firstInstallmentAmount.getText().toString()));
                                else creditEntity.setValue(loanEntity.getInstallmentAmount());
                                debitCreditEntityDao.save(creditEntity);
                                debitCreditIds.add(creditEntity.getId());
                            }
                        } else duplicateFound = false;
                    }
                    if (!duplicateFound) {
                        debitCreditEntity.setLoanId(loanEntity.getId());
                        debitCreditEntity.setPersonId(loanEntity.getPersonId());
                        debitCreditEntity.setDate(installmentDate);
                        if (i == 1)
                            debitCreditEntity.setValue(removeSeparator(context, firstInstallmentAmount.getText().toString()));
                        else debitCreditEntity.setValue(loanEntity.getInstallmentAmount());
                        debitCreditEntityDao.save(debitCreditEntity);
                        debitCreditIds.add(debitCreditEntity.getId());
                    }
                }
                Long count = debitCreditEntityDao.queryBuilder().where(LoanId.eq(loanEntity.getId())).count();
                int diff = count.intValue() - loanEntity.getInstallment();
                if (diff > 0) {
                    List<DebitCreditEntity> list = debitCreditEntityDao.queryBuilder().where(LoanId.eq(loanEntity.getId()), Id.notIn(debitCreditIds)).orderDesc(Date).limit(diff).offset(0).list();
                    for (DebitCreditEntity debitCreditEntity : list) {
                        debitCreditEntityDao.delete(debitCreditEntity);
                    }

                }
            }
            progressBar.setVisibility(View.GONE);
            finish();
        }
    }

    public void addPerson(View view) {
        FragmentManager fm = getSupportFragmentManager();
        PersonSearchFragment personSearchFragment = PersonSearchFragment.newInstance("#303F9F", cashtEntity.getId());
        personSearchFragment.setCompleteListener(new CompleteListener() {
            @Override
            public void onComplete(Object obj) {
                personModels = (List<PersonModel>) obj;
                if (personModels != null && !personModels.isEmpty()) {
                    StringBuilder builder = new StringBuilder(personModels.get(0).getFullName());
                    if (personModels.size() > 1) {
                        builder.append(" و ");
                        builder.append(personModels.size() - 1);
                        builder.append(" عضو دیگر");
                    }
                    fullName.setText(builder.toString());
                }
            }
        });
        personSearchFragment.show(fm, "fragment_add_person");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (loanId != null) {
            getMenuInflater().inflate(R.menu.delete_bar_view, menu);
            return super.onCreateOptionsMenu(menu);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.appDeleteBar) {
            if (loanId != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.areYouSure));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteLoan(loanId);
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
        }

        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    private void deleteLoan(Long loanId) {
        debitCreditEntityDao.queryBuilder().where(LoanId.eq(loanId)).buildDelete().executeDeleteWithoutDetachingEntities();
        loanEntityDao.deleteByKey(loanId);
    }
}


