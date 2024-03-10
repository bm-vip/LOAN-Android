package ir.behrooz.loan.common;

import static ir.behrooz.loan.common.Constants.IRANSANS_LT;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.greenrobot.greendao.Property;

import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.common.sql.WhereCondition;
import ir.behrooz.loan.entity.DaoSession;

public abstract class BaseActivity extends AppCompatActivity {
    protected Context context;
    protected abstract String getTableName();
    protected ViewGroup viewGroup;
//    protected TextViewPlus titleBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    }, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setLocale("fa", getBaseContext().getResources());
        viewGroup = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);
        new FontChangeCrawler(getAssets(), IRANSANS_LT).replaceFonts(viewGroup);
    }
    protected DaoSession getDaoSession(){
        return DBUtil.getWritableInstance(this);
    }
    protected Long dbSum(Property property, String tableName, WhereCondition... conditions) {
        return DBUtil.sum(this, property, tableName, conditions);
    }
    protected Long dbSum(Property property, WhereCondition... conditions) {
        return DBUtil.sum(this, property, getTableName(), conditions);
    }
    protected Long dbSum(StringBuilder sql, WhereCondition... conditions) {
        if (conditions.length > 0) {
            sql.append(" WHERE ");
            for (int i = 0; i < conditions.length; i++) {
                WhereCondition condition = conditions[i];
                sql.append(String.format("%s %s %s", condition.getProperty().columnName, condition.getOprator().getValue(), condition.getValue()));
                if (condition.getAndOr() != null)
                    sql.append(String.format(" %s ", condition.getAndOr()));
            }
        }
        Cursor cursor = DBUtil.getReadableInstance(this).getDatabase().rawQuery(sql.toString(), new String[]{});
        cursor.moveToFirst();
        return cursor.getLong(0);
    }
    protected Cursor find(String tableName, WhereCondition... conditions) {
        return DBUtil.find(this, tableName, conditions);
    }
    protected Cursor find(WhereCondition... conditions) {
        return DBUtil.find(this, getTableName(), conditions);
    }
}
