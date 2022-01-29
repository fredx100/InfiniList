// Cribbed from
// https://stackoverflow.com/a/26431126/885587

package uk.sensoryunderload.infinilist;

import androidx.appcompat.widget.AppCompatImageButton;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

class StatusIndicator extends AppCompatImageButton {

    interface StatusIndicatorListener {
        void incrementStatus();
        StatusFlag getStatus();
        void startDrag();
    }

    class GestureTouch extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mStatusIndicatorListener != null)
                mStatusIndicatorListener.incrementStatus();
            createDrawableState();
            return true;
        }

        @Override
        public boolean onScroll (MotionEvent e1,
                                 MotionEvent e2,
                                 float distanceX,
                                 float distanceY) {
            mStatusIndicatorListener.startDrag();
            return true;
        }
    }

    private StatusIndicatorListener mStatusIndicatorListener;
    private GestureDetector mTouchDetector;

    public StatusIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchDetector = new GestureDetector(context,new GestureTouch());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mTouchDetector.onTouchEvent(event);
        return true;
    }

    void refresh() { createDrawableState(); }

    private void createDrawableState() {
        if (mStatusIndicatorListener == null)
            setImageResource(R.drawable.ic_dragcheck_none);
        else {
            StatusFlag s = mStatusIndicatorListener.getStatus();

            if (s.isEqual(STATUS.NONE))
                setImageResource(R.drawable.ic_dragcheck_none);
            else if (s.isEqual(STATUS.SUCCESS))
              setImageResource(R.drawable.ic_dragcheck_success);
            else if (s.isEqual(STATUS.FAIL))
              setImageResource(R.drawable.ic_dragcheck_fail);
            else if (s.isEqual(STATUS.FLAG))
              setImageResource(R.drawable.ic_dragcheck_flag);
            else if (s.isEqual(STATUS.QUERY))
              setImageResource(R.drawable.ic_dragcheck_query);
        }
    }

    StatusIndicatorListener getStatusIndicatorListener() {
        return mStatusIndicatorListener;
    }

    void setStatusIndicatorListener(StatusIndicatorListener listener) {
        this.mStatusIndicatorListener = listener;
        createDrawableState();
    }
}
