package ir.behrooz.loan.common.sql;

import android.content.Context;
import android.database.Cursor;

import org.greenrobot.greendao.Property;

import java.util.List;

import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.entity.DaoMaster;
import ir.behrooz.loan.entity.DaoSession;
import ir.behrooz.loan.model.SortModel;

/**
 * Created by b.mohammadi on 6/26/2018.
 */
public class DBUtil {
    private DBUtil(){}
    private static DaoSession writableInstance;
    private static DaoSession readableInstance;

    public static DaoSession getWritableInstance (Context context) {
        if(writableInstance == null) {
            writableInstance = new DaoMaster(new DBOpenHelper(context, Constants.DB_NAME).getWritableDb()).newSession();
        }
        return writableInstance;
    }
    public static DaoSession getReadableInstance (Context context) {
        if(readableInstance == null){
            readableInstance = new DaoMaster(new DBOpenHelper(context, Constants.DB_NAME).getReadableDb()).newSession();
        }
        return readableInstance;
    }
   public static Long sum(Context context, Property property, String tableName, WhereCondition... conditions){
        StringBuilder sql = new StringBuilder("SELECT SUM(" + property.columnName + ") FROM " + tableName);
        if (conditions.length > 0) {
            sql.append(" WHERE ");
            for (int i = 0; i < conditions.length; i++) {
                WhereCondition condition = conditions[i];
                sql.append(String.format("%s %s %s", condition.getProperty().columnName, condition.getOprator().getValue(), condition.getValue()));
                if (!condition.getAndOr().isEmpty())
                    sql.append(String.format(" %s ", condition.getAndOr()));
            }
        }

       Cursor cursor = DBUtil.getReadableInstance(context).getDatabase().rawQuery(sql.toString(), new String[]{});
       cursor.moveToFirst();
       return cursor.getLong(0);
   }

   public static Cursor find(Context context, String tableName, WhereCondition... conditions){
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
        if (conditions.length > 0) {
            sql.append(" WHERE ");
            for (int i = 0; i < conditions.length; i++) {
                WhereCondition condition = conditions[i];
                sql.append(String.format("%s %s %s", condition.getProperty().columnName, condition.getOprator().getValue(), condition.getValue()));
                if (!condition.getAndOr().isEmpty())
                    sql.append(String.format(" %s ", condition.getAndOr()));
            }
        }

       Cursor cursor = DBUtil.getReadableInstance(context).getDatabase().rawQuery(sql.toString(), new String[]{});
       return cursor;
   }

    public static String orderBy(List<SortModel> list) {
        StringBuilder builder = new StringBuilder();
        if (list.isEmpty())
            return "";
        for (int i = 0; i < list.size(); i++) {
            SortModel item = list.get(i);
            builder.append(item.getValue());
            if (i < list.size() - 1)
                builder.append(",");
        }
        builder.append(" ");
        builder.append(list.get(0).getSort().name());
        return builder.toString();
    }
}
