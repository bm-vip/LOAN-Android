package ir.behrooz.loan.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import ir.behrooz.loan.LoanActivity;
import ir.behrooz.loan.R;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.common.sql.Oprator;
import ir.behrooz.loan.common.sql.WhereCondition;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntity;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.common.LanguageUtils;

import static ir.behrooz.loan.common.StringUtil.moneySeparator;
import static ir.behrooz.loan.common.Utils.landScape;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.Date;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.LoanId;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.PayStatus;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.Value;

/**
 * Created by Behrooz Mohamadi on 7/5/2018.
 */

public class LoanListAdapter extends RecyclerView.Adapter<LoanListAdapter.ViewHolder> {
    private Context context;
    private CashtEntity cashtEntity;
    private List<LoanEntity> loanEntities;
    private DebitCreditEntityDao debitCreditEntityDao;
    private PersonEntityDao personEntityDao;
    OnItemClickListner onItemClickListner;
    float factor;

    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }

    public interface OnItemClickListner {
        void onClick(Long... ids);//pass your object types.
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loan, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final LoanEntity entity = loanEntities.get(position);
        PersonEntity personEntity = personEntityDao.load(entity.getPersonId());
        holder.fullName.setText(String.format("%s %s", personEntity.getName(), personEntity.getFamily()));
        String loanName = entity.getName();
        if (!landScape(context) && loanName.length() > 10)
            loanName = loanName.substring(0, 10);
        holder.loanName.setText(loanName);
        holder.receiveDate.setText(DateUtil.toPersianString(entity.getDate(), false));
        holder.dayOfMonth.setText(LanguageUtils.getPersianNumbers(entity.getDayInMonth() + ""));
        holder.installment.setText(LanguageUtils.getPersianNumbers(entity.getInstallment() + ""));
        Long remain = DBUtil.sum(context, Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(LoanId, entity.getId().toString(), Oprator.EQUAL, "AND"), new WhereCondition(PayStatus, "0", Oprator.EQUAL));
        holder.remain.setText(moneySeparator(context, remain));
        holder.settled.setText(entity.getSettled() ? context.getString(R.string.done) : context.getString(R.string.not));
        holder.installmentAmount.setText(moneySeparator(context, entity.getInstallmentAmount()));
        holder.value.setText(moneySeparator(context, entity.getValue()));
        long delayedCount = debitCreditEntityDao.queryBuilder().where(LoanId.eq(entity.getId()), PayStatus.eq(false), Date.lt(new Date())).count();
        holder.delayed.setText(LanguageUtils.getPersianNumbers(delayedCount + ""));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (230 * factor));
        if (cashtEntity.getWithDeposit()) {
            holder.winDateLayout.setVisibility(View.GONE);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (200 * factor));
            holder.receiveDateLabel.setText(context.getString(R.string.receiveDate));
            holder.receiveDate.setHint(context.getString(R.string.receiveDate));
        } else {
            holder.cardView.setLayoutParams(params);
            holder.winDateLayout.setVisibility(View.VISIBLE);
            holder.receiveDateLabel.setText(context.getString(R.string.subscribeDate));
            holder.receiveDate.setHint(context.getString(R.string.subscribeDate));
            holder.winDate.setText(entity.getWinDate() == null ? "---" : DateUtil.toPersianString(entity.getWinDate(), false));
        }
        holder.cardView.setLayoutParams(params);

        int color = Color.parseColor("#ffffff");
        if (entity.getSettled()) {
            color = Color.parseColor("#CFD8DC");
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, LoanActivity.class);
                    intent.putExtra("loanId", entity.getId());
                    context.startActivity(intent);
                }
            });
        }
        holder.linearLayout.setBackgroundColor(color);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onItemClickListner.onClick(entity.getId(), entity.getPersonId());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return loanEntities.size();
    }

    public void add(List<LoanEntity> list) {
        this.loanEntities.addAll(list);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fullName, loanName, dayOfMonth, value, installment, installmentAmount, settled, remain, delayed, receiveDate, receiveDateLabel, winDate;
        LinearLayout linearLayout, winDateLayout;
        CardView cardView;

        public ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.card_view);
            fullName = view.findViewById(R.id.fullNameValue);
            loanName = view.findViewById(R.id.loanNameValue);
            dayOfMonth = view.findViewById(R.id.dayOfMonthValue);
            value = view.findViewById(R.id.valueValue);
            installment = view.findViewById(R.id.installmentValue);
            settled = view.findViewById(R.id.settledValue);
            remain = view.findViewById(R.id.remainValue);
            installmentAmount = view.findViewById(R.id.installmentAmountValue);
            receiveDate = view.findViewById(R.id.receiveDateValue);
            receiveDateLabel = view.findViewById(R.id.receiveDateLabel);
            winDate = view.findViewById(R.id.winDateValue);
            delayed = view.findViewById(R.id.delayedValue);
            linearLayout = view.findViewById(R.id.loan_layout);
            winDateLayout = view.findViewById(R.id.winDateLayout);
            new FontChangeCrawler(view.getContext().getAssets(), Constants.IRANSANS_MD).replaceFonts((ViewGroup) view.getRootView());
        }
    }


    public LoanListAdapter(Context context, List<LoanEntity> eventEntities) {
        this.context = context;
        this.loanEntities = eventEntities;
        this.debitCreditEntityDao = DBUtil.getWritableInstance(context).getDebitCreditEntityDao();
        this.personEntityDao = DBUtil.getWritableInstance(context).getPersonEntityDao();
        this.cashtEntity = new CashtEntity(context);
        factor = context.getResources().getDisplayMetrics().density;
    }
}
