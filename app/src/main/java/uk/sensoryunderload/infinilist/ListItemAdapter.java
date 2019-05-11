package uk.sensoryunderload.infinilist;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

final class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ListItemViewHolder> {
    public interface DescendClickListener {
        void descendClick(int pos);
    }

    public  ListItem itemList;
    private int position;
    private DescendClickListener descendClickListener;

    public class ListItemViewHolder extends RecyclerView.ViewHolder
                              implements OnCreateContextMenuListener {
//                              implements OnClickListener {
        public TextView title,
                        content,
                        subitems;
        public CheckBox flag;
        public ImageView descriptionIndicator;
        private ListItem item;
        private DescendClickListener descendClickListener;

        ListItemViewHolder(View view, DescendClickListener dCL) {
            super(view);
            this.descendClickListener = dCL;
            title = view.findViewById(R.id.title);
            content = view.findViewById(R.id.content);
            flag = view.findViewById(R.id.rowStatus);
            subitems = view.findViewById(R.id.subitemCount);
            descriptionIndicator = view.findViewById(R.id.descriptionIndicator);

//            view.setOnClickListener(this);
            view.setOnCreateContextMenuListener(this);
        }

//        @Override
//        public void onClick(View v) {
//            String location = name.getText().toString();
//            Intent goFlip = new Intent(RecyclerAdapter.context, FlipActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString("name", location);
//            bundle.putInt("pos", getAdapterPosition());
//            goFlip.putExtras(bundle);
//            context.startActivity(goFlip);
//        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(Menu.NONE, R.id.delete, Menu.NONE, R.string.item_delete);
            menu.add(Menu.NONE, R.id.addsub, Menu.NONE, R.string.item_addsub);
        }

        public void setup() {
            setupText();
            setupSubitemCount();
            setupDescriptionIndicator();
            // TODO: Populate status flag
        }

        public void setupText() {
            title.setText(item.getTitle());
            content.setText(item.getContent());
        }
        public void setupSubitemCount() {
            subitems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    descendClickListener.descendClick (getAdapterPosition());
                }
            });
            if (item.size() == 0) {
                subitems.setVisibility(View.GONE);
            } else {
                subitems.setVisibility(View.VISIBLE);
                subitems.setText(Integer.toString(item.size()));
            }
        }
        public void setupDescriptionIndicator() {
            if (item.getContent().length() == 0) {
                descriptionIndicator.setVisibility(View.INVISIBLE);
            } else if (content.getVisibility() == View.GONE) {
                descriptionIndicator.setVisibility(View.VISIBLE);
            } else {
                descriptionIndicator.setVisibility(View.GONE);
            }
        }
    }

    ListItemAdapter(ListItem itemList, DescendClickListener dCL) {
        this.itemList = itemList;
        this.descendClickListener = dCL;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_row, parent, false);

        return new ListItemViewHolder(itemView, descendClickListener);
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
