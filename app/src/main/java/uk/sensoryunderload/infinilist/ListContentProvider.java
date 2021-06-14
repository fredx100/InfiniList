package uk.sensoryunderload.infinilist;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;

public class ListContentProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query (@NonNull Uri uri,
                         String[] projection,
                         String selection,
                         String[] selectionArgs,
                         String sortOrder) {
        ListItem list = new ListItem();
        ListView.loadList("Main.todo", list, getContext());
        list = list.goToAddress(widgetAddress);

        final int listLength = list.size();
        String[] columnNames = {"Flag", "Name", "SubItemCount"};
        MatrixCursor cursor = new MatrixCursor(columnNames, listLength);

        // Write the list to the textView
        ListItem child;
        Object[] row = {null, null, null};
        for (int j = 0; j < listLength; ++j) {
            child = list.getChild(j);
            row[0] = child.getStatusString();
            row[1] = child.getTitle();
            row[2] = child.size();
            cursor.addRow(row);
        }

        return cursor;
    }

    @Override
    public Uri insert (@NonNull Uri uri, ContentValues values) {
        // notifyChange();
        return Uri.EMPTY;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      String selection,
                      String[] selectionArgs) {
        // notifyChange();
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri,
                      ContentValues values,
                      String selection,
                      String[] selectionArgs) {
        // notifyChange();
        return 0;
    }

    @Override
    public String getType (@NonNull Uri uri) {
        //return "vnd.android.cursor.item/vnd.sensoryunderload.list" // For single records
        return "vnd.android.cursor.dir/vnd.sensoryunderload.list"; // For multiple records
    }
}

