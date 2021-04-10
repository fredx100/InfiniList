package uk.sensoryunderload.infinilist;

import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class ListRecyclerView extends RecyclerView {
    ListLayoutManager mListLayoutManager;

    public ListRecyclerView(Context context) {
        super(context);
    }
    public ListRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ListRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // We override touch handling so the RecyclerView doesn't handle
    // anything which occurs over the StatusIndicator.
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mListLayoutManager.unblockScrolling();

            View child = findChildViewUnder(e.getX(), e.getY());
            if (child != null) {
                StatusIndicator statusInd = child.findViewById(R.id.rowStatus);
                if ((statusInd != null) &&
                    (e.getX() <= statusInd.getWidth())) {
                    mListLayoutManager.blockScrolling();
                }
            }
        }
        return super.onInterceptTouchEvent(e);
    }

    public void setListLayoutManager(ListLayoutManager lm) {
        mListLayoutManager = lm;
        setLayoutManager(mListLayoutManager);
    }
}
