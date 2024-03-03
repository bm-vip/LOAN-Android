package ir.behrooz.loan.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ir.behrooz.loan.R;
import ir.behrooz.loan.WalletActivity;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.entity.WalletEntity;
import ir.behrooz.loan.entity.WalletEntityDao;

import static ir.behrooz.loan.common.DateUtil.toPersianString;
import static ir.behrooz.loan.common.StringUtil.moneySeparator;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Behrooz Mohamadi on 7/5/2018.
 */

public class WalletListAdapter extends RecyclerView.Adapter<WalletListAdapter.ViewHolder> {
    private Context context;
    private List<WalletEntity> walletEntities;
    private WalletEntityDao walletEntityDao;
    private PersonEntityDao personEntityDao;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_wallet, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final WalletEntity entity = walletEntities.get(position);
        holder.date.setText(toPersianString(entity.getDate(), false));
        PersonEntity personEntity = personEntityDao.load(entity.getPersonId());
        holder.personName.setText(personEntity == null ? "---" : String.format("%s %s", personEntity.getName(), personEntity.getFamily()));
        holder.value.setText(moneySeparator(context, entity.getValue()));
        holder.status.setText(entity.getStatus() ? context.getString(R.string.deposit) : context.getString(R.string.withdrawal));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WalletActivity.class);
                intent.putExtra("walletId", entity.getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return walletEntities.size();
    }

    public void add(List<WalletEntity> list) {
        this.walletEntities.addAll(list);
    }

    public void remove(int index) {
        this.walletEntities.remove(index);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView personName, value, date, status;

        public ViewHolder(View view) {
            super(view);
            personName = view.findViewById(R.id.fullNameValue);
            value = view.findViewById(R.id.valueValue);
            date = view.findViewById(R.id.dateValue);
            status = view.findViewById(R.id.statusValue);
            new FontChangeCrawler(view.getContext().getAssets(), Constants.IRANSANS_MD).replaceFonts((ViewGroup) view.getRootView());
        }
    }


    public WalletListAdapter(Context context, List<WalletEntity> entityList) {
        this.context = context;
        this.walletEntities = entityList;
        this.walletEntityDao = DBUtil.getWritableInstance(context).getWalletEntityDao();
        this.personEntityDao = DBUtil.getWritableInstance(context).getPersonEntityDao();
    }
}
