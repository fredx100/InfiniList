package uk.sensoryunderload.infinilist;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ListLayoutManager extends LinearLayoutManager {
    boolean mScrollBlocked;

    public ListLayoutManager(Context context) {
        super(context);
        mScrollBlocked = false;
    }

    @Override
    public boolean canScrollVertically() {
        return (!mScrollBlocked && super.canScrollVertically());
    }

    public void blockScrolling() {
        mScrollBlocked = true;
    }
    public void unblockScrolling() {
        mScrollBlocked = false;
    }
}
