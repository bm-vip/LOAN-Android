package ir.behrooz.loan.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ScrollViewPlus extends ScrollView {
    private ScrollViewListener scrollViewListener = null;
    public ScrollViewPlus(Context context) {
        super(context);
    }

    public ScrollViewPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollViewPlus(Context context, AttributeSet attrs) {
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
        void onScrollChanged(ScrollViewPlus scrollView,
                             int x, int y, int oldx, int oldy);
    }
}


