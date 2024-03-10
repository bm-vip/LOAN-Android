package ir.behrooz.loan;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static ir.behrooz.loan.common.Constants.APPLICATION_ID;
import static ir.behrooz.loan.common.ReflectionUtils.convert;
import static ir.behrooz.loan.common.ReflectionUtils.select;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.List;

import ir.behrooz.loan.common.BackupAndRestore;
import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.RealPathUtil;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.CashtEntityDao;
import saman.zamani.persiandate.BuildConfig;

public class SettingsActivity extends BaseActivity {
    protected static final int REQUEST_CODE_PERMISSION = 2296;

    @Override
    protected String getTableName() {
        return CashtEntityDao.TABLENAME;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_PERMISSION) {
                if (grantResults.length > 0) {
                    boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                        // perform action when allow permission success
                    } else {
                        Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                    }
                }

        }
    }

    public static class MainPreferenceFragment extends PreferenceFragmentCompat {
        private boolean checkPermission() {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                return Environment.isExternalStorageManager();
            } else {
                int result = ContextCompat.checkSelfPermission(requireActivity(), READ_EXTERNAL_STORAGE);
                int result1 = ContextCompat.checkSelfPermission(requireActivity(), WRITE_EXTERNAL_STORAGE);
                return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
            }
        }
        private void requestPermission() {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s",requireActivity().getPackageName())));
                    startActivityForResult(intent, REQUEST_CODE_PERMISSION);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, REQUEST_CODE_PERMISSION);
                }
            } else {
                //below android 11
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[] {
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                        },
                        REQUEST_CODE_PERMISSION);
            }
        }
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            ListPreference cashList = findPreference("CASH_LIST");
            final Preference backupPath = findPreference("BACKUP_PATH");
            backupPath.setSummary(new File(Environment.getExternalStorageDirectory(), "Loan").getAbsolutePath());
            Preference backup = findPreference("BACKUP");

            List<CashtEntity> cashtEntities = DBUtil.getReadableInstance(requireActivity()).getCashtEntityDao().loadAll();
            List<Long> ids = select(cashtEntities, "getId");
            List<String> names = select(cashtEntities, "getName");
            List<String> stringIds = convert(ids, String.class);
            cashList.setEntryValues(stringIds.toArray(new String[0]));
            cashList.setEntries(names.toArray(new String[0]));
            cashList.setDefaultValue(stringIds.get(0));
            bindPreferenceSummaryToValue(cashList);
            ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        // Handle the selected file URI from the data
                        String realPath = RealPathUtil.getRealPath(requireActivity(), data.getData());
                        BackupAndRestore.importDB(requireActivity(), new File(realPath));
                    }
                }
            });
            backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    if(checkPermission()) {
                        File backupFile = BackupAndRestore.exportDB(requireActivity());
                        backupPath.setSummary(backupFile.getAbsolutePath());
                        return true;
                    }
                    requestPermission();
                    return false;
                }
            });
            Preference restore = findPreference("RESTORE");
            restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    if (checkPermission()) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.setType("*/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        filePickerLauncher.launch(Intent.createChooser(intent, "انتخاب فایل"));
                        return true;
                    }
                    requestPermission();
                    return false;
                }
            });
            Preference share = findPreference("SHARE");
            share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    if (checkPermission()) {
                        File outputFile = new File(backupPath.getSummary().toString(), Constants.DB_NAME);
                        if (outputFile.exists() && outputFile.isFile()) {
                            Uri uri = FileProvider.getUriForFile(requireActivity(),APPLICATION_ID + ".provider", outputFile);
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_TEXT, Constants.DB_NAME);
                            i.putExtra(Intent.EXTRA_STREAM, uri);
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            i.setType("application/octet-stream");

                            startActivity(i);
                        } else {
                            Snackbar.make(requireView(), getString(R.string.error_first_make_backup), Snackbar.LENGTH_LONG).show();
                        }
                        return true;
                    }
                    requestPermission();
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {

        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                if (index >= 0) {
                    preference.setSummary(listPreference.getEntries()[index]);
                }
            } else if (preference instanceof EditTextPreference) {
                preference.setSummary(stringValue);
            } else {
                switch (preference.getKey()) {
                    case "BACKUP_PATH":
                        File sd = Environment.getExternalStorageDirectory();
                        preference.setSummary(sd.getAbsolutePath());
                        break;
                    default:
                        preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };
}
