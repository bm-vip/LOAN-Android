package ir.behrooz.loan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.EditTextPreference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.Constants;
import ir.behrooz.loan.common.FontChangeCrawler;

import static ir.behrooz.loan.common.StringUtil.isNullOrEmpty;

public class EditTextPreferencePlus extends EditTextPreference {
    private final static String PATH_TO_FONT = Constants.IRANSANS_LT;
    private final static int DEFAULT_ICON_SIZE = 16;
    private final static int DEFAULT_ICON_COLOR = Color.GRAY;
    private Typeface font;
    AttributeSet attrs;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EditTextPreferencePlus(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.attrs = attrs;
    }

    public EditTextPreferencePlus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.attrs = attrs;
    }

    public EditTextPreferencePlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
    }

    public EditTextPreferencePlus(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        initialize(view);
        initializeAttributes(view);
    }


    private void initialize(View view) {
        font = Typeface.createFromAsset(getContext().getAssets(), PATH_TO_FONT);
        new FontChangeCrawler(font).replaceFonts((ViewGroup) view);
    }

    private void initializeAttributes(View view) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TextViewPlus);
        initSize(view, typedArray);
        initColor(view, typedArray);
        initFont(view, typedArray);
        typedArray.recycle();
    }

    private void initSize(View view, TypedArray typedArray) {
        int size = typedArray.getInt(R.styleable.TextViewPlus_tvp_size, DEFAULT_ICON_SIZE);
        TextView titleView = view.findViewById(android.R.id.title);
        titleView.setTextSize(size);
    }

    private void initColor(View view, TypedArray typedArray) {
        int iconColor = typedArray.getColor(R.styleable.TextViewPlus_tvp_color, DEFAULT_ICON_COLOR);
        TextView titleView = view.findViewById(android.R.id.title);
        titleView.setTextColor(iconColor);
    }

    private void initFont(View view, TypedArray typedArray) {
        String fontName = typedArray.getString(R.styleable.TextViewPlus_tvp_fontName);
        if (isNullOrEmpty(fontName))
            fontName = PATH_TO_FONT;
        font = Typeface.createFromAsset(getContext().getAssets(), fontName);
        TextView titleView = view.findViewById(android.R.id.title);
        TextView summaryView = view.findViewById(android.R.id.summary);
        titleView.setTypeface(font);
        if (summaryView != null)
            summaryView.setTypeface(font);

        new FontChangeCrawler(font).replaceFonts((ViewGroup) view);
    }
}
