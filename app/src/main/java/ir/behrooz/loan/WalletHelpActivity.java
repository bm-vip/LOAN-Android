package ir.behrooz.loan;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;

import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.entity.WalletEntityDao;

public class WalletHelpActivity extends BaseActivity {

    @Override
    protected String getTableName() {
        return WalletEntityDao.TABLENAME;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_help);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#81C784"));
        }
        Toolbar toolbar = findViewById(R.id.walletHelpToolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#81C784"));
        SpannableStringBuilder searchBuilder = new SpannableStringBuilder();
        searchBuilder.append(getString(R.string.searchAction1))
                .append(" ", new ImageSpan(this, R.drawable.ic_search_black), 0)
                .append(getString(R.string.searchAction2));
        TextView searchAction = findViewById(R.id.searchAction);
        searchAction.setText(searchBuilder);

        SpannableStringBuilder sortBuilder = new SpannableStringBuilder();
        sortBuilder.append(getString(R.string.sortAction1))
                .append(" ", new ImageSpan(this, R.drawable.ic_sort_black_24dp), 0)
                .append(getString(R.string.sortAction2));
        TextView sortAction = findViewById(R.id.sortAction);
        sortAction.setText(sortBuilder);
    }

}
