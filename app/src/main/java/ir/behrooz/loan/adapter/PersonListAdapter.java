package ir.behrooz.loan.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import ir.behrooz.loan.PersonActivity;
import ir.behrooz.loan.R;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.sql.And;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.common.sql.Operator;
import ir.behrooz.loan.common.sql.WhereCondition;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.entity.WalletEntityDao;
import ir.behrooz.loan.common.LanguageUtils;

import static ir.behrooz.loan.common.StringUtil.moneySeparator;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.Date;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.PayStatus;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.PersonId;
import static ir.behrooz.loan.entity.WalletEntityDao.Properties.Value;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Behrooz Mohamadi on 7/5/2018.
 */

public class PersonListAdapter extends RecyclerView.Adapter<PersonListAdapter.ViewHolder> {
    private Context context;
    private List<PersonEntity> personEntities;
    private PersonEntityDao personEntityDao;
    private DebitCreditEntityDao debitCreditEntityDao;
    private CashtEntity cashtEntity;
    OnItemClickListner onItemClickListner;

    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }

    public interface OnItemClickListner {
        void onClick(Long id);//pass your object types.
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_person, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final PersonEntity entity = personEntities.get(position);
        holder.fullName.setText(String.format("%s %s", entity.getName(), entity.getFamily()));
        holder.phone.setText(LanguageUtils.getPersianNumbers(entity.getPhone()));
        Long wallet = 0L;
        if (cashtEntity.getWithDeposit()) {
            wallet = DBUtil.sum(context, Value, WalletEntityDao.TABLENAME, new WhereCondition(PersonId, entity.getId().toString()), new And(WalletEntityDao.Properties.Status, "1"));
            wallet -= DBUtil.sum(context, Value, WalletEntityDao.TABLENAME, new WhereCondition(PersonId, entity.getId().toString()), new And(WalletEntityDao.Properties.Status, "0"));
        }
        else
            wallet = DBUtil.sum(context, Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(PersonId, entity.getId().toString()), new And(PayStatus, "1"));
        holder.wallet.setText(moneySeparator(context, wallet));
        long delayedCount = debitCreditEntityDao.queryBuilder().where(PersonId.eq(entity.getId()), PayStatus.eq(false), Date.lt(new Date())).count();
        holder.delayed.setText(LanguageUtils.getPersianNumbers(delayedCount + ""));

//        holder.editPerson.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(context, PersonActivity.class);
//                intent.putExtra("personId", entity.getId());
//                context.startActivity(intent);
//            }
//        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PersonActivity.class);
                intent.putExtra("personId", entity.getId());
                context.startActivity(intent);
            }
        });
//        holder.deletePerson.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle(context.getString(R.string.areYouSure));
//                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        deleteItem(entity, position);
//                        dialog.dismiss();
//                    }
//                });
//                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog dialog = builder.create();
//                dialog.show();
//                ViewGroup viewGroup = (ViewGroup) dialog.findViewById(android.R.id.content);
//                new FontChangeCrawler(context.getAssets(), IRANSANS_LT).replaceFonts(viewGroup);
//            }
//        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onItemClickListner.onClick(entity.getId());
                return true;
            }
        });
    }

//    private void deleteItem(PersonEntity personEntity, int position) {
//        loanEntityDao.queryBuilder().where(LoanEntityDao.Properties.PersonId.eq(personEntity.getId())).buildDelete().executeDeleteWithoutDetachingEntities();
//        walletEntityDao.queryBuilder().where(WalletEntityDao.Properties.PersonId.eq(personEntity.getId())).buildDelete().executeDeleteWithoutDetachingEntities();
//        debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.PersonId.eq(personEntity.getId())).buildDelete().executeDeleteWithoutDetachingEntities();
//        personEntityDao.delete(personEntity);
//        personEntities.remove(personEntity);
//        notifyItemRemoved(position);
//    }

    @Override
    public int getItemCount() {
        return personEntities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fullName, phone, wallet, delayed;
//        TextViewPlus editPerson, deletePerson;

        public ViewHolder(View view) {
            super(view);
            fullName = (TextView) view.findViewById(R.id.fullNameValue);
            phone = (TextView) view.findViewById(R.id.phoneValue);
            wallet = (TextView) view.findViewById(R.id.walletValue);
            delayed = (TextView) view.findViewById(R.id.delayValue);
//            editPerson = (TextViewPlus) view.findViewById(R.id.editEvent);
//            deletePerson = (TextViewPlus) view.findViewById(R.id.deleteEvent);
            new FontChangeCrawler(view.getContext().getAssets(), Constants.IRANSANS_MD).replaceFonts((ViewGroup) view.getRootView());
        }
    }


    public PersonListAdapter(Context context, List<PersonEntity> eventEntities) {
        this.context = context;
        this.personEntities = eventEntities;
        this.personEntityDao = DBUtil.getWritableInstance(context).getPersonEntityDao();
        this.debitCreditEntityDao = DBUtil.getWritableInstance(context).getDebitCreditEntityDao();
        this.cashtEntity = new CashtEntity(context);
    }
}
