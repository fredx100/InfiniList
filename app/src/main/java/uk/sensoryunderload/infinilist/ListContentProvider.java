package uk.sensoryunderload.infinilist;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import androidx.annotation.NonNull;

import java.util.ArrayList;

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
        ArrayList<Integer> widgetAddress = new ArrayList<Integer>();
        ListItem listTop = new ListItem();
        ListItem listWidget;
        Context context = getContext().getApplicationContext();
        ListView.loadSettings(context, widgetAddress);

        MatrixCursor cursor;
        String[] columnNames = {"Flag", "Name", "SubItemCount"};
        if (ListView.loadList("Main.todo", listTop, context)) {
            listWidget = listTop.goToAddress(widgetAddress);

            final int listLength = listWidget.size();
            cursor = new MatrixCursor(columnNames, listLength);

            // Write the list to the textView
            ListItem child;
            Object[] row = {null, null, null};
            for (int j = 0; j < listLength; ++j) {
                child = listWidget.getChild(j);
                row[0] = child.getStatusString();
                row[1] = child.getTitle();
                row[2] = child.size();
                cursor.addRow(row);
            }
        } else {
            cursor = new MatrixCursor(columnNames, 0);
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

