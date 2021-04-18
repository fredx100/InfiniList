package uk.sensoryunderload.infinilist;

import android.appwidget.AppWidgetManager;

public class ListContentProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        ...
    }

    @Override
    public Cursor query (Uri uri,
                         String[] projection,
                         Bundle queryArgs,
                         CancellationSignal cancellationSignal) {
        ListItem list = new ListItem();
        ListView.loadList ("Main.todo", list, context.getApplicationContext());

        MatrixCursor cursor = new MatrixCursor ("Flag", "Name", "SubItemCount");
        cur

        // Write the list to the textView
        final int listLength = list.size();
        ListItem child;
        ListItem.StatusFlag status = child.getStatus();
        for (int j = 0; j < listLength; ++j) {
            child = list.getChild(j);
            cursor.addRow(child.getStatusString(), child.getTitle(), child.size());
        }

        return cursor;
    }

    @Override
    public Uri insert (Uri uri, ContentValues values) {
        // notifyChange();
    }

    @Override
    public int update (Uri uri, ContentValues values, Bundle extras) {
        // notifyChange();
    }

    @Override
    public int delete (Uri uri, Bundle extras) {
        // notifyChange();
    }

    @Override
    public String getType (Uri uri) {
        //return "vnd.android.cursor.item/vnd.sensoryunderload.list" // For single records
        return "vnd.android.cursor.dir/vnd.sensoryunderload.list" // For multiple records
    }
}

