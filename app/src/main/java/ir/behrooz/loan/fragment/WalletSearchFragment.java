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

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import ir.behrooz.materialdatetimepicker.date.DatePickerDialog;
import ir.behrooz.materialdatetimepicker.utils.PersianCalendar;

import java.util.List;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.LanguageUtils;
import ir.behrooz.loan.model.PersonModel;
import ir.behrooz.loan.model.SortModel;

/**
 * Created by Behrooz Mohamadi on 16/10/28.
 */
public class WalletSearchFragment extends DialogFragment {
    private CompleteListener completeListener;
    private EditText fullName, value, date1, date2, description, statusValue;
    private ImageButton fullNameBtn, statusBtn;
    private Button search, cancel;
    private BeginDateListener beginDateListener;
    private EndDateListener endDateListener;
    private List<PersonModel> personModels;
    private List<SortModel> statusModels;

    public static WalletSearchFragment newInstance(Long cashId) {
        WalletSearchFragment frag = new WalletSearchFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_wallet_search, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fullNameBtn = view.findViewById(R.id.fullNameBtn);
        fullName = view.findViewById(R.id.fullNameValue);
        statusBtn = view.findViewById(R.id.statusBtn);
        statusValue = view.findViewById(R.id.statusValue);
        search = view.findViewById(R.id.search);
        cancel = view.findViewById(R.id.cancel);
        date1 = view.findViewById(R.id.date1);
        date2 = view.findViewById(R.id.date2);
        value = view.findViewById(R.id.value);
        description = view.findViewById(R.id.description);
        final Long cashId = getArguments().getLong("cashId");

        fullNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                PersonSearchFragment personSearchFragment = PersonSearchFragment.newInstance("#81C784", cashId);
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
        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                WalletStatusFragment statusFragment = WalletStatusFragment.newInstance();
                statusFragment.setCompleteListener(new CompleteListener() {
                    @Override
                    public void onComplete(Object obj) {
                        statusModels = (List<SortModel>) obj;
                        if (statusModels != null && !statusModels.isEmpty()) {
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < statusModels.size(); i++) {
                                SortModel statusModel = statusModels.get(i);
                                builder.append(statusModel.getTitle());
                                if (i < statusModels.size() - 1)
                                    builder.append(" و ");
                            }
                            statusValue.setText(builder.toString());
                        }
                    }
                });
                statusFragment.show(fm, "fragment_select_status");
            }
        });

        PersianCalendar now = new PersianCalendar();
        // init beginDate
        beginDateListener = new BeginDateListener();
        final DatePickerDialog dpd1 = DatePickerDialog.newInstance(
                beginDateListener,
                now.getPersianYear(),
                now.getPersianMonth(),
                now.getPersianDay()
        );
        dpd1.vibrate(true);
        dpd1.setTitle(getString(R.string.beginDate));
        date1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && dpd1 != null) {
                    dpd1.show(getFragmentManager(), "dateDialog");
//                    date2.requestFocus();
                }
            }
        });

        // init endDate
        endDateListener = new EndDateListener();
        final DatePickerDialog dpd2 = DatePickerDialog.newInstance(
                endDateListener,
                now.getPersianYear(),
                now.getPersianMonth(),
                now.getPersianDay()
        );
        dpd2.vibrate(true);
        dpd2.setTitle(getString(R.string.endDate));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dpd1.setAccentColor(getContext().getColor(R.color.md_green_300));
            dpd2.setAccentColor(getContext().getColor(R.color.md_green_300));
        }
        date2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && dpd2 != null) {
                    dpd2.show(getFragmentManager(), "dateDialog");
//                    description.requestFocus();
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

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder builder = new StringBuilder();
                if (personModels != null && personModels.size() > 0)
                    builder.append(" AND W.PERSON_ID in (").append(PersonModel.getIds(personModels)).append(")");
                if (statusModels != null && statusModels.size() > 0)
                    builder.append(" AND W.STATUS in (").append(SortModel.getIds(statusModels)).append(")");
                if (!TextUtils.isEmpty(value.getText()))
                    builder.append(" AND W.VALUE = ").append(removeSeparator(getContext(), value.getText().toString()));
                if (!TextUtils.isEmpty(date1.getText()) && TextUtils.isEmpty(date2.getText()))
                    builder.append(" AND W.DATE = ").append(DateUtil.toGregorian(date1.getText().toString()).getTime());
                if (!TextUtils.isEmpty(date1.getText()) && !TextUtils.isEmpty(date2.getText()))
                    builder.append(" AND W.DATE >= ".concat(DateUtil.toGregorian(date1.getText().toString()).getTime() + " AND W.DATE <= ")).append(DateUtil.toGregorian(date2.getText().toString()).getTime());
                if (!TextUtils.isEmpty(description.getText()))
                    builder.append(" AND W.DESCRIPTION LIKE '%").append(description.getText().toString()).append("%'");


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

    public class BeginDateListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String d = year + "/" + addZero(++monthOfYear) + "/" + addZero(dayOfMonth);
            date1.setText(LanguageUtils.getPersianNumbers(fixWeakCharacters(d)));
        }
    }

    public class EndDateListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String d = year + "/" + addZero(++monthOfYear) + "/" + addZero(dayOfMonth);
            date2.setText(LanguageUtils.getPersianNumbers(fixWeakCharacters(d)));
        }
    }
}
