package ir.behrooz.loan;

import static ir.behrooz.loan.common.Constants.IRANSANS_LT;
import static ir.behrooz.loan.common.StringUtil.fixWeakCharacters;
import static ir.behrooz.loan.common.StringUtil.isMobileValid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ir.behrooz.loan.common.BaseActivity;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.common.Utils;
import ir.behrooz.loan.entity.CashtEntity;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntityDao;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.PersonEntityDao;
import ir.behrooz.loan.entity.WalletEntityDao;

public class PersonActivity extends BaseActivity {
    private static final int ADD_CONTACT_NUMBER_REQUEST =  99;
    private EditText name, family, phone, nationalCode, accountNumber;
    private Long personId;
    private PersonEntityDao personEntityDao;
    private LoanEntityDao loanEntityDao;
    private WalletEntityDao walletEntityDao;
    private DebitCreditEntityDao debitCreditEntityDao;

    @Override
    protected String getTableName() {
        return PersonEntityDao.TABLENAME;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
//        titleBar.setText(getString(R.string.title_activity_person));
        Utils.askForPermission(this, Manifest.permission.READ_CONTACTS, ADD_CONTACT_NUMBER_REQUEST);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#3F51B5"));
        }
        personEntityDao = getDaoSession().getPersonEntityDao();
        loanEntityDao = getDaoSession().getLoanEntityDao();
        walletEntityDao = getDaoSession().getWalletEntityDao();
        debitCreditEntityDao = getDaoSession().getDebitCreditEntityDao();
        name = findViewById(R.id.name);
        family = findViewById(R.id.family);
        phone = findViewById(R.id.phone);
        nationalCode = findViewById(R.id.nationalCode);
        accountNumber = findViewById(R.id.accountNumber);
        long subscribCode = personEntityDao.count() + 1L;
        nationalCode.setText(subscribCode + "");

        if (getIntent().hasExtra("personId")) {
            personId = getIntent().getExtras().getLong("personId");
            loadForm();
        } else {
            personId = null;
        }
        nationalCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    savePerson(v);
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void loadForm() {
        PersonEntity personEntity = personEntityDao.load(personId);

        name.setText(personEntity.getName());
        family.setText(personEntity.getFamily());
        phone.setText(personEntity.getPhone());
        nationalCode.setText(personEntity.getNationalCode());
        accountNumber.setText(personEntity.getAccountNumber());
    }

    public void savePerson(View view) {
        name.setError(null);
        family.setError(null);
        phone.setError(null);
        nationalCode.setError(null);

        View focusView = null;
        boolean cancel = focusView != null;

        if (TextUtils.isEmpty(name.getText())) {
            name.setError(getString(R.string.error_field_required));
            focusView = name;
            cancel = true;
        } else if (TextUtils.isEmpty(family.getText())) {
            family.setError(getString(R.string.error_field_required));
            focusView = family;
            cancel = true;
        } else if (phone.getText().toString().isEmpty()) {
            phone.setError(getString(R.string.error_field_required));
            focusView = phone;
            cancel = true;
        } else if (!isMobileValid(phone.getText().toString())) {
            phone.setError(getString(R.string.error_field_invalid));
            focusView = phone;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            PersonEntity personEntity;
            if (personId == null) {
                personEntity = new PersonEntity();
                personEntity.setCashId(new CashtEntity(this).getId());
            }
            else
                personEntity = personEntityDao.load(personId);

            personEntity.setName(name.getText().toString());
            personEntity.setFamily(family.getText().toString());
            personEntity.setPhone(phone.getText().toString());
            personEntity.setNationalCode(nationalCode.getText().toString());
            personEntity.setAccountNumber(accountNumber.getText().toString());

            personEntityDao.save(personEntity);

            finish();
        }
    }

    public void addNumber(View view) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, ADD_CONTACT_NUMBER_REQUEST);
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, ADD_CONTACT_NUMBER_REQUEST);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, ADD_CONTACT_NUMBER_REQUEST);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (ADD_CONTACT_NUMBER_REQUEST):
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        Uri contactData = data.getData();
                        Cursor cur = getContentResolver().query(contactData, null, null, null, null);
                        ContentResolver contentResolver = getContentResolver();

                        if (cur.moveToFirst()) {
                            String id = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                            Cursor phoneCur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                            if (phoneCur.moveToFirst()) {
                                String[] displayName = phoneCur.getString(phoneCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).split(" ");
                                StringBuilder builder = new StringBuilder();
                                for (int i = 0; i < displayName.length; i++) {
                                    if (i == 0)
                                        this.name.setText(displayName[i]);
                                    else {
                                        builder.append(displayName[i]);
                                        builder.append(" ");
                                    }
                                }
                                this.family.setText(builder.toString());
                                String number = phoneCur.getString(phoneCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                this.phone.setText(fixWeakCharacters(number.trim().replace(" ", "")));

                            }
                            id = null;
                            phoneCur = null;
                        }
                        contentResolver = null;
                        cur = null;
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (personId != null) {
            getMenuInflater().inflate(R.menu.delete_bar_view, menu);
            return super.onCreateOptionsMenu(menu);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.appDeleteBar) {
            if (personId != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.areYouSure));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deletePerson(personId);
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                ViewGroup viewGroup = (ViewGroup) dialog.findViewById(android.R.id.content);
                new FontChangeCrawler(context.getAssets(), IRANSANS_LT).replaceFonts(viewGroup);
            }
            return true;
        }
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }
    private void deletePerson(long personId) {
        loanEntityDao.queryBuilder().where(LoanEntityDao.Properties.PersonId.eq(personId)).buildDelete().executeDeleteWithoutDetachingEntities();
        walletEntityDao.queryBuilder().where(WalletEntityDao.Properties.PersonId.eq(personId)).buildDelete().executeDeleteWithoutDetachingEntities();
        debitCreditEntityDao.queryBuilder().where(DebitCreditEntityDao.Properties.PersonId.eq(personId)).buildDelete().executeDeleteWithoutDetachingEntities();
        personEntityDao.deleteByKey(personId);
    }
}
