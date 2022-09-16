package ir.behrooz.loan.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ir.behrooz.loan.R;
import ir.behrooz.loan.adapter.PersonSearchListAdapter;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.model.PersonModel;

import static ir.behrooz.loan.entity.PersonEntityDao.Properties.Family;
import static ir.behrooz.loan.entity.PersonEntityDao.Properties.Name;

/**
 * Created by Behrooz Mohamadi on 16/10/28.
 */
public class PersonSearchFragment extends DialogFragment {

    private CompleteListener completeListener;

    public static PersonSearchFragment newInstance(String color,Long cashId) {
        PersonSearchFragment frag = new PersonSearchFragment();
        Bundle args = new Bundle();
        args.putString("color", color);
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
        View rootView = inflater.inflate(R.layout.fragment_person_search, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String color = getArguments().getString("color");
        final ListView listView = view.findViewById(R.id.personList);
        EditText search = view.findViewById(R.id.personSearch);
        Button submitSearch = view.findViewById(R.id.submitSearch);
        Button cancelSearch = view.findViewById(R.id.cancelSearch);
        submitSearch.setTextColor(Color.parseColor(color));

        PersonEntityDao personEntityDao = DBUtil.getReadableInstance(getContext()).getPersonEntityDao();
        final Long cashId = getArguments().getLong("cashId");
        listView.setAdapter(new PersonSearchListAdapter(getContext(), PersonModel.getModels(personEntityDao.queryBuilder().where(PersonEntityDao.Properties.CashId.eq(cashId)).list())));

        new FontChangeCrawler(getContext().getAssets(), Constants.IRANSANS_MD).replaceFonts((ViewGroup) view);
        search.requestFocus();
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                PersonEntityDao personEntityDao1 = DBUtil.getReadableInstance(getContext()).getPersonEntityDao();
                List<PersonEntity> list = personEntityDao1.queryBuilder().where(PersonEntityDao.Properties.CashId.eq(cashId)).whereOr(Name.like("%" + s + "%"), Family.like("%" + s + "%")).list();
                listView.setAdapter(new PersonSearchListAdapter(getContext(), PersonModel.getModels(list)));
            }
        });

        submitSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<PersonModel> checked = new ArrayList<>();
                for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                    PersonModel personModel = (PersonModel) listView.getAdapter().getItem(i);
                    if (personModel.isChecked())
                        checked.add(personModel);
                }
                if (getCompleteListener() != null)
                    getCompleteListener().onComplete(checked);
                dismiss();
            }
        });
        cancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
