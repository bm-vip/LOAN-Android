package ir.behrooz.loan.common;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import ir.behrooz.loan.widget.TextViewPlus;

/**
 * Created by Behrooz Mohamadi on 16/10/28.
 */
public class FontChangeCrawler {
    private Typeface typeface;

    public FontChangeCrawler(AssetManager assets) {
        typeface = Typeface.createFromAsset(assets, Constants.IRANSANS_LT);
    }

    public FontChangeCrawler(Typeface typeface) {
        this.typeface = typeface;
    }

    public FontChangeCrawler(AssetManager assets, String assetsFontFileName) {
        typeface = Typeface.createFromAsset(assets, assetsFontFileName);
    }

    public void replaceFonts(ViewGroup viewTree) {
        View child;
        for (int i = 0; i < viewTree.getChildCount(); ++i) {
            child = viewTree.getChildAt(i);
            if (child instanceof ViewGroup) {
                replaceFonts((ViewGroup) child);
            } else if (child instanceof TextView && !(child instanceof TextViewPlus)) {
                TextView tv = (TextView) child;
                tv.setTypeface(typeface);
            } else if (child instanceof Button) {
                Button btn = (Button) child;
                btn.setTypeface(typeface);
                btn.setTextColor(Color.WHITE);
            } else if (child instanceof RadioButton) {
                Button radio = (RadioButton) child;
                radio.setTypeface(typeface);
                radio.setTextColor(Color.WHITE);
            } else if (child instanceof EditText) {
                EditText et = (EditText) child;
                et.setTypeface(typeface);
            } else if (child instanceof AutoCompleteTextView) {
                AutoCompleteTextView atv = (AutoCompleteTextView) child;
                atv.setTypeface(typeface);
            }
        }
    }
}