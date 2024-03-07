package ir.behrooz.loan.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.core.widget.NestedScrollView;

public class NestedScrollViewPlus extends NestedScrollView { // Rename the class
    private ScrollViewListener scrollViewListener = null;

    public NestedScrollViewPlus(Context context) {
        super(context);
    }

    public NestedScrollViewPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NestedScrollViewPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    public interface ScrollViewListener {
        void onScrollChanged(NestedScrollViewPlus scrollView,
                             int x, int y, int oldx, int oldy);
    }
}
