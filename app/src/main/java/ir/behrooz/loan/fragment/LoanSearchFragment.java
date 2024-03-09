package ir.behrooz.loan.fragment;

import static ir.behrooz.loan.common.DateUtil.addZero;
import static ir.behrooz.loan.common.StringUtil.fixWeakCharacters;
import static ir.behrooz.loan.common.StringUtil.onChangedEditText;
import static ir.behrooz.loan.common.StringUtil.removeSeparator;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import ir.behrooz.materialdatetimepicker.date.DatePickerDialog;
import ir.behrooz.materialdatetimepicker.utils.PersianCalendar;

import java.util.Date;
import java.util.List;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.LanguageUtils;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.model.PersonModel;

/**
 * Created by Behrooz Mohamadi on 16/10/28.
 */
public class LoanSearchFragment extends DialogFragment {
    private CompleteListener completeListener;
    private EditText fullName, loanName, dayOfMonth, value, installment, installmentAmount, date1, date2, delayed, remain;
    private ImageButton fullNameBtn;
    private Button search, cancel;
    private DateListener1 dateListener1;
    private DateListener2 dateListener2;
    private List<PersonModel> personModels;

    public static LoanSearchFragment newInstance(Long cashId) {
        LoanSearchFragment frag = new LoanSearchFragment();
        Bundle args = new Bundle();
        args.putLong("cashId", cashId);
        frag.setArguments(args);
        return frag;
    }

    public CompleteListener getCompleteListener() {
        return completeListener;
    }

    public void setCompleteListener(CompleteListener completeListener) {
        this.completeListener = completeListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_loan_search, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fullNameBtn = view.findViewById(R.id.fullNameBtn);
        fullName = view.findViewById(R.id.fullNameValue);
        search = view.findViewById(R.id.search);
        cancel = view.findViewById(R.id.cancel);
        date1 = view.findViewById(R.id.date1);
        date2 = view.findViewById(R.id.date2);
        loanName = view.findViewById(R.id.loanName);
        dayOfMonth = view.findViewById(R.id.dayOfMonth);
        value = view.findViewById(R.id.value);
        installment = view.findViewById(R.id.installment);
        installmentAmount = view.findViewById(R.id.installmentAmount);
        delayed = view.findViewById(R.id.delayed);
        remain = view.findViewById(R.id.remain);
        final Long cashId = getArguments().getLong("cashId");

        fullNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                PersonSearchFragment personSearchFragment = PersonSearchFragment.newInstance("#303F9F", cashId);
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
        });
        dateListener1 = new DateListener1();
        dateListener2 = new DateListener2();
        PersianCalendar now = new PersianCalendar();
        final DatePickerDialog dpd1 = DatePickerDialog.newInstance(
                dateListener1,
                now.getPersianYear(),
                now.getPersianMonth(),
                now.getPersianDay()
        );
        final DatePickerDialog dpd2 = DatePickerDialog.newInstance(
                dateListener2,
                now.getPersianYear(),
                now.getPersianMonth(),
                now.getPersianDay()
        );
        dpd1.vibrate(true);
        dpd1.setTitle(getString(R.string.receiveDate1));
        dpd2.vibrate(true);
        dpd2.setTitle(getString(R.string.receiveDate2));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dpd1.setAccentColor(getContext().getColor(R.color.colorPrimaryDark));
            dpd2.setAccentColor(getContext().getColor(R.color.colorPrimaryDark));
        }
        date1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && dpd1 != null) {
                    dpd1.show(getFragmentManager(), "dateDialog1");
                }
            }
        });
        date2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && dpd2 != null) {
                    dpd2.show(getFragmentManager(), "dateDialog2");
                }
            }
        });

        value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onChangedEditText(getContext(), this, value, charSequence, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        installmentAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onChangedEditText(getContext(), this, installmentAmount, charSequence, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        remain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onChangedEditText(getContext(), this, remain, charSequence, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder builder = new StringBuilder();
                if (personModels != null && personModels.size() > 0)
                    builder.append(" AND L.PERSON_ID in (".concat(PersonModel.getIds(personModels)).concat(")"));
                if (!TextUtils.isEmpty(loanName.getText()))
                    builder.append(" AND L.NAME LIKE '%".concat(loanName.getText().toString()).concat("%'"));
                if (!TextUtils.isEmpty(dayOfMonth.getText()))
                    builder.append(" AND L.DAY_IN_MONTH = ".concat(dayOfMonth.getText().toString()));
                if (!TextUtils.isEmpty(value.getText()))
                    builder.append(" AND L.VALUE = ".concat(removeSeparator(getContext(), value.getText().toString()) + ""));
                if (!TextUtils.isEmpty(installment.getText()))
                    builder.append(" AND L.INSTALLMENT = ".concat(installment.getText().toString()));
                if (!TextUtils.isEmpty(installmentAmount.getText()))
                    builder.append(" AND L.INSTALLMENT_AMOUNT = ".concat(removeSeparator(getContext(), installmentAmount.getText().toString()) + ""));
                if (!TextUtils.isEmpty(date1.getText()) && TextUtils.isEmpty(date2.getText()))
                    builder.append(" AND L.DATE = ").append(DateUtil.toGregorian(date1.getText().toString()).getTime());
                if (!TextUtils.isEmpty(date1.getText()) && !TextUtils.isEmpty(date2.getText()))
                    builder.append(" AND L.DATE >= ".concat(DateUtil.toGregorian(date1.getText().toString()).getTime() + " AND L.DATE <= ")).append(DateUtil.toGregorian(date2.getText().toString()).getTime());
                if (!TextUtils.isEmpty(delayed.getText()))
                    builder.append(" AND ".concat(delayed.getText().toString()).concat("=(select count(*) from ").concat(DebitCreditEntityDao.TABLENAME).concat(" DC where DC.LOAN_ID=L._id AND PAY_STATUS=0 AND DATE <").concat(new Date().getTime() + ")"));
                if (!TextUtils.isEmpty(remain.getText()))
                    builder.append(" AND ".concat(removeSeparator(getContext(), remain.getText().toString()) + "=").concat("(select sum(DC.VALUE) from ").concat(DebitCreditEntityDao.TABLENAME).concat(" DC where DC.LOAN_ID=L._id AND PAY_STATUS = 0)"));

                if (getCompleteListener() != null)
                    getCompleteListener().onComplete(builder.toString());
                dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        new FontChangeCrawler(getContext().getAssets(), Constants.IRANSANS_MD).replaceFonts((ViewGroup) view);
    }

    public class DateListener1 implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String d = year + "/" + addZero(++monthOfYear) + "/" + addZero(dayOfMonth);
            date1.setText(LanguageUtils.getPersianNumbers(fixWeakCharacters(d)));
        }
    }
    public class DateListener2 implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String d = year + "/" + addZero(++monthOfYear) + "/" + addZero(dayOfMonth);
            date2.setText(LanguageUtils.getPersianNumbers(fixWeakCharacters(d)));
        }
    }
}
