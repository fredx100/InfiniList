package uk.sensoryunderload.infinilist;

import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.RecyclerView;

// new ItemTouchHelper.Callback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

class ListCallback extends ItemTouchHelper.Callback {
    private int origFrom;
    private int to;
    private ListItemAdapter.ListControlListener listControlListener;

    ListCallback(ListItemAdapter.ListControlListener lcl) {
        origFrom = -1;
        listControlListener = lcl;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        int from = viewHolder.getAdapterPosition();
        to = target.getAdapterPosition();

        if (origFrom == -1)
            origFrom = from;

        if (from != to) {
            listControlListener.move(from, to);
            return true; // true if moved, false otherwise
        } else
            return false;
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (origFrom != to) {
            listControlListener.save();
            origFrom = -1;
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder vh, int amount) {}

    // Only start dragging on
    // ListControlListener.startDrag, not simply on a
    // long press (as the long press is needed for the
    // context menu anyway):
    @Override
    public boolean isLongPressDragEnabled() { return false; }
};
