package ir.behrooz.loan.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ir.behrooz.loan.R;
import ir.behrooz.loan.adapter.SortListAdapter;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.entity.WalletEntityDao;
import ir.behrooz.loan.model.SortModel;

/**
 * Created by Behrooz Mohamadi on 16/10/28.
 */
public class WalletStatusFragment extends DialogFragment {

    private CompleteListener completeListener;

    public static WalletStatusFragment newInstance() {
        WalletStatusFragment frag = new WalletStatusFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_sort, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView listView = view.findViewById(R.id.sortList);
        TextView sortHeader = view.findViewById(R.id.sortHeader);
        Button cancelSort = view.findViewById(R.id.cancelSort);
        Button ascSort = view.findViewById(R.id.ascSort);
        Button descSort = view.findViewById(R.id.descSort);
        sortHeader.setText(getString(R.string.status));

        List<SortModel> sortModels = new ArrayList<>();
        sortModels.add(new SortModel(getString(R.string.deposit), "1"));
        sortModels.add(new SortModel(getString(R.string.withdrawal), "0"));
        listView.setAdapter(new SortListAdapter(getContext(), sortModels));

        new FontChangeCrawler(getContext().getAssets(), Constants.IRANSANS_MD).replaceFonts((ViewGroup) view);

        sortHeader.setBackgroundColor(Color.parseColor("#81C784"));
        ascSort.setTextColor(Color.parseColor("#81C784"));

        ascSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<SortModel> checked = new ArrayList<>();
                for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                    SortModel sortModel = (SortModel) listView.getAdapter().getItem(i);
                    if (sortModel.isChecked()) {
                        sortModel.setProperty(WalletEntityDao.Properties.Status);
                        checked.add(sortModel);
                    }
                }
                if (getCompleteListener() != null)
                    getCompleteListener().onComplete(checked);
                dismiss();
            }
        });
        descSort.setVisibility(View.GONE);
        ascSort.setText(getString(R.string.select));
        cancelSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
