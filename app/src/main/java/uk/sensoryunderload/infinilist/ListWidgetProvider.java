package uk.sensoryunderload.infinilist;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

public class ListWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate (Context context,
                          AppWidgetManager appWidgetManager,
                          int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Build the string
        if (N > 0) {
            Uri uri = Uri.parse("content://uk.sensoryunderload.infinilist.ListContentProvider/widget");
            Cursor result = context.getContentResolver().query(uri, null, null, null, null);

            if (result != null) {
                final int M = result.getCount();
                StringBuilder sb = new StringBuilder();
                if (M > 0) {
                    String nl = System.getProperty("line.separator");

                    // Write the list to the textView
                    for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                        sb.append(result.getString(0)).append(" ").append(result.getString(1));
                        int subItems = result.getInt(2);
                        if (subItems != 0) {
                            sb.append(" (").append(subItems).append(")");
                        }
                        sb.append(nl);
                    }
                } else {
                    sb.append("<list empty>");
                }

                result.close();

                // Perform this loop procedure for each App Widget that belongs to this provider
                for (int appWidgetId : appWidgetIds) {
                    // Get the layout for the App Widget
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.infinilist_appwidget);
                    views.setTextViewText(R.id.widgetTextView, sb.toString());

                    // Tell the AppWidgetManager to perform an update on the current app widget
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        }
    }
}
