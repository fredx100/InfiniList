package uk.sensoryunderload.infinilist;

import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

final class ListItemDetailsLookup extends ItemDetailsLookup<Long>{
  private final RecyclerView mRecyclerView;

  ListItemDetailsLookup(RecyclerView recyclerView) {
    mRecyclerView = recyclerView;
  }

  static final public class ListItemDetails extends ItemDetails<Long> {
    private final long mPosition;
    private final int  mStatusWidth;

    ListItemDetails(long pos, int statusWidth) {
      this.mPosition = pos;
      this.mStatusWidth = statusWidth;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ListItemDetails) {
        return (((ListItemDetails) obj).mPosition == mPosition);
      } else {
        return false;
      }
    }

    @Override
    public int getPosition() { return (int) mPosition; }

    public Long getSelectionKey() { return mPosition; }

    public boolean hasSelectionKey() { return true; }

    public boolean inDragRegion(MotionEvent e) {
      return (e.getX() <= mStatusWidth);
    }

    public boolean inSlectionHotspot(MotionEvent e) {
      return (e.getX() <= mStatusWidth);
    }
  }

  @Override
  public ItemDetails<Long> getItemDetails(MotionEvent e) {
    View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
    if (view != null) {
      int statusWidth = view.findViewById(R.id.rowStatus).getMeasuredWidth();
      RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
      if (holder instanceof ListItemAdapter.ListItemViewHolder) {
        return new ListItemDetails(((ListItemAdapter.ListItemViewHolder) holder).getItemId(), statusWidth);
      }
    }
    return null;
  }
}

