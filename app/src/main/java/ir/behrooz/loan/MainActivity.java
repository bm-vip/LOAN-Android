package ir.behrooz.loan;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import ir.behrooz.loan.common.AlarmReceiver;
import ir.behrooz.loan.common.BackupAndRestore;
import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.LanguageUtils;
import ir.behrooz.loan.common.Utils;
import ir.behrooz.loan.common.sql.And;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.common.sql.Operator;
import ir.behrooz.loan.common.sql.WhereCondition;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.CashtEntityDao;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntityDao;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.entity.WalletEntityDao;
import ir.behrooz.loan.fragment.AboutFragment;
import ir.behrooz.loan.widget.TextViewPlus;

import static ir.behrooz.loan.common.Constants.IRANSANS_LT;
import static ir.behrooz.loan.common.Constants.IRANSANS_MD;
import static ir.behrooz.loan.common.StringUtil.moneySeparator;
import static ir.behrooz.loan.common.Utils.getVersion;
import static ir.behrooz.loan.common.Utils.landScape;
import static ir.behrooz.loan.entity.WalletEntityDao.Properties.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Button walletBtn, loanBtn, personBtn, installmentBtn, paidBtn, unpaidBtn;
    TextViewPlus titleBar;
    Toolbar toolbar;
    DebitCreditEntityDao debitCreditEntityDao;
    CashtEntityDao cashtEntityDao;
    NavigationView navigationView;
    float factor;
    AlarmReceiver alarmReceiver = new AlarmReceiver();
    private CashtEntity cashtEntity;
    private SharedPreferences preferences;

    @Override
    protected String getTableName() {
        return WalletEntityDao.TABLENAME;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);

        factor = getResources().getDisplayMetrics().density;
