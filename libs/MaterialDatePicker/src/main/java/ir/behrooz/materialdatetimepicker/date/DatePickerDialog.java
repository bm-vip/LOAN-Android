/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.behrooz.materialdatetimepicker.date;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import ir.behrooz.materialdatetimepicker.HapticFeedbackController;
import ir.behrooz.materialdatetimepicker.R;
import ir.behrooz.materialdatetimepicker.TypefaceHelper;
import ir.behrooz.materialdatetimepicker.Utils;
import ir.behrooz.materialdatetimepicker.utils.LanguageUtils;
import ir.behrooz.materialdatetimepicker.utils.PersianCalendar;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog allowing users to select a date.
 */
public class DatePickerDialog extends DialogFragment implements
        OnClickListener, DatePickerController {

    private static final String TAG = "DatePickerDialog";

    private static final int UNINITIALIZED = -1;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int YEAR_VIEW = 1;

    private static final String KEY_SELECTED_YEAR = "year";
    private static final String KEY_SELECTED_MONTH = "month";
    private static final String KEY_SELECTED_DAY = "day";
    private static final String KEY_LIST_POSITION = "list_position";
    private static final String KEY_WEEK_START = "week_start";
    private static final String KEY_YEAR_START = "year_start";
    private static final String KEY_YEAR_END = "year_end";
    private static final String KEY_CURRENT_VIEW = "current_view";
    private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";
    private static final String KEY_MIN_DATE = "min_date";
    private static final String KEY_MAX_DATE = "max_date";
    private static final String KEY_HIGHLIGHTED_DAYS = "highlighted_days";
    private static final String KEY_SELECTABLE_DAYS = "selectable_days";
    private static final String KEY_THEME_DARK = "theme_dark";
    private static final String KEY_THEME_DARK_CHANGED = "theme_dark_changed";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_DISMISS = "dismiss";
    private static final String KEY_AUTO_DISMISS = "auto_dismiss";
    private static final String KEY_DEFAULT_VIEW = "default_view";
    private static final String KEY_TITLE = "title";
    private static final String KEY_OK_RESID = "ok_resid";
    private static final String KEY_OK_STRING = "ok_string";
    private static final String KEY_CANCEL_RESID = "cancel_resid";
    private static final String KEY_CANCEL_STRING = "cancel_string";


    private static final int DEFAULT_START_YEAR = 1390;
    private static final int DEFAULT_END_YEAR = 1900;

    private static final int ANIMATION_DURATION = 300;
    private static final int ANIMATION_DELAY = 500;

    private static SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());
    private static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.getDefault());

    private final PersianCalendar mPersianCalendar = new PersianCalendar();
    private OnDateSetListener mCallBack;
    private HashSet<OnDateChangedListener> mListeners = new HashSet<>();
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;

    private AccessibleDateAnimator mAnimator;

    private TextView mDayOfWeekView;
    private LinearLayout mMonthAndDayView;
    private TextView mSelectedMonthTextView;
    private TextView mSelectedDayTextView;
    private TextView mYearView;
    private DayPickerView mDayPickerView;
    private YearPickerView mYearPickerView;

    private int mCurrentView = UNINITIALIZED;

    private int mWeekStart = PersianCalendar.SATURDAY;
    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
    private String mTitle;
     private PersianCalendar mMinDate;
    private PersianCalendar mMaxDate;
    private PersianCalendar[] highlightedDays;
    private PersianCalendar[] selectableDays;
    private boolean mThemeDark = false;
    private boolean mThemeDarkChanged = false;
    private int mAccentColor = -1;
    private boolean mVibrate = true;
    private boolean mDismissOnPause = false;
    private boolean mAutoDismiss = false;
    private int mDefaultView = MONTH_AND_DAY_VIEW;
    private int mOkResid = R.string.mdtp_ok;
    private String mOkString;
    private int mCancelResid = R.string.mdtp_cancel;
    private String mCancelString;

    private HapticFeedbackController mHapticFeedbackController;

    private boolean mDelayAnimation = true;

    // Accessibility strings.
    private String mDayPickerDescription;
    private String mSelectDay;
    private String mYearPickerDescription;
    private String mSelectYear;

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {

        /**
         * @param view The view associated with this listener.
         * @param year The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility
         *            with {@link java.util.Calendar}.
         * @param dayOfMonth The day of the month that was set.
         */
        void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth);
    }

    /**
     * The callback used to notify other date picker components of a change in selected date.
     */
    public interface OnDateChangedListener {

        void onDateChanged();
    }


    public DatePickerDialog() {
        // Empty constructor required for dialog fragment.
    }

    /**
     * @param callBack How the parent is notified that the date is set.
     * @param year The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth The initial day of the dialog.
     */
    public static DatePickerDialog newInstance(OnDateSetListener callBack, int year,
            int monthOfYear, 
            int dayOfMonth) {
        DatePickerDialog ret = new DatePickerDialog();
        ret.initialize(callBack, year, monthOfYear, dayOfMonth);
        return ret;
    }

    public void initialize(OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        mCallBack = callBack;
        mPersianCalendar.setPersianDate(year, monthOfYear, dayOfMonth);
        mThemeDark = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (savedInstanceState != null) {
            mPersianCalendar.setPersianDate(
                    savedInstanceState.getInt(KEY_SELECTED_YEAR),
                    savedInstanceState.getInt(KEY_SELECTED_MONTH),
                    savedInstanceState.getInt(KEY_SELECTED_DAY)
            );
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_YEAR, mPersianCalendar.getPersianYear());
        outState.putInt(KEY_SELECTED_MONTH, mPersianCalendar.getPersianMonth());
        outState.putInt(KEY_SELECTED_DAY, mPersianCalendar.getPersianDay());
        outState.putInt(KEY_WEEK_START, mWeekStart);
        outState.putInt(KEY_YEAR_START, mMinYear);
        outState.putInt(KEY_YEAR_END, mMaxYear);
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView);
        int listPosition = -1;
        if (mCurrentView == MONTH_AND_DAY_VIEW) {
            listPosition = mDayPickerView.getMostVisiblePosition();
        } else if (mCurrentView == YEAR_VIEW) {
            listPosition = mYearPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
        }
        outState.putInt(KEY_LIST_POSITION, listPosition);
        outState.putSerializable(KEY_MIN_DATE, mMinDate);
        outState.putSerializable(KEY_MAX_DATE, mMaxDate);
        outState.putSerializable(KEY_HIGHLIGHTED_DAYS, highlightedDays);
        outState.putSerializable(KEY_SELECTABLE_DAYS, selectableDays);
        outState.putBoolean(KEY_THEME_DARK, mThemeDark);
        outState.putBoolean(KEY_THEME_DARK_CHANGED, mThemeDarkChanged);
        outState.putInt(KEY_ACCENT, mAccentColor);
        outState.putBoolean(KEY_VIBRATE, mVibrate);
        outState.putBoolean(KEY_DISMISS, mDismissOnPause);
        outState.putBoolean(KEY_AUTO_DISMISS, mAutoDismiss);
        outState.putInt(KEY_DEFAULT_VIEW, mDefaultView);
        outState.putString(KEY_TITLE, mTitle);
        outState.putInt(KEY_OK_RESID, mOkResid);
        outState.putString(KEY_OK_STRING, mOkString);
        outState.putInt(KEY_CANCEL_RESID, mCancelResid);
        outState.putString(KEY_CANCEL_STRING, mCancelString);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.mdtp_date_picker_dialog, null);

        mDayOfWeekView = (TextView) view.findViewById(R.id.date_picker_header);
        mMonthAndDayView = (LinearLayout) view.findViewById(R.id.date_picker_month_and_day);
        mMonthAndDayView.setOnClickListener(this);
        mSelectedMonthTextView = (TextView) view.findViewById(R.id.date_picker_month);
        mSelectedDayTextView = (TextView) view.findViewById(R.id.date_picker_day);
        mYearView = (TextView) view.findViewById(R.id.date_picker_year);
        mYearView.setOnClickListener(this);

        int listPosition = -1;
        int listPositionOffset = 0;
        int currentView = MONTH_AND_DAY_VIEW;
        if (savedInstanceState != null) {
            mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
            mMinYear = savedInstanceState.getInt(KEY_YEAR_START);
            mMaxYear = savedInstanceState.getInt(KEY_YEAR_END);
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
            listPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
            listPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET);
            mMinDate = (PersianCalendar)savedInstanceState.getSerializable(KEY_MIN_DATE);
            mMaxDate = (PersianCalendar)savedInstanceState.getSerializable(KEY_MAX_DATE);
            highlightedDays = (PersianCalendar[])savedInstanceState.getSerializable(KEY_HIGHLIGHTED_DAYS);
            selectableDays = (PersianCalendar[])savedInstanceState.getSerializable(KEY_SELECTABLE_DAYS);
            mThemeDark = savedInstanceState.getBoolean(KEY_THEME_DARK);
            mThemeDarkChanged = savedInstanceState.getBoolean(KEY_THEME_DARK_CHANGED);
            mAccentColor = savedInstanceState.getInt(KEY_ACCENT);
            mVibrate = savedInstanceState.getBoolean(KEY_VIBRATE);
            mDismissOnPause = savedInstanceState.getBoolean(KEY_DISMISS);
            mAutoDismiss = savedInstanceState.getBoolean(KEY_AUTO_DISMISS);
            mTitle = savedInstanceState.getString(KEY_TITLE);
            mOkResid = savedInstanceState.getInt(KEY_OK_RESID);
            mOkString = savedInstanceState.getString(KEY_OK_STRING);
            mCancelResid = savedInstanceState.getInt(KEY_CANCEL_RESID);
            mCancelString = savedInstanceState.getString(KEY_CANCEL_STRING);
        }

        final Activity activity = getActivity();
        mDayPickerView = new SimpleDayPickerView(activity, this);
        mYearPickerView = new YearPickerView(activity, this);

        // if theme mode has not been set by java code, check if it is specified in Style.xml
        if (!mThemeDarkChanged) {
            mThemeDark = Utils.isDarkTheme(activity, mThemeDark);
        }

        Resources res = getResources();
        mDayPickerDescription = res.getString(R.string.mdtp_day_picker_description);
        mSelectDay = res.getString(R.string.mdtp_select_day);
        mYearPickerDescription = res.getString(R.string.mdtp_year_picker_description);
        mSelectYear = res.getString(R.string.mdtp_select_year);

        int bgColorResource = mThemeDark ? R.color.mdtp_date_picker_view_animator_dark_theme : R.color.mdtp_date_picker_view_animator;
        view.setBackgroundColor(ContextCompat.getColor(activity, bgColorResource));

        mAnimator = (AccessibleDateAnimator) view.findViewById(R.id.animator);
        mAnimator.addView(mDayPickerView);
        mAnimator.addView(mYearPickerView);
        mAnimator.setDateMillis(mPersianCalendar.getTimeInMillis());
        // TODO: Replace with animation decided upon by the design team.
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIMATION_DURATION);
        mAnimator.setInAnimation(animation);
        // TODO: Replace with animation decided upon by the design team.
        Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(ANIMATION_DURATION);
        mAnimator.setOutAnimation(animation2);

        Button okButton = (Button) view.findViewById(R.id.ok);
        okButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                tryVibrate();
                if (mCallBack != null) {
                    mCallBack.onDateSet(DatePickerDialog.this, mPersianCalendar.getPersianYear(),
                            mPersianCalendar.getPersianMonth(), mPersianCalendar.getPersianDay());
                }
                dismiss();
            }
        });
        okButton.setTypeface(TypefaceHelper.get(activity,"Roboto-Medium"));
        if(mOkString != null) okButton.setText(mOkString);
        else okButton.setText(mOkResid);

        Button cancelButton = (Button) view.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tryVibrate();
                if(getDialog() != null) getDialog().cancel();
            }
        });
        cancelButton.setTypeface(TypefaceHelper.get(activity,"Roboto-Medium"));
        if(mCancelString != null) cancelButton.setText(mCancelString);
        else cancelButton.setText(mCancelResid);
        cancelButton.setVisibility(isCancelable() ? View.VISIBLE : View.GONE);

        // If an accent color has not been set manually, get it from the context
        if (mAccentColor == -1) {
            mAccentColor = Utils.getAccentColorFromThemeIfAvailable(getActivity());
        }
        if(mDayOfWeekView != null) mDayOfWeekView.setBackgroundColor(Utils.darkenColor(mAccentColor));
        view.findViewById(R.id.day_picker_selected_date_layout).setBackgroundColor(mAccentColor);
        okButton.setTextColor(mAccentColor);
        cancelButton.setTextColor(mAccentColor);

        if(getDialog() == null) {
            view.findViewById(R.id.done_background).setVisibility(View.GONE);
        }

        updateDisplay(false);
        setCurrentView(currentView);

        if (listPosition != -1) {
            if (currentView == MONTH_AND_DAY_VIEW) {
                mDayPickerView.postSetSelection(listPosition);
            } else if (currentView == YEAR_VIEW) {
                mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
            }
        }

        mHapticFeedbackController = new HapticFeedbackController(activity);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHapticFeedbackController.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHapticFeedbackController.stop();
        if(mDismissOnPause) dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if(mOnCancelListener != null) mOnCancelListener.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mOnDismissListener != null) mOnDismissListener.onDismiss(dialog);
    }

    private void setCurrentView(final int viewIndex) {

        switch (viewIndex) {
            case MONTH_AND_DAY_VIEW:
                ObjectAnimator pulseAnimator = Utils.getPulseAnimator(mMonthAndDayView, 0.9f,
                        1.05f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mDayPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(true);
                    mYearView.setSelected(false);
                    mAnimator.setDisplayedChild(MONTH_AND_DAY_VIEW);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                String dayString = LanguageUtils.getPersianNumbers(mPersianCalendar.getPersianLongDate());
                mAnimator.setContentDescription(mDayPickerDescription+": "+dayString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
                break;
            case YEAR_VIEW:
                pulseAnimator = Utils.getPulseAnimator(mYearView, 0.85f, 1.1f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mYearPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(false);
                    mYearView.setSelected(true);
                    mAnimator.setDisplayedChild(YEAR_VIEW);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                String yearString = LanguageUtils.
                        getPersianNumbers(String.valueOf(mPersianCalendar.getPersianYear()));
                mAnimator.setContentDescription(mYearPickerDescription+": "+yearString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
        }
    }

    private void updateDisplay(boolean announce) {
        if (mDayOfWeekView != null) {
            mDayOfWeekView.setText(mPersianCalendar.getPersianWeekDayName());
        }

        mSelectedMonthTextView.setText(LanguageUtils.
                getPersianNumbers(mPersianCalendar.getPersianMonthName()));
        mSelectedDayTextView.setText(LanguageUtils.
                getPersianNumbers(String.valueOf(mPersianCalendar.getPersianDay())));
        mYearView.setText(LanguageUtils.
                getPersianNumbers(String.valueOf(mPersianCalendar.getPersianYear())));

        // Accessibility.
        long millis = mPersianCalendar.getTimeInMillis();
        mAnimator.setDateMillis(millis);
        String monthAndDayText = LanguageUtils.getPersianNumbers(
                mPersianCalendar.getPersianMonthName() + " " +
                mPersianCalendar.getPersianDay()
        );
        mMonthAndDayView.setContentDescription(monthAndDayText);

        if (announce) {
            String fullDateText = LanguageUtils.
                    getPersianNumbers(mPersianCalendar.getPersianLongDate());
            Utils.tryAccessibilityAnnounce(mAnimator, fullDateText);
        }
    }

    /**
     * Set whether the device should vibrate when touching fields
     * @param vibrate true if the device should vibrate when touching a field
     */
    public void vibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    /**
     * Set whether the picker should dismiss itself when being paused or whether it should try to survive an orientation change
     * @param dismissOnPause true if the dialog should dismiss itself when it's pausing
     */
    public void dismissOnPause(boolean dismissOnPause) {
        mDismissOnPause = dismissOnPause;
    }

    /**
     * Set whether the picker should dismiss itself when a day is selected
     * @param autoDismiss true if the dialog should dismiss itself when a day is selected
     */
    @SuppressWarnings("unused")
    public void autoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
    }

    /**
     * Set whether the dark theme should be used
     * @param themeDark true if the dark theme should be used, false if the default theme should be used
     */
    public void setThemeDark(boolean themeDark) {
        mThemeDark = themeDark;
        mThemeDarkChanged = true;
    }

    /**
     * Returns true when the dark theme should be used
     * @return true if the dark theme should be used, false if the default theme should be used
     */
    @Override
    public boolean isThemeDark() {
        return mThemeDark;
    }

    /**
     * Set the accent color of this dialog
     * @param color the accent color you want
     */
    public void setAccentColor(String color) {
        try {
            mAccentColor = Color.parseColor(color);
        } catch(IllegalArgumentException e) {
            throw e;
        }
    }

    /**
     * Set the accent color of this dialog
     * @param color the accent color you want
     */
    public void setAccentColor(@ColorInt int color) {
        mAccentColor = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));;
    }

    /**
     * Get the accent color of this dialog
     * @return accent color
     */
    @Override
    public int getAccentColor() {
        return mAccentColor;
    }

    /**
     * Set whether the year picker of the month and day picker is shown first
     * @param yearPicker boolean
     */
    public void showYearPickerFirst(boolean yearPicker) {
        mDefaultView = yearPicker ? YEAR_VIEW : MONTH_AND_DAY_VIEW;
    }

    @SuppressWarnings("unused")
    public void setFirstDayOfWeek(int startOfWeek) {
        if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " +
                    "Calendar.SATURDAY");
        }
        mWeekStart = startOfWeek;
        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
    }

    @SuppressWarnings("unused")
    public void setYearRange(int startYear, int endYear) {
        if (endYear < startYear) {
            throw new IllegalArgumentException("Year end must be larger than or equal to year start");
        }

        mMinYear = startYear;
        mMaxYear = endYear;
        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
    }

    /**
     * Sets the minimal date supported by this DatePicker. Dates before (but not including) the
     * specified date will be disallowed from being selected.
     * @param calendar a Calendar object set to the year, month, day desired as the mindate.
     */
    @SuppressWarnings("unused")
    public void setMinDate(PersianCalendar calendar) {
        mMinDate = calendar;

        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
    }

    /**
     * @return The minimal date supported by this DatePicker. Null if it has not been set.
     */
    @Override
    public PersianCalendar getMinDate() {
        return mMinDate;
    }

    /**
     * Sets the minimal date supported by this DatePicker. Dates after (but not including) the
     * specified date will be disallowed from being selected.
     * @param calendar a Calendar object set to the year, month, day desired as the maxdate.
     */
    @SuppressWarnings("unused")
    public void setMaxDate(PersianCalendar calendar) {
        mMaxDate = calendar;

        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
    }

    /**
     * @return The maximal date supported by this DatePicker. Null if it has not been set.
     */
    @Override
    public PersianCalendar getMaxDate() {
        return mMaxDate;
    }

    @Override
    public boolean isOutOfRange(int year, int month, int day) {
        return false;
    }

    /**
     * Sets an array of dates which should be highlighted when the picker is drawn
     * @param highlightedDays an Array of Calendar objects containing the dates to be highlighted
     */
    @SuppressWarnings("unused")
    public void setHighlightedDays(PersianCalendar[] highlightedDays) {
        // Sort the array to optimize searching over it later on
        Arrays.sort(highlightedDays);
        this.highlightedDays = highlightedDays;
    }

    /**
     * @return The list of dates, as Calendar Objects, which should be highlighted. null is no dates should be highlighted
     */
    @Override
    public PersianCalendar[] getHighlightedDays() {
        return highlightedDays;
    }

    /**
     * Set's a list of days which are the only valid selections.
     * Setting this value will take precedence over using setMinDate() and setMaxDate()
     * @param selectableDays an Array of Calendar Objects containing the selectable dates
     */
    @SuppressWarnings("unused")
    public void setSelectableDays(PersianCalendar[] selectableDays) {
        // Sort the array to optimize searching over it later on
        Arrays.sort(selectableDays);
        this.selectableDays = selectableDays;
    }

    /**
     * @return an Array of Calendar objects containing the list with selectable items. null if no restriction is set
     */
    @Override
    public PersianCalendar[] getSelectableDays() {
        return selectableDays;
    }

    /**
     * Set a title to be displayed instead of the weekday
     * @param title String - The title to be displayed
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * Set the label for the Ok button (max 12 characters)
     * @param okString A literal String to be used as the Ok button label
     */
    @SuppressWarnings("unused")
    public void setOkText(String okString) {
        mOkString = okString;
    }

    /**
     * Set the label for the Ok button (max 12 characters)
     * @param okResid A resource ID to be used as the Ok button label
     */
    @SuppressWarnings("unused")
    public void setOkText(@StringRes int okResid) {
        mOkString = null;
        mOkResid = okResid;
    }

    /**
     * Set the label for the Cancel button (max 12 characters)
     * @param cancelString A literal String to be used as the Cancel button label
     */
    @SuppressWarnings("unused")
    public void setCancelText(String cancelString) {
        mCancelString = cancelString;
    }

    /**
     * Set the label for the Cancel button (max 12 characters)
     * @param cancelResid A resource ID to be used as the Cancel button label
     */
    @SuppressWarnings("unused")
    public void setCancelText(@StringRes int cancelResid) {
        mCancelString = null;
        mCancelResid = cancelResid;
    }

    @SuppressWarnings("unused")
    public void setOnDateSetListener(OnDateSetListener listener) {
        mCallBack = listener;
    }

    @SuppressWarnings("unused")
    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
    }

    @SuppressWarnings("unused")
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    // If the newly selected month / year does not contain the currently selected day number,
    // change the selected day number to the last day of the selected month or year.
    //      e.g. Switching from Mar to Apr when Mar 31 is selected -> Apr 30
    //      e.g. Switching from 2012 to 2013 when Feb 29, 2012 is selected -> Feb 28, 2013
    private void adjustDayInMonthIfNeeded(int month, int year) {
//        int day = mPersianCalendar.getPersianDay();
//        int daysInMonth = Utils.getDaysInMonth(month, year);
//        if (day > daysInMonth) {
//            mPersianCalendar.setPersianDate(Persian);
//        } TODO
    }

    @Override
    public void onClick(View v) {
        tryVibrate();
        if (v.getId() == R.id.date_picker_year) {
            setCurrentView(YEAR_VIEW);
        } else if (v.getId() == R.id.date_picker_month_and_day) {
            setCurrentView(MONTH_AND_DAY_VIEW);
        }
    }

    @Override
    public void onYearSelected(int year) {
        adjustDayInMonthIfNeeded(mPersianCalendar.getPersianMonth(), year);
        mPersianCalendar.setPersianDate(year, mPersianCalendar.getPersianMonth(),
                mPersianCalendar.getPersianDay());
        updatePickers();
        setCurrentView(MONTH_AND_DAY_VIEW);
        updateDisplay(true);
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        mPersianCalendar.setPersianDate(year, month, day);
        updatePickers();
        updateDisplay(true);
    }

    private void updatePickers() {
        for(OnDateChangedListener listener : mListeners) listener.onDateChanged();
    }


    @Override
    public MonthAdapter.CalendarDay getSelectedDay() {
        return new MonthAdapter.CalendarDay(mPersianCalendar);
    }

    @Override
    public int getMinYear() {
        if(selectableDays != null) return selectableDays[0].getPersianYear();
        // Ensure no years can be selected outside of the given minimum date
        return mMinDate != null && mMinDate.getPersianYear() > mMinYear ? mMinDate.getPersianYear() : mMinYear;
    }

    @Override
    public int getMaxYear() {
        if(selectableDays != null) return selectableDays[selectableDays.length-1].getPersianYear();
        // Ensure no years can be selected outside of the given maximum date
        return mMaxDate != null && mMaxDate.getPersianYear() < mMaxYear ? mMaxDate.getPersianYear() : mMaxYear;
    }

    @Override
    public int getFirstDayOfWeek() {
        return mWeekStart;
    }

    @Override
    public void registerOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void tryVibrate() {
        if(mVibrate) mHapticFeedbackController.tryVibrate();
    }

    public void notifyOnDateListener() {
        if (mCallBack != null) {
            mCallBack.onDateSet(DatePickerDialog.this, mPersianCalendar.getPersianYear(),
                    mPersianCalendar.getPersianMonth(), mPersianCalendar.getPersianDay());
        }
    }
}
