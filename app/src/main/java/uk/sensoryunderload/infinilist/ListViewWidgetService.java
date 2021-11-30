package uk.sensoryunderload.infinilist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.util.Log;

public class ListViewWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("INFLIST-LOG", "Creating new remoteViewsFactory");
        return new ListViewRemoteViewsFactory(this.getApplicationContext());
    }
}

class ListViewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final Uri URI = Uri.parse("content://uk.sensoryunderload.infinilist.ListContentProvider/widget");
    private final Context mContext;
    private Cursor mCursor;
    private int testInt;

    public ListViewRemoteViewsFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        mCursor = mContext.getContentResolver().query(URI, null, null, null, null);
        testInt = mCursor.getCount();
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews rv;
        int subItems = 0;

        rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item_view);

        if (mCursor.moveToPosition(position)) {
            rv.setTextViewText(R.id.widgetItemStatus, mCursor.getString(0));
            rv.setTextViewText(R.id.widgetItemTitle, mCursor.getString(1));
            subItems = mCursor.getInt(2);
            if (subItems != 0) {
                rv.setTextViewText(R.id.widgetSubitemCount, "(" + subItems + ")");
            } else {
                rv.setTextViewText(R.id.widgetSubitemCount, "");
            }
        }

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        mCursor = mContext.getContentResolver().query(URI, null, null, null, null);
        testInt = mCursor.getCount();
    }
}
