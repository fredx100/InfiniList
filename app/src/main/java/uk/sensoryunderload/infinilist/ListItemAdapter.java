package uk.sensoryunderload.infinilist;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

final class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ListItemViewHolder> {
    interface ListControlListener {
        void descend(int pos);
        void save();
        void move(int from, int to);
        void notifyStatusChange(int itemIndex);
        void startDrag(RecyclerView.ViewHolder viewHolder);
        MenuInflater getMainMenuInflater();
        ListItem getCopiedList();
    }

    ListItem itemList; // The list being displayed
    private int position; // The position of itemList within it's parent.
    private ListControlListener listControlListener;

    class ListItemViewHolder extends RecyclerView.ViewHolder
                             implements OnCreateContextMenuListener,
                                        StatusIndicator.StatusIndicatorListener {
        TextView title,
                 content,
                 subitems;
        StatusIndicator statusIndicator;
        LinearLayout row;
        private ListItem item;
        private ListControlListener listControlListener;

        ListItemViewHolder(View view, ListControlListener lCL) {
            super(view);
            this.listControlListener = lCL;
            title = view.findViewById(R.id.title);
            content = view.findViewById(R.id.content);
            subitems = view.findViewById(R.id.subitemCount);
            statusIndicator = view.findViewById(R.id.rowStatus);
            row = view.findViewById(R.id.row);

            view.setOnCreateContextMenuListener(this);
            statusIndicator.setStatusIndicatorListener(this);
        }

        private RecyclerView.ViewHolder getViewHolder() { return this; }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
          highlight();

          listControlListener.getMainMenuInflater().inflate(R.menu.context_menu, menu);

          menu.findItem(R.id.paste_into)
              .setEnabled(!listControlListener.getCopiedList().isEmpty());
        }

        // StatusIndicatorListener
        @Override
        public void incrementStatus() {
            item.getStatus().cycle();
            listControlListener.notifyStatusChange (getAdapterPosition());
            listControlListener.save();
        }
        @Override
        public StatusFlag getStatus() {
            if (item == null)
                return new StatusFlag();
            else
                return item.getStatus();
        }
        @Override
        public void startDrag() {
            listControlListener.startDrag(getViewHolder());
        }

        void setup() {
            setupText();
            setupSubitemCount();
            setupStatusIndicator();
        }

        private void setupText() {
            title.setText(item.getTitle());
            content.setText(item.getContent());
        }
        private void setupSubitemCount() {
            subitems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listControlListener.descend (getAdapterPosition());
                }
            });
            if (item.size() == 0) {
                subitems.setVisibility(View.GONE);
            } else {
                subitems.setVisibility(View.VISIBLE);
                subitems.setText(Integer.toString(item.size()));
            }
        }
        private void setupStatusIndicator() {
            statusIndicator.refresh();
        }

        public void highlight() {
            row.setBackgroundColor(itemView.getResources().getColor(R.color.colorItemSelectedBackground));
            title.setTextColor(itemView.getResources().getColor(R.color.colorItemSelectedStrong));
            content.setTextColor(itemView.getResources().getColor(R.color.colorItemSelectedWeak));
        }
        public void deHighlight() {
            row.setBackgroundColor(itemView.getResources().getColor(R.color.colorItemBackground));
            title.setTextColor(itemView.getResources().getColor(R.color.colorItemStrong));
            content.setTextColor(itemView.getResources().getColor(R.color.colorItemWeak));
        }
    }

    ListItemAdapter(ListItem itemList, ListControlListener lCL) {
        this.itemList = itemList;
        this.listControlListener = lCL;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_row, parent, false);

        return new ListItemViewHolder(itemView, listControlListener);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder holder, int pos) {
        holder.item = itemList.getChild(pos);

        holder.setup();

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                storePosition(holder.getAdapterPosition());
                return false;
            }
        });
    }

    private void storePosition(int pos) {
        position = pos;
    }
    int retrievePosition() {
        return position;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
