package uk.sensoryunderload.infinilist;

import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.*;
import android.view.ContextMenu.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.content.Context;

import java.util.List;

final class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.MyViewHolder> {

    private ListItem itemList;

    public class MyViewHolder extends RecyclerView.ViewHolder
                              implements OnCreateContextMenuListener {
        public TextView title, content;
        public CheckBox flag;
        public ImageView hasChildrenImage;
        private ListItem item;
        private Context context; // TODO: can possibly purge once done with Toast

        public MyViewHolder(View view) {
            super(view);
            context = view.getContext();
            title = (TextView) view.findViewById(R.id.title);
            content = (TextView) view.findViewById(R.id.content);
            flag = (CheckBox) view.findViewById(R.id.rowStatus);
            hasChildrenImage = (ImageView) view.findViewById(R.id.rowHasChildren);
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
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
//            new ListItemAdapter().info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            MenuItem Delete = menu.add(Menu.NONE, 1, 1, "@string/delete");
            MenuItem AddSub = menu.add(Menu.NONE, 2, 2, "@string/addsub");
            Delete.setOnMenuItemClickListener(onContextMenu);
            AddSub.setOnMenuItemClickListener(onContextMenu);
        }

        private final MenuItem.OnMenuItemClickListener onContextMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1: // Delete
                        //Do stuff
//                        Toast.makeText(ListItemAdaptor.context, "Deleting position " + position,
                        item.
                        int position = getAdaptorPosition();
                        Toast.makeText(context, "Deleting position " + position,
                                Toast.LENGTH_LONG).show();
                        break;
                    case 2: // Add Subitem
                        //Do stuff
                        break;
                }
                return true;
            }
        };

        /* Approach gathered from android docs
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.delete:
                    deleteItem(info.id);
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }

        private void deleteItem(int id) {
            Toast.makeText(getApplicationContext(), id, Toast.LENGTH_SHORT).show();
        }
        */
    }

    public ListItemAdapter(ListItem itemList) {
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int pos) {
        holder.item = itemList.getChild(pos);

        holder.title.setText(holder.item.getTitle());
        holder.content.setText(holder.item.getContent());
        // TODO: Populate status flag
        // TODO: Optionally hide hasChildren image:
//        if (holder.item.hasChildren()) {
//            holder.hasChildrenImage.src = "";
//        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
