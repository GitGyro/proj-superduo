package barqsoft.footballscores;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.service.myFetchService;
import barqsoft.footballscores.widgets.FootballScoresWidgetService;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link FootballScoresAppWidgetConfigureActivity FootballScoresAppWidgetConfigureActivity}
 */
public class FootballScoresAppWidget extends AppWidgetProvider {
    public static String ACTION_NEXT_DAY = "barqsoft.footballscores.widget.ACTION_NEXT_DAY";
    public static String ACTION_PREV_DAY = "barqsoft.footballscores.widget.ACTION_PREV_DAY";
    public static String ACTION_UPDATE_GAMES = "barqsoft.footballscores.widget.ACTION_UPDATE_GAMES";
    public static String ACTION_REFRESH_DAY = "barqsoft.footballscores.widget.ACTION_REFRESH_DAY";

    private static Integer whichDay = Utilies.BASE_TODAY_ID;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        requestScoreUpdate(context);
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            FootballScoresAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if(appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            Log.e("Debug>>","Invalid AppWidgetId "+appWidgetId+ "action "+action);
        }
        if (action.equals(ACTION_NEXT_DAY)) {

            if (whichDay < Utilies.MAX_DAY_INDEX) {
                whichDay++;

            //WARNING:: it is a global copy for the app widget, will be applicable to all widget instances

            Utilies.setWidgetDaySelection(whichDay);
            final AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
           /*
            * do not know how else to update day_name
            */
            updateAppWidget(ctx, mgr, appWidgetId);
           /*
            * Updates list. Updating widget above did not seem to update list.
            */
            final ComponentName cn = new ComponentName(ctx, FootballScoresAppWidget.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.score_list);

            }

        } else if (action.equals(ACTION_PREV_DAY)) {
            if (whichDay > 0) {
                whichDay--;

                //WARNING:: it is a global copy for the app widget, will be applicable to all widget instances
                Utilies.setWidgetDaySelection(whichDay);

                final AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
                updateAppWidget(ctx, mgr, appWidgetId);
                final ComponentName cn = new ComponentName(ctx, FootballScoresAppWidget.class);
                mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.score_list);
            }
        } else if (action.equals(ACTION_UPDATE_GAMES)) {
            // Download fresh data from the site
            requestScoreUpdate(ctx);
            /*
              * use alarm to request refersh of list
             */
            requestDelayedWidgetUpdate(ctx,appWidgetId);

            final AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);

            final ComponentName cn = new ComponentName(ctx, FootballScoresAppWidget.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.score_list);

        }
        else if (action.equals(ACTION_REFRESH_DAY)) {
        // refresh list
        //    Log.e("Debug>>", "<<<<  refresh selection >>>>");
        final AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);

        final ComponentName cn = new ComponentName(ctx, FootballScoresAppWidget.class);
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.score_list);

        }
        else if (action.equals("android.intent.action.MY_PACKAGE_REPLACED"))
        {
            Log.e("Debug>>", "<<<<  Package replaced >>>>");
        }
        super.onReceive(ctx, intent);
    }
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        Intent intent = new Intent(context, FootballScoresWidgetService.class);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.football_scores_app_widget);
        views.setRemoteAdapter(R.id.score_list,intent);
        views.setEmptyView(R.id.score_list, R.id.empty_view);
        views.setTextViewText(R.id.day_name, Utilies.getDayName(context));
        /*
          * Setup handlers for buttons
          */
        //
        // Handler for next day
        //
        final Intent refreshIntentNext = new Intent(context, FootballScoresAppWidget.class);
        refreshIntentNext.setAction(ACTION_NEXT_DAY);

        refreshIntentNext.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        refreshIntentNext.setData(Uri.parse(refreshIntentNext.toUri(Intent.URI_INTENT_SCHEME)));

        final PendingIntent refreshPendingIntentNext = PendingIntent.getBroadcast(context, 0,
                refreshIntentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.refresh_next, refreshPendingIntentNext);
        //
        // Handler for prev day
        //
        final Intent refreshIntentPrev = new Intent(context, FootballScoresAppWidget.class);
        refreshIntentPrev.setAction(ACTION_PREV_DAY);

        refreshIntentPrev.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        refreshIntentPrev.setData(Uri.parse(refreshIntentPrev.toUri(Intent.URI_INTENT_SCHEME)));

        final PendingIntent refreshPendingIntentPrev = PendingIntent.getBroadcast(context, 0,
                refreshIntentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.refresh_previous, refreshPendingIntentPrev);
        //
        // Handler for updating match results and scores
        //
        final Intent refreshIntentUpdateGames = new Intent(context, FootballScoresAppWidget.class);
        refreshIntentUpdateGames.setAction(ACTION_UPDATE_GAMES);

        refreshIntentUpdateGames.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        refreshIntentUpdateGames.setData(Uri.parse(refreshIntentUpdateGames.toUri(Intent.URI_INTENT_SCHEME)));

        final PendingIntent refreshPendingIntentUpdateGames = PendingIntent.getBroadcast(context, 0,
                refreshIntentUpdateGames, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.update_games, refreshPendingIntentUpdateGames);

        //
        // Instruct the widget manager to update the widget
        //
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    private void requestScoreUpdate(Context ctx){
        Intent service_start = new Intent(ctx, myFetchService.class);
        ctx.startService(service_start);

    }
    private void requestDelayedWidgetUpdate(Context context, int appWidgetId){
        final Intent intentDelayed = new Intent(context, FootballScoresAppWidget.class);
        intentDelayed.setAction(ACTION_REFRESH_DAY);

        intentDelayed.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intentDelayed.setData(Uri.parse(intentDelayed.toUri(Intent.URI_INTENT_SCHEME)));

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intentDelayed, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager almMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        almMgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);


    }
}

