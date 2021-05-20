package uk.sensoryunderload.infinilist;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.net.Uri;

public class ListWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate (Context context,
                          AppWidgetManager appWidgetManager,
                          int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        Uri uri = Uri.parse("content://uk.sensoryunderload.infinilist.ListContentProvider/widget");
        Cursor result = context.getContentResolver().query(uri, null, null, null, null);

        // Build the string
        if (N > 0) {
            final int M = result.getCount();
            StringBuilder sb = new StringBuilder();
            if (M > 0) {
                String nl = System.getProperty("line.separator");

                // Write the list to the textView
                for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                    sb.append(result.getString(0) + " " + result.getString(1));
                    int subitems = result.getInt(2);
                    if (subitems != 0) {
                        sb.append(" (" + subitems + ")");
                    }
                    sb.append(nl);
                }
            } else {
                sb.append("<list empty>");
            }

            // Perform this loop procedure for each App Widget that belongs to this provider
            for (int i = 0; i < N; i++) {
                int appWidgetId = appWidgetIds[i];

                // Get the layout for the App Widget
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.infinilist_appwidget);
                views.setTextViewText(R.id.widgetTextView, sb.toString());

                // Tell the AppWidgetManager to perform an update on the current app widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }
}
