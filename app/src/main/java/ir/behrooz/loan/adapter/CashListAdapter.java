package ir.behrooz.loan.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



import java.util.List;

import ir.behrooz.loan.CashActivity;
import ir.behrooz.loan.R;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.entity.CashtEntity;

/**
 * Created by Behrooz Mohamadi on 7/5/2018.
 */

public class CashListAdapter extends RecyclerView.Adapter<CashListAdapter.ViewHolder> {
    private Context context;
    private List<CashtEntity> cashtEntityList;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cash, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final CashtEntity entity = cashtEntityList.get(position);
        holder.name.setText(entity.getName());
        holder.currencyType.setText(entity.getCurrencyType());
        holder.affectNext.setText(makeYesNo(entity.getAffectNext()));
        holder.checkCashRemain.setText(makeYesNo(entity.getCheckCashRemain()));
        holder.withDeposit.setText(entity.getWithDeposit() ? context.getString(R.string.withDeposit) : context.getString(R.string.withoutDeposit));
        holder.notifyDayOfLoan.setText(makeYesNo(entity.getNotifyDayOfLoan()));
        holder.showSettledLoan.setText(makeYesNo(entity.getShowSettledLoan()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CashActivity.class);
                intent.putExtra("cashId", entity.getId());
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CashActivity.class);
                intent.putExtra("cashId", entity.getId());
                context.startActivity(intent);
            }
        });
    }

    private String makeYesNo(boolean value) {
        if (value)
            return context.getString(R.string.yes);
        return context.getString(R.string.no);
    }

    @Override
    public int getItemCount() {
        return cashtEntityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, currencyType, withDeposit, checkCashRemain, affectNext, notifyDayOfLoan, showSettledLoan;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            currencyType = (TextView) view.findViewById(R.id.currencyType);
            withDeposit = (TextView) view.findViewById(R.id.withDeposit);
            checkCashRemain = (TextView) view.findViewById(R.id.checkCashRemain);
            affectNext = (TextView) view.findViewById(R.id.affectNext);
            notifyDayOfLoan = (TextView) view.findViewById(R.id.notifyDayOfLoan);
            showSettledLoan = (TextView) view.findViewById(R.id.showSettledLoan);
            new FontChangeCrawler(view.getContext().getAssets(), Constants.IRANSANS_MD).replaceFonts((ViewGroup) view.getRootView());
        }
    }


    public CashListAdapter(Context context, List<CashtEntity> cashtEntityList) {
        this.context = context;
        this.cashtEntityList = cashtEntityList;
    }
}
