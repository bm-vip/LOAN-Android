package ir.behrooz.loan;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ir.behrooz.loan.adapter.CashListAdapter;
import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.CashtEntityDao;

public class CashListActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private CashtEntityDao cashtEntityDao = getDaoSession().getCashtEntityDao();
    private CashListAdapter adapter;
    private String search = "";

    @Override
    protected String getTableName() {
        return CashtEntityDao.TABLENAME;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#3F51B5"));
        }
        recyclerView = (RecyclerView) findViewById(R.id.cash_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    protected void onResume() {
        super.onResume();
        adapter = new CashListAdapter(this, search());
        recyclerView.setAdapter(adapter);
    }

    public void cashActivity(View view) {
        startActivityForResult(new Intent(this, CashActivity.class), 100);
    }

    private List<CashtEntity> search() {
        String sql = "SELECT C.* FROM Cash C WHERE C.NAME LIKE '%" + search + "%' ORDER BY C.NAME";
        Cursor cursor = getDaoSession().getDatabase().rawQuery(sql, new String[]{});
        List<CashtEntity> cashtEntities = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                cashtEntities.add(cashtEntityDao.readEntity(cursor, 0));
                cursor.moveToNext();
            }
        }
        return cashtEntities;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_search_view, menu);
        MenuItem mSearch = menu.findItem(R.id.singleAppSearchBar);

        SearchView mSearchView = (SearchView) mSearch.getActionView();
        int searchPlateId = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView searchText = mSearchView.findViewById(searchPlateId);
        if(searchText!=null)
            searchText.setTypeface(Typeface.createFromAsset(getAssets(), Constants.IRANSANS_LT));
        mSearchView.setQueryHint(getString(R.string.search));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search = newText;
                adapter = new CashListAdapter(context, search());
                recyclerView.setAdapter(adapter);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
