package uk.sensoryunderload.infinilist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.List;

final class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.MyViewHolder> {

    private ListItem itemList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, content;
        public CheckBox flag;
        public ImageView hasChildrenImage;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            content = (TextView) view.findViewById(R.id.content);
            flag = (CheckBox) view.findViewById(R.id.rowStatus);
            hasChildrenImage = (ImageView) view.findViewById(R.id.rowHasChildren);
        }
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
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ListItem item = itemList.getChild(position);
        holder.title.setText(item.getTitle());
        holder.content.setText(item.getContent());
        // TODO: Populate status flag
        // TODO: Optionally hide hasChildren image:
//        if (item.hasChildren()) {
//            holder.hasChildrenImage.src = "";
//        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
