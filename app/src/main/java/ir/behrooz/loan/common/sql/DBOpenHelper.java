package ir.behrooz.loan.common.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;

import static ir.behrooz.loan.common.Utils.getVersionCode;
import static ir.behrooz.loan.entity.DaoMaster.createAllTables;

public class DBOpenHelper extends DatabaseOpenHelper {
    public int version;
    public DBOpenHelper(Context context, String name) {
        super(context, name, getVersionCode(context));
        this.version = getVersionCode(context);
    }

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, getVersionCode(context));
        this.version = getVersionCode(context);
    }

    @Override
    public void onCreate(Database db) {
        Log.i("greenDAO", "Creating tables for schema version " + version);
        createAllTables(db, true);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
//        Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
//        dropAllTables(db, true);
        onCreate(db);
    }
}
