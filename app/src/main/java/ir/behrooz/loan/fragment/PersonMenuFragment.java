package ir.behrooz.loan.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.behrooz.loan.DebitCreditListActivity;
import ir.behrooz.loan.LoanActivity;
import ir.behrooz.loan.LoanListActivity;
import ir.behrooz.loan.R;
import ir.behrooz.loan.WalletActivity;
import ir.behrooz.loan.WalletListActivity;
import ir.behrooz.loan.adapter.MenuListAdapter;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.sql.And;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.common.sql.Operator;
import ir.behrooz.loan.common.sql.WhereCondition;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.DebitCreditEntity;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntity;
import ir.behrooz.loan.entity.LoanEntityDao;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.WalletEntityDao;
import ir.behrooz.loan.model.MenuModel;

import static ir.behrooz.loan.common.StringUtil.fixWeakCharacters;
import static ir.behrooz.loan.common.StringUtil.moneySeparator;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.PayStatus;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.PersonId;
import static ir.behrooz.loan.entity.WalletEntityDao.Properties.Value;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by Behrooz Mohamadi on 16/10/28.
 */
public class PersonMenuFragment extends DialogFragment {
    private CompleteListener completeListener;
    private CashtEntity cashtEntity;

    public static PersonMenuFragment newInstance(Long personId) {
        PersonMenuFragment frag = new PersonMenuFragment();
        Bundle args = new Bundle();
        args.putLong("personId", personId);
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
        this.cashtEntity = new CashtEntity(getContext());

        List<MenuModel> menuModels = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();
        map.put("personId", getArguments().getLong("personId"));
        if (cashtEntity.getWithDeposit())
            menuModels.add(new MenuModel(WalletActivity.class, map, getString(R.string.title_activity_wallet), R.drawable.ic_add_circle_outline_blue_24dp));

        menuModels.add(new MenuModel(LoanActivity.class, map, getString(R.string.title_activity_loan), R.drawable.ic_add_circle_outline_blue_24dp));

        if (cashtEntity.getWithDeposit())
            menuModels.add(new MenuModel(WalletListActivity.class, map, getString(R.string.title_activity_wallet_list), R.drawable.ic_list_blue_24dp));

        menuModels.add(new MenuModel(LoanListActivity.class, map, getString(R.string.title_activity_loan_list), R.drawable.ic_list_blue_24dp));
        menuModels.add(new MenuModel(DebitCreditListActivity.class, map, getString(R.string.installmentList), R.drawable.ic_list_blue_24dp));
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.report));
        i.putExtra(android.content.Intent.EXTRA_TEXT, createReport(view.getContext(), getArguments().getLong("personId")));
        menuModels.add(new MenuModel(i, getString(R.string.send), R.drawable.ic_menu_send_blue));
        listView.setAdapter(new MenuListAdapter(getContext(), menuModels));

        new FontChangeCrawler(getContext().getAssets(), Constants.IRANSANS_MD).replaceFonts((ViewGroup) view);
    }

    public static String createReport(Context ctx, Long personId) {
        CashtEntity cashtEntity = new CashtEntity(ctx);
        PersonEntity personEntity = DBUtil.getReadableInstance(ctx).getPersonEntityDao().load(personId);
        StringBuilder builder = new StringBuilder(cashtEntity.getName());
        builder.append("\n───────────────\n");
        builder.append(String.format("%s %s\n", personEntity.getName(), personEntity.getFamily()));
        builder.append(String.format("%s: %s", ctx.getString(R.string.subscribCode), personEntity.getNationalCode()));
        builder.append("\n───────────────\n");
        Long wallet = 0L;
        if (cashtEntity.getWithDeposit()) {
            wallet = DBUtil.sum(ctx, Value, WalletEntityDao.TABLENAME, new And(PersonId, personId.toString()), new WhereCondition(WalletEntityDao.Properties.Status, "1"));
            wallet -= DBUtil.sum(ctx, Value, WalletEntityDao.TABLENAME, new And(PersonId, personId.toString()), new WhereCondition(WalletEntityDao.Properties.Status, "0"));
        } else
            wallet = DBUtil.sum(ctx, Value, DebitCreditEntityDao.TABLENAME, new And(PersonId, personId.toString()), new WhereCondition(PayStatus, "1"));
        builder.append(String.format("%s: %s\n", ctx.getString(R.string.wallet), moneySeparator(ctx, wallet)));
        Long remain = DBUtil.sum(ctx, DebitCreditEntityDao.Properties.Value, DebitCreditEntityDao.TABLENAME, new And(DebitCreditEntityDao.Properties.PersonId, personId.toString()), new WhereCondition(DebitCreditEntityDao.Properties.PayStatus, "0"));
        builder.append(String.format("%s: %s\n", ctx.getString(R.string.remainLoan), moneySeparator(ctx, remain)));
        DebitCreditEntityDao debitCreditEntityDao = DBUtil.getReadableInstance(ctx).getDebitCreditEntityDao();
        long delayedCount = debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.PersonId.eq(personId), DebitCreditEntityDao.Properties.PayStatus.eq(false), DebitCreditEntityDao.Properties.Date.lt(new Date())).count();
        builder.append(String.format("%s: %d\n", ctx.getString(R.string.delayed), delayedCount));
        builder.append(ctx.getString(R.string.lastThree));
        List<DebitCreditEntity> debitCreditEntityList = debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.PersonId.eq(personId), DebitCreditEntityDao.Properties.PayStatus.eq(true)).orderDesc(DebitCreditEntityDao.Properties.Id).limit(3).offset(0).list();
        for (DebitCreditEntity debitCreditEntity : debitCreditEntityList) {
            builder.append(fixWeakCharacters(String.format("\n%s - %s", moneySeparator(ctx, debitCreditEntity.getValue()), DateUtil.toPersianString(debitCreditEntity.getDate(), false))));
            builder.append("\n-------------------");
        }
        builder.append("\n───────────────\n");
        LoanEntity lastLoanEntity = DBUtil.getReadableInstance(ctx).getLoanEntityDao().queryBuilder().where(LoanEntityDao.Properties.PersonId.eq(personId)).orderDesc(LoanEntityDao.Properties.Id).limit(1).offset(0).unique();
        if (lastLoanEntity != null) {
            builder.append(ctx.getString(R.string.lastLoan));
            builder.append(String.format("\n%s: %s\n", ctx.getString(R.string.amount), moneySeparator(ctx, lastLoanEntity.getValue())));
            builder.append(String.format("%s: %s\n", ctx.getString(R.string.receiveDate), DateUtil.toPersianString(lastLoanEntity.getDate(), false)));
            Long loanRemain = DBUtil.sum(ctx, DebitCreditEntityDao.Properties.Value, DebitCreditEntityDao.TABLENAME, new And(DebitCreditEntityDao.Properties.LoanId, lastLoanEntity.getId().toString()), new WhereCondition(DebitCreditEntityDao.Properties.PayStatus, "0"));
            builder.append(String.format("%s: %s\n", ctx.getString(R.string.remainLoan), moneySeparator(ctx, loanRemain)));
            long paidCount = debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.LoanId.eq(lastLoanEntity.getId()), DebitCreditEntityDao.Properties.PayStatus.eq(true)).count();
            builder.append(String.format("%s: %s\n", ctx.getString(R.string.installmentPaid), String.format("%d از %d", paidCount, lastLoanEntity.getInstallment())));
            DebitCreditEntity firstUnpaid = debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.LoanId.eq(lastLoanEntity.getId()), DebitCreditEntityDao.Properties.PayStatus.eq(false)).orderAsc(DebitCreditEntityDao.Properties.Date).limit(1).offset(0).unique();
            if (firstUnpaid != null)
                builder.append(String.format("%s: %s\n", ctx.getString(R.string.nextDayOfMonth), DateUtil.toPersianString(firstUnpaid.getDate(), false)));
            DebitCreditEntity lastUnpaid = debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.LoanId.eq(lastLoanEntity.getId()), DebitCreditEntityDao.Properties.PayStatus.eq(false)).orderDesc(DebitCreditEntityDao.Properties.Date).limit(1).offset(0).unique();
            if (lastUnpaid != null)
                builder.append(String.format("%s: %s\n", ctx.getString(R.string.lastInstallment), DateUtil.toPersianString(lastUnpaid.getDate(), false)));
            long loanDelayedCount = debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.LoanId.eq(lastLoanEntity.getId()), DebitCreditEntityDao.Properties.PayStatus.eq(false), DebitCreditEntityDao.Properties.Date.lt(new Date())).count();
            builder.append(String.format("%s: %d", ctx.getString(R.string.delayed), loanDelayedCount));
        }
        return builder.toString();
    }
}
