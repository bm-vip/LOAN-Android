package ir.behrooz.loan;

import static ir.behrooz.loan.common.DateUtil.addZero;
import static ir.behrooz.loan.common.DateUtil.toGregorian;
import static ir.behrooz.loan.common.DateUtil.truncate;
import static ir.behrooz.loan.common.StringUtil.fixWeakCharacters;
import static ir.behrooz.loan.common.StringUtil.isNullOrEmpty;
import static ir.behrooz.loan.common.StringUtil.moneySeparator;
import static ir.behrooz.loan.common.StringUtil.onChangedEditText;
import static ir.behrooz.loan.common.StringUtil.removeSeparator;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.Date;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.LoanId;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.PayStatus;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.LanguageUtils;
import ir.behrooz.loan.common.Utils;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.DebitCreditEntity;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntity;
import ir.behrooz.loan.entity.LoanEntityDao;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.model.NotificationModel;
import ir.behrooz.materialdatetimepicker.date.DatePickerDialog;
import ir.behrooz.materialdatetimepicker.utils.PersianCalendar;

public class DebitCreditActivity extends BaseActivity {

    private EditText date, value, personFullName, description, loanName;
    private TextView dateLabel, valueLabel, personFullNameLabel, descriptionLabel, loanNameLabel, payStatusGroupLabel;
    private Switch paySwitch;
    private Long debitCreditId;
    public String color = "#00C853";
    private DateListener dateListener;
    CashtEntity cashtEntity;
    PersonEntityDao personEntityDao;
    LoanEntityDao loanEntityDao;
    DebitCreditEntityDao debitCreditEntityDao;

    @Override
    protected String getTableName() {
        return DebitCreditEntityDao.TABLENAME;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debit_credit);

        cashtEntity = new CashtEntity(this);
//        titleBar.setText(getString(R.string.title_activity_person));

        if (getIntent().hasExtra("color")) {
            color = getIntent().getExtras().getString("color");
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(color));
        }

        loanEntityDao = getDaoSession().getLoanEntityDao();
        personEntityDao = getDaoSession().getPersonEntityDao();
        debitCreditEntityDao = getDaoSession().getDebitCreditEntityDao();

