package ir.behrooz.loan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.view.MenuItem;

import java.io.File;
import java.util.List;

import ir.behrooz.loan.common.AlarmReceiver;
import ir.behrooz.loan.common.BackupAndRestore;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.RealPathUtil;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.CashtEntity;

import static ir.behrooz.loan.common.ReflectionUtils.convert;
import static ir.behrooz.loan.common.ReflectionUtils.select;

public class SettingsActivity extends PreferenceActivity {
    public static final int RESTORE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        AlarmReceiver alarmReceiver = new AlarmReceiver();

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data != null) {
                if (requestCode == RESTORE_CODE) {
                    String realPath = RealPathUtil.getRealPath(getActivity(), data.getData());
                    BackupAndRestore.importDB(getActivity(), new File(realPath));
                }
            }
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            ListPreference cashList = (ListPreference) findPreference("CASH_LIST");
            final Preference backupPath = findPreference("BACKUP_PATH");
            Preference backup = findPreference("BACKUP");

            List<CashtEntity> cashtEntities = DBUtil.getReadableInstance(getActivity()).getCashtEntityDao().loadAll();
            List<Long> ids = select(cashtEntities, "getId");
            List<String> names = select(cashtEntities, "getName");
            List<String> stringIds = convert(ids, String.class);
            cashList.setEntryValues(stringIds.toArray(new String[0]));
            cashList.setEntries(names.toArray(new String[0]));
            cashList.setDefaultValue(stringIds.get(0));

            bindPreferenceSummaryToValue(cashList);

            backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    File backupFile = BackupAndRestore.exportDB(getActivity());
                    backupPath.setSummary(backupFile.getAbsolutePath());
                    return true;
                }
            });
            Preference restore = findPreference("RESTORE");
            restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(Intent.createChooser(intent, "انتخاب فایل"), RESTORE_CODE);
                    return true;
                }
            });
            Preference share = findPreference("SHARE");
            share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    File outputFile = new File(backupPath.getSummary().toString(), Constants.DB_NAME);
                    if (outputFile.exists() && outputFile.isFile()) {
                        Uri uri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", outputFile);

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.putExtra(Intent.EXTRA_TEXT, Constants.DB_NAME);
                        i.putExtra(Intent.EXTRA_STREAM, uri);
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        i.setType("*/*");
                        startActivity(i);
                    } else {
                        Snackbar.make(getView(), getString(R.string.error_first_make_backup), Snackbar.LENGTH_LONG).show();
                    }
                    return true;
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

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
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
