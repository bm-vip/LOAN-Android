package ir.behrooz.loan;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.AppBarLayout;

import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.entity.DebitCreditEntityDao;

public class DebitCreditHelpActivity extends BaseActivity {
    public String color = "#00C853";

    @Override
    protected String getTableName() {
        return DebitCreditEntityDao.TABLENAME;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debit_credit_help);

        if (getIntent().hasExtra("color")) {
            color = getIntent().getExtras().getString("color");
        }

        Toolbar toolbar = findViewById(R.id.debitCreditHelpToolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Color.parseColor(color));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_debit_credit_help);
        appBarLayout.setBackgroundColor(Color.parseColor(color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(color));
        }

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
