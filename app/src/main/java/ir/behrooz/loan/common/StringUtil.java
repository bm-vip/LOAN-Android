package ir.behrooz.loan.common;

import android.content.Context;
import android.text.TextWatcher;
import android.widget.EditText;

import com.mojtaba.materialdatetimepicker.utils.LanguageUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import ir.behrooz.loan.entity.CashtEntity;

import static ir.behrooz.loan.common.NumberUtil.getLong;

public class StringUtil {

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String removeWeakCharacters(String data) {
        final char rightToLeftEmbedding = (char) 0x202B;
        final char popDirectionalFormatting = (char) 0x202C;
        return data.replaceAll(String.format("[\\,/,+,-,=,;,$,%c,%c]", rightToLeftEmbedding, popDirectionalFormatting), "");
    }

    public static String fixWeakCharacters(String data) {
        final char RightToLeftEmbedding = (char) 0x202B;
        final char PopDirectionalFormatting = (char) 0x202C;
        if (data == null && data.trim().length() == 0)
            return "";
        String[] weakCharacters = new String[]{"\\", "/", "+", "-", "=", ";", "$"};
        for (String weakCharacter : weakCharacters) {
            data = data.replace(weakCharacter, RightToLeftEmbedding + weakCharacter + PopDirectionalFormatting);
        }
        return data;
    }

    public static String moneySeparator(Context context, Long value, boolean rightCurrency) {
        String currencyType = new CashtEntity(context).getCurrencyType();
        if (value == null)
            return String.format("0 %s", currencyType);
        NumberFormat formatter = new DecimalFormat("#,###");
        if (rightCurrency)
            return fixWeakCharacters(String.format("%s %s", LanguageUtils.getPersianNumbers(formatter.format(value)), currencyType));
        return fixWeakCharacters(String.format("%s %s", currencyType, LanguageUtils.getPersianNumbers(formatter.format(value))));
    }

    public static String moneySeparator(Context context, Long value) {
        return moneySeparator(context, value, true);
    }

    public static Long removeSeparator(Context context, String value) {
        if (isNullOrEmpty(value))
            return 0L;
        String currencyType = new CashtEntity(context).getCurrencyType();
        value = LanguageUtils.getLatinNumbers(value);
        value = value.replaceAll("[ " + currencyType + ",٬]", "");
        return getLong(value);
    }

    public static boolean isMobileValid(String mobile) {
        if (mobile.contains("+")) {
            mobile = "+" + removeWeakCharacters(mobile);
        }
        return mobile.matches("(0098|\\+98|0)[9][0-9]{9}");
    }

    public static void onChangedEditText(Context context, TextWatcher watcher, EditText editText, CharSequence charSequence, int i2) {
        editText.removeTextChangedListener(watcher);
        Long amount = removeSeparator(context, charSequence.toString());
        if (i2 == 0) {
            if (amount > 0) {
                String s = amount.toString();
                s = s.substring(0, s.length() - 1);
                amount = isNullOrEmpty(s) ? 0L : Long.valueOf(s);
            }
        }
        editText.setText(moneySeparator(context, amount));
        editText.setSelection(editText.getText().toString().length());
        editText.addTextChangedListener(watcher);
    }
}
