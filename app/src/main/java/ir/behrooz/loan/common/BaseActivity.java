package ir.behrooz.loan.common;

import static ir.behrooz.loan.common.Constants.IRANSANS_LT;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BaseActivity extends AppCompatActivity {
    protected Context context;
    protected ViewGroup viewGroup;
//    protected TextViewPlus titleBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setLocale("fa", getBaseContext().getResources());
        viewGroup = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);
        new FontChangeCrawler(getAssets(), IRANSANS_LT).replaceFonts(viewGroup);
    }
}
