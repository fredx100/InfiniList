// Cribbed from
// https://stackoverflow.com/a/26431126/885587

package uk.sensoryunderload.infinilist;

import android.support.v7.widget.AppCompatImageButton;
import android.content.Context;
import android.util.AttributeSet;

class StatusIndicator extends AppCompatImageButton {

    interface StatusIndicatorListener {
        void incrementStatus();
        StatusFlag getStatus();
    }

    private StatusIndicatorListener mStatusIndicatorListener;

    public StatusIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        boolean success = true;
        if (mStatusIndicatorListener != null)
            mStatusIndicatorListener.incrementStatus();
        else
            success = false;
        createDrawableState();
        return success;
    }

    public void refresh() { createDrawableState(); }
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

    public StatusIndicatorListener getStatusIndicatorListener() {
        return mStatusIndicatorListener;
    }

    public void setStatusIndicatorListener(StatusIndicatorListener listener) {
        this.mStatusIndicatorListener = listener;
        createDrawableState();
    }
}
