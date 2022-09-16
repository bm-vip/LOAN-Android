package ir.behrooz.loan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.Constants;

import static ir.behrooz.loan.common.StringUtil.isNullOrEmpty;

public class TextViewPlus extends TextView {
    private final static String PATH_TO_FONT = Constants.IRANSANS_LT;
    private final static int DEFAULT_ICON_SIZE = 19;
    private final static int DEFAULT_ICON_COLOR = Color.GRAY;
    private Typeface font;

    public TextViewPlus(Context context) {
        super(context);
        initialize(context);
    }

    public TextViewPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttributes(context, attrs);
    }

    public TextViewPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeAttributes(context, attrs);
    }

    private void initialize(Context context) {
        if (isInEditMode()) {
            return;
        }
        font = Typeface.createFromAsset(context.getAssets(), PATH_TO_FONT);
        setTypeface(font);
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
        setTypeface(font);
    }

    private void initializeAttributes(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextViewPlus);
        initFontName(context, typedArray);
        initText(typedArray);
        initSize(typedArray);
        initColor(typedArray);
        typedArray.recycle();
    }

    private void initText(TypedArray typedArray) {
        String text = typedArray.getString(R.styleable.TextViewPlus_tvp_text);

        if (text == null || text.isEmpty()) {
            return;
        }

        setText(text);
    }

    private void initSize(TypedArray typedArray) {
        int IconSize = typedArray.getInt(R.styleable.TextViewPlus_tvp_size, DEFAULT_ICON_SIZE);
        setTextSize(IconSize);
    }

    private void initColor(TypedArray typedArray) {
        int IconColor = typedArray.getColor(R.styleable.TextViewPlus_tvp_color, DEFAULT_ICON_COLOR);
        setTextColor(IconColor);
    }

    /**
     * sets  icon basing on String resources
     * Icons are created from -icons TTF font by Erik Flowers
     * Full icons reference can be found at: http://erikflowers.github.io/-icons/
     *
     * @param iconCode icon code located in res/values/strings.xml file
     */
    public void setIconResource(String iconCode) {
        setText(iconCode);
    }

    /**
     * sets icon size
     *
     * @param size icon size as an integer; default size is equal to 100
     */
    public void setIconSize(int size) {
        setTextSize(size);
    }

    /**
     * sets icon color resource
     *
     * @param colorResource color resource - eg. Color.RED or any custom color; default is Color.BLACK
     */
    public void setIconColor(int colorResource) {
        setTextColor(colorResource);
    }
}
