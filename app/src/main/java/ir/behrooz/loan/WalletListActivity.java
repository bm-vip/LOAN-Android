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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ir.behrooz.loan.adapter.WalletListAdapter;
import ir.behrooz.loan.adapter.pdf.PdfDocumentAdapter;
import ir.behrooz.loan.adapter.pdf.PrintJobMonitorService;
import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.WalletEntity;
import ir.behrooz.loan.entity.WalletEntityDao;
import ir.behrooz.loan.fragment.WalletSearchFragment;
import ir.behrooz.loan.fragment.WalletSortFragment;
import ir.behrooz.loan.model.SortModel;
import ir.behrooz.loan.report.WalletListPDF;

import static ir.behrooz.loan.common.sql.DBUtil.orderBy;

public class WalletListActivity extends BaseActivity {
    public RecyclerView recyclerView;
    private WalletEntityDao walletEntityDao;
    public WalletListAdapter adapter;
    boolean isLoading = false;
    Long personId;
    String search = "";
    public ProgressBar progressBar;
    private PrintManager mgr = null;
    private List<SortModel> sortModels;
    private CashtEntity cashtEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);
        this.cashtEntity = new CashtEntity(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mgr = (PrintManager) getSystemService(PRINT_SERVICE);
        }
//        titleBar.setText(getString(R.string.title_activity_loan_list));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#81C784"));
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#81C784")));

        if (getIntent().hasExtra("personId")) {
            personId = getIntent().getExtras().getLong("personId");
            PersonEntity personEntity = DBUtil.getReadableInstance(this).getPersonEntityDao().load(personId);
            getSupportActionBar().setTitle(String.format("%s %s %s", getString(R.string.title_activity_wallet_list), personEntity.getName(), personEntity.getFamily()));
        }

        walletEntityDao = DBUtil.getReadableInstance(this).getWalletEntityDao();
        recyclerView = (RecyclerView) findViewById(R.id.wallet_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progressBar = findViewById(R.id.wallet_progress);
        sortModels = new ArrayList<>();
        sortModels.add(new SortModel("", "W.PERSON_ID", SortModel.Sort.DESC));
        sortModels.add(new SortModel("", "W.DATE"));
        initScrollListener();
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
        if (walletEntityDao.count() != adapter.getItemCount()) {
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


    @Override
    protected void onResume() {
        super.onResume();
        adapter = new WalletListAdapter(this, search(10, 0));
        recyclerView.setAdapter(adapter);
    }

    public void walletActivity(View view) {
        startActivityForResult(new Intent(this, WalletActivity.class), 100);
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
                WalletSortFragment walletSortFragment = WalletSortFragment.newInstance();
                walletSortFragment.setCompleteListener(new CompleteListener() {
                    @Override
                    public void onComplete(Object obj) {
                        sortModels = (List<SortModel>) obj;
                        adapter = new WalletListAdapter(context, search(10, 0));
                        recyclerView.setAdapter(adapter);
                    }
                });
                walletSortFragment.show(fm, "fragment_sort");
                return true;

            case R.id.appSearchBar:
                WalletSearchFragment walletSearchFragment = WalletSearchFragment.newInstance(cashtEntity.getId());
                walletSearchFragment.setCompleteListener(new CompleteListener() {
                    @Override
                    public void onComplete(Object obj) {
                        search = (String) obj;
                        adapter = new WalletListAdapter(context, search(10, 0));
                        recyclerView.setAdapter(adapter);
                    }
                });
                walletSearchFragment.show(fm, "fragment_sort");
                return true;
            case R.id.appPrintBar:
                String fileName = "WalletList_".concat(DateUtil.toPersianWithTimeString(new Date()).concat(".pdf"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    print(fileName,
                            new PdfDocumentAdapter(context, fileName),
                            new PrintAttributes.Builder().build());
                }
                return true;
            case R.id.appHelpBar:
                startActivity(new Intent(this, WalletHelpActivity.class));
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private PrintJob print(String name, PrintDocumentAdapter adapter, PrintAttributes attrs) {
        WalletListPDF walletListPDF = new WalletListPDF(context);
        walletListPDF.createPdf(name, search(1000, 0));
        startService(new Intent(this, PrintJobMonitorService.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return (mgr.print(name, adapter, attrs));
        }
        return null;
    }

    public List<WalletEntity> search(int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT W.* FROM Wallet W INNER JOIN Person P ON P._id=W.PERSON_ID ");
        sql.append("WHERE W.CASH_ID=").append(cashtEntity.getId().toString());
        sql.append(search);
        if (personId != null)
            sql.append(" AND W.PERSON_ID = " + personId);
        sql.append(" order by ");
        sql.append(orderBy(sortModels));
        sql.append(String.format(Locale.US, " LIMIT %d OFFSET %d;", limit, offset));
        Cursor cursor = DBUtil.getReadableInstance(context).getDatabase().rawQuery(sql.toString(), new String[]{});
        List<WalletEntity> walletEntities = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                walletEntities.add(walletEntityDao.readEntity(cursor,0));
                cursor.moveToNext();
            }
        }
        return walletEntities;
    }
}