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

final class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.MyViewHolder> {

    private ListItem itemList;

    private int position;

    public class MyViewHolder extends RecyclerView.ViewHolder
                              implements OnCreateContextMenuListener {
//                              implements OnClickListener, OnCreateContextMenuListener {
        public TextView title, content;
        public CheckBox flag;
        public ImageView hasChildrenImage;
        private ListItem item;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            content = view.findViewById(R.id.content);
            flag = view.findViewById(R.id.rowStatus);
            hasChildrenImage = view.findViewById(R.id.rowHasChildren);

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
    }

    ListItemAdapter(ListItem itemList) {
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int pos) {
        holder.item = itemList.getChild(pos);

        holder.title.setText(holder.item.getTitle());
        holder.content.setText(holder.item.getContent());
        // TODO: Populate status flag
        // TODO: Optionally hide hasChildren image:
//        if (holder.item.hasChildren()) {
//            holder.hasChildrenImage.src = "";
//        }
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
