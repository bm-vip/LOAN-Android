package ir.behrooz.loan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mojtaba.materialdatetimepicker.utils.LanguageUtils;

import ir.behrooz.loan.common.AlarmReceiver;
import ir.behrooz.loan.common.BackupAndRestore;
import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.common.sql.Oprator;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);

        factor = getResources().getDisplayMetrics().density;
//        titleBar.setText(getString(R.string.app_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#3F51B5"));
        }

        debitCreditEntityDao = DBUtil.getReadableInstance(this).getDebitCreditEntityDao();
        cashtEntityDao = DBUtil.getReadableInstance(this).getCashtEntityDao();
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
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Cash ADD COLUMN IF NOT EXIST CURRENCY_TYPE TEXT NOT NULL DEFAULT '" + getString(R.string.toman) + "'");
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Cash ADD COLUMN IF NOT EXIST WITH_DEPOSIT INTEGER NOT NULL DEFAULT 1");
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Cash ADD COLUMN IF NOT EXIST CHECK_CASH_REMAIN INTEGER NOT NULL DEFAULT 1");
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Cash ADD COLUMN IF NOT EXIST AFFECT_NEXT INTEGER NOT NULL DEFAULT 1");
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Cash ADD COLUMN IF NOT EXIST NOTIFY_DAY_OF_LOAN INTEGER NOT NULL DEFAULT 1");
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Cash ADD COLUMN IF NOT EXIST NOTIFY_DAY_OF_LOAN INTEGER NOT NULL DEFAULT 1");
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
        Long walletSum = 0L;
        try {
            cashtEntity = new CashtEntity(this);
            walletSum = DBUtil.sum(this, WalletEntityDao.Properties.Value, WalletEntityDao.TABLENAME, new WhereCondition(WalletEntityDao.Properties.Status, "1", Oprator.EQUAL, "AND"), new WhereCondition(WalletEntityDao.Properties.CashId, cashtEntity.getId().toString(), Oprator.EQUAL));
            walletSum -= DBUtil.sum(this, WalletEntityDao.Properties.Value, WalletEntityDao.TABLENAME, new WhereCondition(WalletEntityDao.Properties.Status, "0", Oprator.EQUAL, "AND"), new WhereCondition(WalletEntityDao.Properties.CashId, cashtEntity.getId().toString(), Oprator.EQUAL));
        } catch (Exception ex) {
            walletSum = DBUtil.sum(this, WalletEntityDao.Properties.Value, WalletEntityDao.TABLENAME);
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Wallet ADD COLUMN CASH_ID INTEGER NOT NULL DEFAULT 1");
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Loan ADD COLUMN CASH_ID INTEGER NOT NULL DEFAULT 1");
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Person ADD COLUMN CASH_ID INTEGER NOT NULL DEFAULT 1");
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE DebitCredit ADD COLUMN CASH_ID INTEGER NOT NULL DEFAULT 1");
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Wallet ADD COLUMN STATUS INTEGER NOT NULL DEFAULT 1");
        }
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
        long paidLoan = 0L;
        try {
            paidLoan = DBUtil.sum(context, LoanEntityDao.Properties.Value, LoanEntityDao.TABLENAME, new WhereCondition(LoanEntityDao.Properties.WinDate, "NULL", Oprator.IS_NOT, "AND"), new WhereCondition(LoanEntityDao.Properties.CashId, cashtEntity.getId().toString(), Oprator.EQUAL));
        } catch (Exception e) {
            DBUtil.getWritableInstance(this).getDatabase().execSQL("ALTER TABLE Loan ADD COLUMN WIN_DATE INTEGER");
        }
        if (cashtEntity.getWithDeposit()) {
            long unpaidSum = DBUtil.sum(context, DebitCreditEntityDao.Properties.Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(DebitCreditEntityDao.Properties.PayStatus, "0", Oprator.EQUAL, "AND"), new WhereCondition(DebitCreditEntityDao.Properties.CashId, cashtEntity.getId().toString(), Oprator.EQUAL));
            cashRemain = walletSum - unpaidSum;
        } else {
            long paidInstallment = DBUtil.sum(context, DebitCreditEntityDao.Properties.Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(DebitCreditEntityDao.Properties.PayStatus, "1", Oprator.EQUAL, "AND"), new WhereCondition(DebitCreditEntityDao.Properties.CashId, cashtEntity.getId().toString(), Oprator.EQUAL));
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
        return moneySeparator(context, DBUtil.sum(context, DebitCreditEntityDao.Properties.Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(DebitCreditEntityDao.Properties.PayStatus, payStatus, Oprator.EQUAL, "AND"), new WhereCondition(DebitCreditEntityDao.Properties.CashId, cashId, Oprator.EQUAL)));
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
