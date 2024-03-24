package ir.behrooz.loan.widget;

import static ir.behrooz.loan.common.StringUtil.isNullOrEmpty;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.Constants;

public class AppCompatSpinnerPlus extends AppCompatSpinner {
    private final static String PATH_TO_FONT = Constants.IRANSANS_LT;
    private final static int DEFAULT_ICON_SIZE = 19;
    private final static int DEFAULT_ICON_COLOR = Color.GRAY;
    private Typeface font;
    private int iconColor;
    private int iconSize;
    public AppCompatSpinnerPlus(@NonNull Context context) {
        super(context);
        initialize(context);
    }

    public AppCompatSpinnerPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttributes(context, attrs);
    }

    public AppCompatSpinnerPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeAttributes(context, attrs);
    }

    private void initialize(Context context) {
        if (isInEditMode()) {
            return;
        }
        font = Typeface.createFromAsset(context.getAssets(), PATH_TO_FONT);
    }
    private void initializeAttributes(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextViewPlus);
        initFontName(context, typedArray);
        initSize(typedArray);
        initColor(typedArray);
        typedArray.recycle();
    }
    private void initFontName(Context context, TypedArray typedArray) {
        if (isInEditMode()) {
            return;
        }
        String fontName = typedArray.getString(R.styleable.TextViewPlus_tvp_fontName);

        if (isNullOrEmpty(fontName)) {
            font = Typeface.createFromAsset(context.getAssets(), PATH_TO_FONT);
        } else {
            font = Typeface.createFromAsset(context.getAssets(), fontName);
        }
        //setTypeface(font);
    }
    private void initSize(TypedArray typedArray) {
        iconSize = typedArray.getInt(R.styleable.TextViewPlus_tvp_size, DEFAULT_ICON_SIZE);
        //setTextSize(iconSize);
    }

    private void initColor(TypedArray typedArray) {
          iconColor = typedArray.getColor(R.styleable.TextViewPlus_tvp_color, DEFAULT_ICON_COLOR);
//        setTextColor(iconColor);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (this.getAdapter() != null && this.getAdapter() instanceof SpinnerAdapter) {
            SpinnerAdapter adapter = (SpinnerAdapter) this.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                View view = adapter.getView(i, null, this);
                if (view instanceof TextView) {
                    TextView textView = ((TextView) view);
                    textView.setTypeface(font);
                    textView.setTextColor(iconColor);
                    textView.setTextSize(iconSize);
                }
            }
        }
    }
}
