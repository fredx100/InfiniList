package uk.sensoryunderload.infinilist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class ListViewWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListViewRemoteViewsFactory(this.getApplicationContext());
    }
}

class ListViewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context mContext;
    private Cursor mCursor;

    public ListViewRemoteViewsFactory(Context context) {
        mContext = context;
    }

    public void onCreate() {
        Uri uri = Uri.parse("content://uk.sensoryunderload.infinilist.ListContentProvider/widget");
        mCursor = mContext.getContentResolver().query(uri, null, null, null, null);
    }

    public void onDestroy() {
        mCursor.close();
    }

    public int getCount() {
//        return mCursor.getCount();
        return 4;
    }

    public RemoteViews getViewAt(int position) {
        RemoteViews rv;
        int subItems;
//        if (mCursor.moveToPosition(position)) {
            rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item_view);
//            rv.setTextViewText(R.id.widgetItemStatus, mCursor.getString(0));
//            rv.setTextViewText(R.id.widgetItemTitle, mCursor.getString(1));
//            subItems = mCursor.getInt(2);
            rv.setTextViewText(R.id.widgetItemStatus, "v");
            rv.setTextViewText(R.id.widgetItemTitle, "Oh, here we go...");
            subItems = (position % 3);
            if (subItems != 0) {
                rv.setTextViewText(R.id.widgetSubitemCount, "(" + subItems + ")");
            }
//        }

        return rv;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
    }
}
