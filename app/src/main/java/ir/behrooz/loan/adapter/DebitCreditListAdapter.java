package ir.behrooz.loan.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ir.behrooz.loan.DebitCreditActivity;
import ir.behrooz.loan.R;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.DebitCreditEntity;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntity;
import ir.behrooz.loan.entity.LoanEntityDao;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.PersonEntityDao;

import static ir.behrooz.loan.common.StringUtil.moneySeparator;
import static ir.behrooz.loan.common.Utils.landScape;

/**
 * Created by Behrooz Mohamadi on 7/5/2018.
 */

public class DebitCreditListAdapter extends RecyclerView.Adapter<DebitCreditListAdapter.ViewHolder> {
    private Context context;
    private String color;
    private List<DebitCreditEntity> creditDebitEntities;
    private LoanEntityDao loanEntityDao;
    private PersonEntityDao personEntityDao;
    private DebitCreditEntityDao debitCreditEntityDao;

    public void add(List<DebitCreditEntity> list) {
        creditDebitEntities.addAll(list);
    }

    public void remove(int index) {
        creditDebitEntities.remove(index);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_debit_credit, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final DebitCreditEntity entity = creditDebitEntities.get(position);
        holder.date.setText(DateUtil.toPersianString(entity.getDate(), false));
        PersonEntity personEntity = personEntityDao.load(entity.getPersonId());
        LoanEntity loanEntity = loanEntityDao.load(entity.getLoanId());
        holder.personFullName.setText(personEntity == null ? "---" : String.format("%s %s", personEntity.getName(), personEntity.getFamily()));
        String loanName = loanEntity.getName();
        if (!landScape(context) && loanName.length() > 10)
            loanName = loanName.substring(0, 10);
        holder.loanName.setText(loanName);
        holder.payStatus.setText(entity.getPayStatus() ? context.getString(R.string.done) : context.getString(R.string.not));
        holder.value.setText(moneySeparator(context, entity.getValue()));

//        int color = Color.parseColor("#00C853");
        int layoutColor = Color.parseColor("#ffffff");
        if (!entity.getPayStatus()) {
            layoutColor = Color.parseColor("#CFD8DC");
        }

        holder.linearLayout.setBackgroundColor(layoutColor);
        holder.payStatusLabel.setTextColor(Color.parseColor(color));
        holder.loanNameLabel.setTextColor(Color.parseColor(color));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DebitCreditActivity.class);
                intent.putExtra("debitCreditId", entity.getId());
                intent.putExtra("color", color);
                context.startActivity(intent);
            }
        });
    }

    private void deleteItem(DebitCreditEntity creditDebitEntity, int position) {
        debitCreditEntityDao.delete(creditDebitEntity);
        creditDebitEntities.remove(creditDebitEntity);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return creditDebitEntities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView payStatus, value, date, personFullName, loanName,
                payStatusLabel, loanNameLabel;
        LinearLayout linearLayout;

        public ViewHolder(View view) {
            super(view);
            payStatus = (TextView) view.findViewById(R.id.payStatusValue);
            payStatusLabel = (TextView) view.findViewById(R.id.payStatusLabel);
            value = (TextView) view.findViewById(R.id.valueValue);
            date = (TextView) view.findViewById(R.id.dateValue);
            personFullName = (TextView) view.findViewById(R.id.fullNameValue);
            loanName = (TextView) view.findViewById(R.id.loanValue);
            loanNameLabel = (TextView) view.findViewById(R.id.loanLabel);
            linearLayout = view.findViewById(R.id.debit_credit_layout);
            new FontChangeCrawler(view.getContext().getAssets(), Constants.IRANSANS_MD).replaceFonts((ViewGroup) view.getRootView());
        }
    }


    public DebitCreditListAdapter(Context context, String color, List<DebitCreditEntity> creditDebitEntities) {
        this.context = context;
        this.color = color;
        this.creditDebitEntities = creditDebitEntities;
        this.loanEntityDao = DBUtil.getReadableInstance(context).getLoanEntityDao();
        this.personEntityDao = DBUtil.getReadableInstance(context).getPersonEntityDao();
        this.debitCreditEntityDao = DBUtil.getWritableInstance(context).getDebitCreditEntityDao();
    }
}
