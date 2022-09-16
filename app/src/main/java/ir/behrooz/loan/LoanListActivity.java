package ir.behrooz.loan;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ir.behrooz.loan.adapter.LoanListAdapter;
import ir.behrooz.loan.adapter.pdf.PdfDocumentAdapter;
import ir.behrooz.loan.adapter.pdf.PrintJobMonitorService;
import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.LoanEntity;
import ir.behrooz.loan.entity.LoanEntityDao;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.fragment.LoanMenuFragment;
import ir.behrooz.loan.fragment.LoanSearchFragment;
import ir.behrooz.loan.fragment.LoanSortFragment;
import ir.behrooz.loan.model.SortModel;
import ir.behrooz.loan.report.LoanListPDF;

import static ir.behrooz.loan.common.sql.DBUtil.orderBy;

public class LoanListActivity extends BaseActivity {
    public RecyclerView recyclerView;
    public LoanListAdapter adapter;
    String search = "";
    boolean isLoading = false;
    public Boolean settled;
    public Long personId;
    public ProgressBar progressBar;
    private LoanEntityDao loanEntityDao;
    private PrintManager mgr = null;
    private List<SortModel> sortModels;
    CashtEntity cashtEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_list);
        cashtEntity = new CashtEntity(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mgr = (PrintManager) getSystemService(PRINT_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#303F9F"));
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#303F9F")));
        if (getIntent().hasExtra("settled")) {
            settled = getIntent().getExtras().getBoolean("settled");
        }
        if (getIntent().hasExtra("personId")) {
            personId = getIntent().getExtras().getLong("personId");
            PersonEntity personEntity = DBUtil.getReadableInstance(this).getPersonEntityDao().load(personId);
            getSupportActionBar().setTitle(String.format("%s %s %s", getString(R.string.title_activity_loan_list), personEntity.getName(), personEntity.getFamily()));
        }

        loanEntityDao = DBUtil.getReadableInstance(this).getLoanEntityDao();
        progressBar = findViewById(R.id.loan_progress);
        recyclerView = (RecyclerView) findViewById(R.id.loan_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        sortModels = new ArrayList<>();
        sortModels.add(new SortModel("", "P.NAME,P.FAMILY"));
        initScrollListener();
    }


    @Override
    protected void onResume() {
        super.onResume();
        adapter = new LoanListAdapter(this, search(10, 0));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListner(new LoanListAdapter.OnItemClickListner() {
            @Override
            public void onClick(Long... ids) {
                FragmentManager fm = getSupportFragmentManager();
                LoanMenuFragment loanMenuFragment = LoanMenuFragment.newInstance(ids);
                loanMenuFragment.show(fm, "fragment_menu");
            }
        });
    }

    public void loanActivity(View view) {
        startActivityForResult(new Intent(this, LoanActivity.class), 100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.appSortBar:
                LoanSortFragment loanSortFragment = LoanSortFragment.newInstance("Some Title");
                loanSortFragment.setCompleteListener(new CompleteListener() {
                    @Override
                    public void onComplete(Object obj) {
                        sortModels = (List<SortModel>) obj;
                        adapter = new LoanListAdapter(context, search(10, 0));
                        recyclerView.setAdapter(adapter);
                        adapter.setOnItemClickListner(new LoanListAdapter.OnItemClickListner() {
                            @Override
                            public void onClick(Long... ids) {
                                FragmentManager fm = getSupportFragmentManager();
                                LoanMenuFragment loanMenuFragment = LoanMenuFragment.newInstance(ids);
                                loanMenuFragment.show(fm, "fragment_menu");
                            }
                        });
                    }
                });
                loanSortFragment.show(fm, "fragment_sort");
                return true;

            case R.id.appSearchBar:
                LoanSearchFragment loanSearchFragment = LoanSearchFragment.newInstance(cashtEntity.getId());
                loanSearchFragment.setCompleteListener(new CompleteListener() {
                    @Override
                    public void onComplete(Object obj) {
                        search = (String) obj;
                        adapter = new LoanListAdapter(context, search(10, 0));
                        recyclerView.setAdapter(adapter);
                        adapter.setOnItemClickListner(new LoanListAdapter.OnItemClickListner() {
                            @Override
                            public void onClick(Long... ids) {
                                FragmentManager fm = getSupportFragmentManager();
                                LoanMenuFragment loanMenuFragment = LoanMenuFragment.newInstance(ids);
                                loanMenuFragment.show(fm, "fragment_menu");
                            }
                        });
                    }
                });
                loanSearchFragment.show(fm, "fragment_sort");
                return true;
            case R.id.appPrintBar:
                String fileName = "LoanList_".concat(DateUtil.toPersianWithTimeString(new Date())).concat(".pdf");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    print(fileName,
                            new PdfDocumentAdapter(context, fileName),
                            new PrintAttributes.Builder().build());
                }
                return true;
            case R.id.appHelpBar:
                startActivity(new Intent(this, LoanHelpActivity.class));
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private PrintJob print(String name, PrintDocumentAdapter adapter,
                           PrintAttributes attrs) {
        LoanListPDF loanListPDF = new LoanListPDF(context);
        loanListPDF.createPdf(name, search(1000, 0));
        startService(new Intent(this, PrintJobMonitorService.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return (mgr.print(name, adapter, attrs));
        }
        return null;
    }

    public List<LoanEntity> search(int limit, int offset) {
        if (loanEntityDao.count() == offset)
            return Collections.emptyList();
        StringBuilder sql = new StringBuilder("SELECT L.* FROM Loan L INNER JOIN Person P ON P._id=L.PERSON_ID ");
        sql.append("WHERE L.CASH_ID=").append(cashtEntity.getId().toString());
        sql.append(search);
        if (settled != null)
            sql.append(" AND L.SETTLED = " + (settled ? "1" : "2"));
        if (personId != null)
            sql.append(" AND L.PERSON_ID = " + personId);
        sql.append(" order by ");
        sql.append(orderBy(sortModels));
        sql.append(String.format(Locale.US, " LIMIT %d OFFSET %d;", limit, offset));
        Cursor cursor = DBUtil.getReadableInstance(context).getDatabase().rawQuery(sql.toString(), new String[]{});
        List<LoanEntity> loanEntities = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                loanEntities.add(loanEntityDao.readEntity(cursor, 0));
                cursor.moveToNext();
            }
        }

        return loanEntities;
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
        if (loanEntityDao.count() != adapter.getItemCount()) {
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