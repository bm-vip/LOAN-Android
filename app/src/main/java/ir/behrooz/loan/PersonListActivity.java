package ir.behrooz.loan;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ir.behrooz.loan.adapter.PersonListAdapter;
import ir.behrooz.loan.adapter.pdf.PdfDocumentAdapter;
import ir.behrooz.loan.adapter.pdf.PrintJobMonitorService;
import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.CompleteListener;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.fragment.PersonMenuFragment;
import ir.behrooz.loan.fragment.PersonSortFragment;
import ir.behrooz.loan.model.SortModel;
import ir.behrooz.loan.report.PersonListPDF;

import static ir.behrooz.loan.common.sql.DBUtil.orderBy;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class PersonListActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private PersonEntityDao personEntityDao = DBUtil.getReadableInstance(this).getPersonEntityDao();
    PersonListAdapter adapter;
    private PrintManager pmgr = null;
    private List<SortModel> sortModels;
    private String search = "";
    CashtEntity cashtEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list);
        cashtEntity = new CashtEntity(this);
        pmgr = (PrintManager) getSystemService(PRINT_SERVICE);
//        titleBar.setText(getString(R.string.title_activity_person_list));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#3F51B5"));
        }
        sortModels = new ArrayList<>();
        sortModels.add(new SortModel("", "P.NAME,P.FAMILY"));
        recyclerView = (RecyclerView) findViewById(R.id.person_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    protected void onResume() {
        super.onResume();
        adapter = new PersonListAdapter(this, search());
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListner(new PersonListAdapter.OnItemClickListner() {
            @Override
            public void onClick(Long id) {
                FragmentManager fm = getSupportFragmentManager();
                PersonMenuFragment personMenuFragment = PersonMenuFragment.newInstance(id);
                personMenuFragment.show(fm, "fragment_menu");
            }
        });
    }

    public void personActivity(View view) {
        startActivityForResult(new Intent(this, PersonActivity.class), 100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.personAppSortBar) {
            FragmentManager fm = getSupportFragmentManager();
            PersonSortFragment personSortFragment = PersonSortFragment.newInstance();
            personSortFragment.setCompleteListener(new CompleteListener() {
                @Override
                public void onComplete(Object obj) {
                    sortModels = (List<SortModel>) obj;
                    adapter = new PersonListAdapter(context, search());
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListner(new PersonListAdapter.OnItemClickListner() {
                        @Override
                        public void onClick(Long id) {
                            FragmentManager fm = getSupportFragmentManager();
                            PersonMenuFragment personMenuFragment = PersonMenuFragment.newInstance(id);
                            personMenuFragment.show(fm, "fragment_menu");
                        }
                    });
                }
            });
            personSortFragment.show(fm, "fragment_sort");
            return true;
        } else if (item.getItemId() == R.id.personAppPrintBar) {
            String fileName = "PersonList_".concat(DateUtil.toPersianWithTimeString(new Date())).concat(".pdf");
            print(fileName,
                    new PdfDocumentAdapter(context, fileName),
                    new PrintAttributes.Builder().build());
            return true;
        } else if (item.getItemId() == R.id.personAppHelpBar) {
            startActivity(new Intent(this, PersonHelpActivity.class));
            return true;
        }
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    private List<PersonEntity> search() {
        StringBuilder sql = new StringBuilder("SELECT P.* FROM Person P WHERE P.CASH_ID =");
        sql.append(cashtEntity.getId());
        sql.append(" AND (P.NAME LIKE '%".concat(search));
        sql.append("%' OR P.FAMILY LIKE '%".concat(search));
        sql.append("%') order by ");
        sql.append(orderBy(sortModels));
        Cursor cursor = DBUtil.getReadableInstance(context).getDatabase().rawQuery(sql.toString(), new String[]{});
        List<PersonEntity> personEntities = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                personEntities.add(personEntityDao.readEntity(cursor, 0));
                cursor.moveToNext();
            }
        }
        return personEntities;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.person_search_view, menu);
        MenuItem mSearch = menu.findItem(R.id.singleAppSearchBar);

        SearchView mSearchView = (SearchView) mSearch.getActionView();
//        TextView searchText = (TextView) mSearchView.findViewById(R.id.search_src_text);
//        searchText.setTypeface(Typeface.createFromAsset(getAssets(), Constants.IRANSANS_LT));
        mSearchView.setQueryHint(getString(R.string.search));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search = newText;
                adapter = new PersonListAdapter(context, search());
                recyclerView.setAdapter(adapter);
                adapter.setOnItemClickListner(new PersonListAdapter.OnItemClickListner() {
                    @Override
                    public void onClick(Long id) {
                        FragmentManager fm = getSupportFragmentManager();
                        PersonMenuFragment personMenuFragment = PersonMenuFragment.newInstance(id);
                        personMenuFragment.show(fm, "fragment_menu");
                    }
                });
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private PrintJob print(String fileName, PrintDocumentAdapter adapter, PrintAttributes attrs) {
        PersonListPDF personListPDF = new PersonListPDF(context);
        personListPDF.createPdf(fileName, search());
        startService(new Intent(this, PrintJobMonitorService.class));
        return (pmgr.print(fileName, adapter, attrs));
    }
}