//        titleBar.setText(getString(R.string.app_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#3F51B5"));
        }

        debitCreditEntityDao = getDaoSession().getDebitCreditEntityDao();
        cashtEntityDao = getDaoSession().getCashtEntityDao();
        walletBtn = (Button) findViewById(R.id.walletBtn);
        loanBtn = (Button) findViewById(R.id.loanBtn);
        personBtn = (Button) findViewById(R.id.personBtn);
        installmentBtn = (Button) findViewById(R.id.installmentBtn);
        paidBtn = (Button) findViewById(R.id.paidBtn);
        unpaidBtn = (Button) findViewById(R.id.unpaidBtn);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        titleBar = toolbar.findViewById(R.id.title_bar);

        if (landScape(this)) {
            titleBar.setGravity(Gravity.LEFT);
            titleBar.setTextSize(16);
            titleBar.setIconSize(16);
            titleBar.setTypeface(Typeface.createFromAsset(context.getAssets(), IRANSANS_MD));
            RelativeLayout relativeLayout = findViewById(R.id.mainRelativeLayout);
            relativeLayout.setPadding((int) (5 * factor), (int) (50 * factor), (int) (5 * factor), (int) (5 * factor));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_menu);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        try {
            cashtEntity = new CashtEntity(this);
        } catch (Exception ex) {
                DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Cash ADD COLUMN SHOW_SETTLED_LOAN INTEGER NOT NULL DEFAULT 1");
                DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Person ADD COLUMN ACCOUNT_NUMBER TEXT");
            cashtEntity = new CashtEntity(this);
        }
        if (cashtEntityDao.count() == 0) {
            cashtEntityDao.save(cashtEntity);
        }
        if (cashtEntity.getNotifyDayOfLoan()) {
            if (!alarmReceiver.isAlarmRunning(this))
                alarmReceiver.setAlarm(this);
        } else {
            alarmReceiver.cancelAlarm(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cashtEntity = new CashtEntity(this);
        Long walletSum = dbSum(new StringBuilder(String.format("SELECT SUM(CASE WHEN %s = 1 THEN %s ELSE -%s END) FROM %s", Status.columnName, Value.columnName, Value.columnName, WalletEntityDao.TABLENAME)),
                new WhereCondition(CashId, cashtEntity.getId().toString()));
        if (cashtEntity.getWithDeposit()) {
            walletBtn.setText(String.format("%s\n%s", getString(R.string.wallets), moneySeparator(context, walletSum)));
            walletBtn.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, (int) (130 * factor), 1);
            params.setMargins(0, 0, (int) (5 * factor), 0);
            installmentBtn.setLayoutParams(params);
        } else {
            walletBtn.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, (int) (130 * factor), 1);
            params.setMargins(0, 0, 0, 0);
            installmentBtn.setLayoutParams(params);
        }
        long personCount = DBUtil.getReadableInstance(this).getPersonEntityDao().queryBuilder().where(PersonEntityDao.Properties.CashId.eq(cashtEntity.getId())).count();
        personBtn.setText(String.format("%s\n%d", getString(R.string.title_activity_person_list), personCount));
        long loanCount = DBUtil.getReadableInstance(this).getLoanEntityDao().queryBuilder().where(LoanEntityDao.Properties.CashId.eq(cashtEntity.getId())).count();
        loanBtn.setText(String.format("%s\n%d", getString(R.string.title_activity_loan_list), loanCount));
        long installmentCount = DBUtil.getReadableInstance(this).getDebitCreditEntityDao().queryBuilder().where(DebitCreditEntityDao.Properties.CashId.eq(cashtEntity.getId())).count();
        installmentBtn.setText(String.format("%s\n%d", getString(R.string.installments), installmentCount));
        paidBtn.setText(String.format("%d\nقسط %s\n%s", getDebitCreditCount(cashtEntity.getId(), true), getString(R.string.paid), getDebitCreditSum(cashtEntity.getId().toString(), "1")));
        unpaidBtn.setText(String.format("%d\nقسط %s\n%s", getDebitCreditCount(cashtEntity.getId(), false), getString(R.string.unpaid), getDebitCreditSum(cashtEntity.getId().toString(), "0")));

        long cashRemain = 0L;
        long paidLoan = dbSum(LoanEntityDao.Properties.Value, LoanEntityDao.TABLENAME, new WhereCondition(LoanEntityDao.Properties.WinDate, "NULL", Operator.IS_NOT), new And(LoanEntityDao.Properties.CashId, cashtEntity.getId().toString()));

        if (cashtEntity.getWithDeposit()) {
            long unpaidSum = dbSum(DebitCreditEntityDao.Properties.Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(DebitCreditEntityDao.Properties.PayStatus, "0"), new And(DebitCreditEntityDao.Properties.CashId, cashtEntity.getId().toString()));
            cashRemain = walletSum - unpaidSum;
        } else {
            long paidInstallment = dbSum(DebitCreditEntityDao.Properties.Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(DebitCreditEntityDao.Properties.PayStatus, "1"), new And(DebitCreditEntityDao.Properties.CashId, cashtEntity.getId().toString()));
            cashRemain = paidInstallment - paidLoan;
        }

        View headerLayout = navigationView.getHeaderView(0);
        TextViewPlus menuHeaderTop = headerLayout.findViewById(R.id.menu_header_top);
        TextViewPlus menuHeaderBottom = headerLayout.findViewById(R.id.menu_header_bottom);
        menuHeaderBottom.setText(String.format("%s %s %s", getString(R.string.app_name), getString(R.string.version), LanguageUtils.getPersianNumbers(getVersion(context))));
        menuHeaderTop.setText(cashtEntity.getName());

        if (landScape(this))
            titleBar.setText(String.format("%s\t:%s", moneySeparator(context, cashRemain, false), getString(R.string.cashRemain)));
        else
            titleBar.setText(String.format("%s\n%s", getString(R.string.cashRemain), moneySeparator(context, cashRemain)));
        new FontChangeCrawler(getAssets(), IRANSANS_LT).replaceFonts(navigationView);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (!preferences.getBoolean("EXIT_AND_BACKUP", true))
            finish();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.areYouSure));
            builder.setPositiveButton(R.string.exitAndBackup, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    BackupAndRestore.exportDB(context);
                    dialog.dismiss();
                    finish();
                }
            });
            builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    finish();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
            Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setTextColor(Color.BLACK);
            Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(Color.BLUE);
            viewGroup = (ViewGroup) dialog.findViewById(android.R.id.content);
            new FontChangeCrawler(getAssets(), IRANSANS_LT).replaceFonts(viewGroup);
        }
    }

    private String getDebitCreditSum(String cashId, String payStatus) {
        return moneySeparator(context, dbSum(DebitCreditEntityDao.Properties.Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(DebitCreditEntityDao.Properties.PayStatus, payStatus), new And(DebitCreditEntityDao.Properties.CashId, cashId)));
    }

    private long getDebitCreditCount(Long cashId, boolean payStatus) {
        return debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.CashId.eq(cashId), DebitCreditEntityDao.Properties.PayStatus.eq(payStatus)).count();
    }

    public void personListActivity(View view) {
        startActivity(new Intent(this, PersonListActivity.class));
    }

    public void loanListActivity(View view) {
        startActivity(new Intent(this, LoanListActivity.class));
    }

    public void walletListActivity(View view) {
        startActivity(new Intent(this, WalletListActivity.class));
    }

    public void debitCreditListActivity(View view) {
        startActivity(new Intent(this, DebitCreditListActivity.class));
    }

    public void paidPage(View view) {
        Intent intent = new Intent(this, DebitCreditListActivity.class);
        intent.putExtra("paid", true);
        intent.putExtra("color", "#EA80FC");
        startActivity(intent);
    }

    public void unpaidPage(View view) {
        Intent intent = new Intent(this, DebitCreditListActivity.class);
        intent.putExtra("paid", false);
        intent.putExtra("color", "#AA00FF");
        startActivity(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.cashDeffination) {
            startActivity(new Intent(this, CashListActivity.class));
        }
        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        if (id == R.id.about) {
            FragmentManager fm = getSupportFragmentManager();
            AboutFragment aboutFragment = AboutFragment.newInstance();
            aboutFragment.show(fm, "fragment_about");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_menu);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
