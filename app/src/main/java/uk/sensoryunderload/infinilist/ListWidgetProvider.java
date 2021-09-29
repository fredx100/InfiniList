package uk.sensoryunderload.infinilist;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.util.Log;

public class ListWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate (Context context,
                          AppWidgetManager appWidgetManager,
                          int[] appWidgetIds) {
        // Attach the remote adaptor to each of the widgets
        for (int appWidgetId : appWidgetIds) {
            Log.d("INFLIST-LOG", "Updating widget " + appWidgetId);
            // Create the intent which references the list widget
            // service.
            Intent intent = new Intent(context, ListViewWidgetService.class);
            RemoteViews rv = new RemoteViews(context.getPackageName(),
                                             R.layout.infinilist_appwidget);
            rv.setRemoteAdapter(R.id.widgetListView, intent);

//            // Attach the OPEN_LIST_ACTION (via an intent) to the list
//            // view.
//            Intent openIntent = new Intent(context, ListView.class);
//            openIntent.setAction(ListView.OPEN_LIST_ACTION);
//            PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0,
//                                                                        openIntent, 0);
//            rv.setOnClickPendingIntent(R.id.widgetListView, openPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }
}

