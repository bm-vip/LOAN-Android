package ir.behrooz.loan.common;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import static ir.behrooz.loan.common.Constants.IRANSANS_LT;

public class BaseActivity extends AppCompatActivity {
    protected Context context;
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
}
