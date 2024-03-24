package ir.behrooz.loan;

import static ir.behrooz.loan.common.sql.DBUtil.orderBy;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ir.behrooz.loan.adapter.DebitCreditListAdapter;
import ir.behrooz.loan.adapter.pdf.PdfDocumentAdapter;
import ir.behrooz.loan.adapter.pdf.PrintJobMonitorService;
import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.Utils;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.DebitCreditEntity;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntity;
import ir.behrooz.loan.entity.LoanEntityDao;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.fragment.DebitCreditSearchFragment;
import ir.behrooz.loan.fragment.DebitCreditSortFragment;
import ir.behrooz.loan.model.SortModel;
import ir.behrooz.loan.report.DebitCreditListPDF;

public class DebitCreditListActivity extends BaseActivity {
    public RecyclerView recyclerView;
    public DebitCreditListAdapter adapter;
    private DebitCreditEntityDao debitCreditEntityDao;
    private LoanEntityDao loanEntityDao;
    private PersonEntityDao personEntityDao;
    boolean isLoading = false;
    Long personId, loanId;
    String search = "";
    public ProgressBar progressBar;
    public Boolean payStatus;
    public String color = "#2E7D32";
    private PrintManager mgr = null;
    private List<SortModel> sortModels;
    CashtEntity cashtEntity;

    @Override
    protected String getTableName() {
        return DebitCreditEntityDao.TABLENAME;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debit_credit_list);

        debitCreditEntityDao = getDaoSession().getDebitCreditEntityDao();
        loanEntityDao = getDaoSession().getLoanEntityDao();
        personEntityDao = getDaoSession().getPersonEntityDao();

        cashtEntity = new CashtEntity(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mgr = (PrintManager) getSystemService(PRINT_SERVICE);
        }

        if (getIntent().hasExtra("color")) {
            color = getIntent().getExtras().getString("color");
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));

        if (getIntent().hasExtra("paid")) {
            payStatus = getIntent().getExtras().getBoolean("paid");
        }
        if (getIntent().hasExtra("personId")) {
            personId = getIntent().getExtras().getLong("personId");
            PersonEntity personEntity = personEntityDao.load(personId);
            getSupportActionBar().setTitle(String.format("%s %s %s", getString(R.string.installmentList), personEntity.getName(), personEntity.getFamily()));
        }
        if (getIntent().hasExtra("loanId")) {
            loanId = getIntent().getExtras().getLong("loanId");
            LoanEntity loanEntity = loanEntityDao.load(loanId);
            PersonEntity personEntity = personEntityDao.load(loanEntity.getPersonId());
            getSupportActionBar().setTitle(String.format("%s %s %s", getString(R.string.installmentList), personEntity.getName(), personEntity.getFamily()));
        }

        recyclerView = (RecyclerView) findViewById(R.id.creditDebit_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progressBar = findViewById(R.id.creditDebit_progress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor(color), PorterDuff.Mode.MULTIPLY);
            getWindow().setStatusBarColor(Color.parseColor(color));
        }

        sortModels = new ArrayList<>();
        sortModels.add(new SortModel("", "DC.LOAN_ID"));
        sortModels.add(new SortModel("", "DC.DATE"));
        initScrollListener();
        Utils.loadNotificationPermission(this);
        Utils.showNotification(context, DebitCreditActivity.getAllUnpaidInstallment(context));
    }


    @Override
    protected void onResume() {
        super.onResume();
        adapter = new DebitCreditListAdapter(this, color, search(10, 0));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        if (item.getItemId() == R.id.appDeleteBar) {
            DebitCreditSortFragment debitCreditSortFragment = DebitCreditSortFragment.newInstance(color);
            debitCreditSortFragment.setCompleteListener(new CompleteListener() {
                @Override
                public void onComplete(Object obj) {
                    sortModels = (List<SortModel>) obj;
                    adapter = new DebitCreditListAdapter(context, color, search(10, 0));
                    recyclerView.setAdapter(adapter);
                }
            });
            debitCreditSortFragment.show(fm, "fragment_sort");
            return true;
        } else if (item.getItemId() == R.id.appSearchBar) {
            DebitCreditSearchFragment debitCreditSearchFragment = DebitCreditSearchFragment.newInstance(color, cashtEntity.getId());
            debitCreditSearchFragment.setCompleteListener(new CompleteListener() {
                @Override
                public void onComplete(Object obj) {
                    search = (String) obj;
                    adapter = new DebitCreditListAdapter(context, color, search(10, 0));
                    recyclerView.setAdapter(adapter);
                }
            });
            debitCreditSearchFragment.show(fm, "fragment_search");
            return true;
        } else if (item.getItemId() == R.id.appPrintBar) {

            String fileName = "Installment_".concat(DateUtil.toPersianWithTimeString(new Date())).concat(".pdf");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                print(fileName,
                        new PdfDocumentAdapter(context, fileName),
                        new PrintAttributes.Builder().build());
            }
            return true;
        } else if (item.getItemId() == R.id.appHelpBar) {

            Intent intent = new Intent(this, DebitCreditHelpActivity.class);
            intent.putExtra("color", color);
            startActivity(intent);
            return true;
        }

        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private PrintJob print(String name, PrintDocumentAdapter adapter,
                           PrintAttributes attrs) {
        DebitCreditListPDF debitCreditListPDF = new DebitCreditListPDF(context);
        debitCreditListPDF.createPdf(name, search(1000, 0));
        startService(new Intent(this, PrintJobMonitorService.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return (mgr.print(name, adapter, attrs));
        }
        return null;
    }

    public List<DebitCreditEntity> search(int limit, int offset) {
        if (debitCreditEntityDao.count() == offset)
            return Collections.emptyList();
        StringBuilder sql = new StringBuilder("SELECT DC.* FROM DebitCredit DC INNER JOIN Person P ON P._id=DC.PERSON_ID INNER JOIN Loan L on L._id=DC.LOAN_ID ");
        sql.append("WHERE DC.CASH_ID=").append(cashtEntity.getId().toString());
        sql.append(search);
        if (payStatus != null)
            sql.append(" AND DC.PAY_STATUS = " + (payStatus ? "1" : "0"));
        if (personId != null)
            sql.append(" AND DC.PERSON_ID = " + personId);
        if (loanId != null)
            sql.append(" AND DC.LOAN_ID = " + loanId);
        sql.append(" order by ");
        sql.append(orderBy(sortModels));
        sql.append(String.format(Locale.US, " LIMIT %d OFFSET %d;", limit, offset));
        Cursor cursor = getDaoSession().getDatabase().rawQuery(sql.toString(), new String[]{});
        List<DebitCreditEntity> debitCreditEntities = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                debitCreditEntities.add(debitCreditEntityDao.readEntity(cursor, 0));
                cursor.moveToNext();
            }
        }

        return debitCreditEntities;
    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1) {
                        loadMore();
                    }
                }
            }
        });
    }

    private void loadMore() {
        if (debitCreditEntityDao.count() != adapter.getItemCount()) {
            adapter.notifyItemInserted(adapter.getItemCount());
            isLoading = true;
            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int scrollPosition = adapter.getItemCount();
                    adapter.add(search(10, scrollPosition));
                    adapter.notifyDataSetChanged();
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                }
            }, 2000);
        }
    }
}