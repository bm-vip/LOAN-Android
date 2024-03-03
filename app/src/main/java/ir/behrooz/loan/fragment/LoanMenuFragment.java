package ir.behrooz.loan.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.behrooz.loan.DebitCreditListActivity;
import ir.behrooz.loan.R;
import ir.behrooz.loan.adapter.MenuListAdapter;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.model.MenuModel;

/**
 * Created by Behrooz Mohamadi on 16/10/28.
 */
public class LoanMenuFragment extends DialogFragment {
    private CompleteListener completeListener;

    public static LoanMenuFragment newInstance(Long... ids) {
        LoanMenuFragment frag = new LoanMenuFragment();
        Bundle args = new Bundle();
        args.putLong("loanId", ids[0]);
        args.putLong("personId", ids[1]);
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
        View rootView = inflater.inflate(R.layout.fragment_custom_menu, container, false);
        TextView titleLabel = rootView.findViewById(R.id.actionMenuHeader);
        long personId = getArguments().getLong("personId");
        PersonEntity personEntity = DBUtil.getReadableInstance(getContext()).getPersonEntityDao().load(personId);
        titleLabel.setText(String.format("%s %s %s", getString(R.string.actionMenu), personEntity.getName(), personEntity.getFamily()));
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView listView = view.findViewById(R.id.menuList);

        List<MenuModel> menuModels = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();
        map.put("loanId", getArguments().getLong("loanId"));
        menuModels.add(new MenuModel(DebitCreditListActivity.class, map, getString(R.string.installmentList), R.drawable.ic_list_blue_24dp));
        listView.setAdapter(new MenuListAdapter(getContext(), menuModels));

        new FontChangeCrawler(getContext().getAssets(), Constants.IRANSANS_MD).replaceFonts((ViewGroup) view);
    }
}
