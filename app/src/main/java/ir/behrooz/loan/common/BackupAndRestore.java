package ir.behrooz.loan.common;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

public class BackupAndRestore {
    public static void importDB(Context context, File inputFile) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                File dstFile = context.getDatabasePath(Constants.DB_NAME);
                FileUtil.copyFile(inputFile, dstFile);
                Toast.makeText(context, "بازیابی اطلاعات با موفقیت انجام شد", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File exportDB(Context context, File outputFile) {
        try {
            if (outputFile.canWrite()) {
                File srcFile = context.getDatabasePath(Constants.DB_NAME);
                FileUtil.copyFile(srcFile, new File(outputFile, Constants.DB_NAME));
                Toast.makeText(context, "استخراج اطلاعات با موفقیت انجام شد، و در مسیر فایل استخراج شده بنام Loan.db ذخیره شد", Toast.LENGTH_LONG).show();
                return outputFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File exportDB(Context context) {
        File outputFile = new File(Environment.getExternalStorageDirectory(), "Loan");
        if (!outputFile.exists()) {
            if (!outputFile.mkdirs()) {
                return null;
            }
        }
        return exportDB(context, outputFile);
    }
}