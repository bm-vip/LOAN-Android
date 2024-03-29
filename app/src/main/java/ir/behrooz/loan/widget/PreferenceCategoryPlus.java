package ir.behrooz.loan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.Constants;

import static ir.behrooz.loan.common.StringUtil.isNullOrEmpty;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

public class PreferenceCategoryPlus extends PreferenceCategory {
    private final static String PATH_TO_FONT = Constants.IRANSANS_MD;
    private final static int DEFAULT_ICON_SIZE = 18;
    private final static int DEFAULT_ICON_COLOR = Color.GRAY;
    private Typeface font;
    AttributeSet attrs;


    public PreferenceCategoryPlus(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.attrs = attrs;
    }

    public PreferenceCategoryPlus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.attrs = attrs;
    }

    public PreferenceCategoryPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
    }

    public PreferenceCategoryPlus(Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        initializeAttributes(holder.itemView);
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
        if(DEFAULT_ICON_COLOR != DEFAULT_ICON_SIZE) {
            TextView titleView = view.findViewById(android.R.id.title);
            titleView.setTextSize(size);
        }
    }

    private void initColor(View view, TypedArray typedArray) {
        int iconColor = typedArray.getColor(R.styleable.TextViewPlus_tvp_color, DEFAULT_ICON_COLOR);
        if(DEFAULT_ICON_COLOR != iconColor) {
            TextView titleView = view.findViewById(android.R.id.title);
            titleView.setTextColor(iconColor);
        }
    }

    private void initFont(View view, TypedArray typedArray) {
        String fontName = typedArray.getString(R.styleable.TextViewPlus_tvp_fontName);
        if (isNullOrEmpty(fontName))
            fontName = PATH_TO_FONT;
        font = Typeface.createFromAsset(getContext().getAssets(), fontName);
        TextView titleView = view.findViewById(android.R.id.title);
        titleView.setTypeface(font);
    }
}