//        AppBarLayout appBarLayout = findViewById(R.id.app_bar_debit_credit);
//        appBarLayout.setBackgroundColor(Color.parseColor(color));
        value = findViewById(R.id.value);
        valueLabel = findViewById(R.id.valueLabel);
        valueLabel.setTextColor(Color.parseColor(color));

        personFullName = findViewById(R.id.fullNameValue);
        personFullNameLabel = findViewById(R.id.fullNameLabel);
        personFullNameLabel.setTextColor(Color.parseColor(color));

        payStatusGroupLabel = findViewById(R.id.payStatusGroupLabel);
        payStatusGroupLabel.setTextColor(Color.parseColor(color));
        paySwitch = findViewById(R.id.paySwitch);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{Color.parseColor(color), Color.parseColor(color), Color.GRAY}
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            paySwitch.setThumbTintList(colorStateList);
            paySwitch.setTrackTintList(colorStateList);
        }
        paySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    paySwitch.setText(getString(R.string.done));
                else paySwitch.setText(getString(R.string.not));
            }
        });

        description = findViewById(R.id.description);
        descriptionLabel = findViewById(R.id.descriptionLabel);
        descriptionLabel.setTextColor(Color.parseColor(color));

        loanName = findViewById(R.id.loanName);
        loanNameLabel = findViewById(R.id.loanNameLabel);
        loanNameLabel.setTextColor(Color.parseColor(color));

        date = findViewById(R.id.date);
        dateLabel = findViewById(R.id.dateLabel);
        dateLabel.setTextColor(Color.parseColor(color));

        Button saveDebitCredit = findViewById(R.id.saveDebitCredit);
        saveDebitCredit.setBackgroundColor(Color.parseColor(color));

        saveDebitCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDebitCredit(view);
            }
        });


        dateListener = new DateListener();
        PersianCalendar now = new PersianCalendar();
        final DatePickerDialog dpd = DatePickerDialog.newInstance(
                dateListener,
                now.getPersianYear(),
                now.getPersianMonth(),
                now.getPersianDay()
        );
        dpd.setAccentColor(Color.parseColor(color));
        dpd.vibrate(true);
        dpd.setTitle(getString(R.string.date));

        date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && dpd != null) {
                    dpd.show(getSupportFragmentManager(), "dateDialog");
                    description.requestFocus();
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

        if (getIntent().hasExtra("debitCreditId")) {
            debitCreditId = getIntent().getExtras().getLong("debitCreditId");
            loadForm(debitCreditId);
        }

        description.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveDebitCredit(v);
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag("dateDialog");
        if (dpd != null) dpd.setOnDateSetListener(dateListener);

        if (getIntent().hasExtra("notification_debitCreditId")) {
            String notificationId = getIntent().getExtras().getString("notification_debitCreditId");
            if (!isNullOrEmpty(notificationId)) {
                debitCreditId = Long.parseLong(notificationId);
                loadForm(debitCreditId);
            }
        }
    }
    public static List<NotificationModel> getAllUnpaidInstallment(Context context){
        DebitCreditEntityDao debitCreditEntityDao = DBUtil.getReadableInstance(context).getDebitCreditEntityDao();
        CashtEntity cashtEntity = new CashtEntity(context);
        List<DebitCreditEntity> list = debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.CashId.eq(cashtEntity.getId()), DebitCreditEntityDao.Properties.Date.le(truncate(new Date())), DebitCreditEntityDao.Properties.PayStatus.eq(false)).list();
        if (list != null && list.size() > 0) {
            List<NotificationModel> notificationModelList = new ArrayList<>();
            for (DebitCreditEntity debitCreditEntity : list) {
                PersonEntity personEntity = DBUtil.getReadableInstance(context).getPersonEntityDao().load(debitCreditEntity.getPersonId());
                LoanEntity loanEntity = DBUtil.getReadableInstance(context).getLoanEntityDao().load(debitCreditEntity.getLoanId());

                notificationModelList.add(new NotificationModel(
                        debitCreditEntity.getId(),
                        String.format("%s %s", personEntity.getName(), personEntity.getFamily()),
                        String.format("%s %s %s %s %s",
                                context.getString(R.string.loanPayTime),
                                loanEntity.getName(),
                                context.getString(R.string.withAmount),
                                moneySeparator(context, debitCreditEntity.getValue()),
                                context.getString(R.string.isComming)), debitCreditEntity.getDate()));
            }
            return notificationModelList;
        }
        return null;
    }
    public class DateListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String d = year + "/" + addZero(++monthOfYear) + "/" + addZero(dayOfMonth);
            date.setText(LanguageUtils.getPersianNumbers(fixWeakCharacters(d)));
        }
    }

    private void loadForm(Long id) {
        if (id != null && id > 0) {
            DebitCreditEntity debitCreditEntity = debitCreditEntityDao.load(id);
            value.setText(moneySeparator(context, debitCreditEntity.getValue()));
            date.setText(DateUtil.toPersianString(debitCreditEntity.getDate(), false));
            PersonEntity personEntity = personEntityDao.load(debitCreditEntity.getPersonId());
            personFullName.setText(String.format("%s %s", personEntity.getName(), personEntity.getFamily()));
            description.setText(debitCreditEntity.getDescription());
            LoanEntity loanEntity = loanEntityDao.load(debitCreditEntity.getLoanId());
            loanName.setText(loanEntity.getName());
            paySwitch.setChecked(debitCreditEntity.getPayStatus());
        }
    }

    public void saveDebitCredit(View view) {
        date.setError(null);
        value.setError(null);

        View focusView = null;
        boolean cancel = focusView != null;

        if (TextUtils.isEmpty(date.getText())) {
            date.setError(getString(R.string.error_field_required));
            focusView = date;
            cancel = true;
        } else if (TextUtils.isEmpty(personFullName.getText())) {
            personFullName.setError(getString(R.string.error_field_required));
            focusView = personFullName;
            cancel = true;
        } else if (TextUtils.isEmpty(value.getText())) {
            value.setError(getString(R.string.error_field_required));
            focusView = value;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            DebitCreditEntity entity = debitCreditEntityDao.load(debitCreditId);
            Long newValue = removeSeparator(context, value.getText().toString());
            if (cashtEntity.getAffectNext() && !entity.getValue().equals(newValue)) {
                DebitCreditEntity nextDebitCreditEntity = debitCreditEntityDao.queryBuilder()
                        .where(LoanId.eq(entity.getLoanId()),
                                Date.gt(entity.getDate())).orderAsc(Date).offset(0).limit(1)
                        .unique();
                if (nextDebitCreditEntity != null) {
                    long dif = entity.getValue() - newValue;
                    nextDebitCreditEntity.setValue(nextDebitCreditEntity.getValue() + dif);
                    debitCreditEntityDao.save(nextDebitCreditEntity);
                }
            }

            entity.setPayStatus(paySwitch.isChecked());
            entity.setDescription(description.getText().toString());
            entity.setDate(toGregorian(date.getText().toString()));
            entity.setValue(newValue);
            debitCreditEntityDao.save(entity);
            Long unpaidCount = debitCreditEntityDao.queryBuilder().where(LoanId.eq(entity.getLoanId()), PayStatus.eq(false)).count();
            LoanEntity loanEntity = loanEntityDao.load(entity.getLoanId());
            loanEntity.setSettled(unpaidCount == 0L);
            loanEntityDao.save(loanEntity);
        }
        finish();
    }
}
