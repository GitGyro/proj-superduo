package barqsoft.footballscores.widgets;


import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.scoresAdapter;


/**
 * This is the service that provides the factory to be bound to the collection service.
 */
public class FootballScoresWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FootballScoresWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
/**
 * This is the factory that will provide data to the collection widget.
 */
class FootballScoresWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;
    private int mAppWidgetId;
    public FootballScoresWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }
    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), we do nothing here.
    }
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }
    public int getCount() {
        return mCursor.getCount();
    }
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.score_widget_list_item);

        if (mCursor.moveToPosition(position)) {
           // printRec();

            rv.setTextViewText(R.id.home_name, mCursor.getString(scoresAdapter.COL_HOME));
            rv.setTextViewText(R.id.away_name, mCursor.getString(scoresAdapter.COL_AWAY));

            rv.setTextViewText(R.id.data_textview, mCursor.getString(scoresAdapter.COL_MATCHTIME));
            rv.setTextViewText(R.id.score_textview,
                    Utilies.getScores(mCursor.getInt(scoresAdapter.COL_HOME_GOALS),
                            mCursor.getInt(scoresAdapter.COL_AWAY_GOALS)));
            rv.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(
                    mCursor.getString(scoresAdapter.COL_HOME)));

            rv.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(
                    mCursor.getString(scoresAdapter.COL_AWAY)
            ));


         }

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

        if (mCursor != null) {
            mCursor.close();
        }
       //Debug>>
       // mCursor = mContext.getContentResolver().query(DatabaseContract.BASE_CONTENT_URI, null, null,
       //         null, null);
        /*
         * Query data for the selected day only
         * TODO:: save selected day and appWidgetId to make it specific to each instance
         */
        String[] selectedDate = new String[1];
        Date fragmentdate = new Date(System.currentTimeMillis()+((Utilies.getWidgetDaySelection() -Utilies.BASE_TODAY_ID)*86400000));
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        selectedDate[0] = mformat.format(fragmentdate);
        mCursor = mContext.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null,
                selectedDate , null);
        /*
        //Debug>>
        if (mCursor != null){
            Log.e("Debug>>","onDataSetChanged OK Page = "+Utilies.getWidgetDaySelection()+" cnt= "+mCursor.getCount());
        }
        else{
            Log.e("Debug>>","onDataSetChanged !OK");
        }
        */
    }
    /* Debug>>
    private void printRec(){
        String home_name = mCursor.getString(scoresAdapter.COL_HOME);
        String away_name = mCursor.getString(scoresAdapter.COL_AWAY);
        String scores = Utilies.getScores(mCursor.getInt(scoresAdapter.COL_HOME_GOALS),
                mCursor.getInt(scoresAdapter.COL_AWAY_GOALS));
        String matchDate = mCursor.getString(scoresAdapter.COL_MATCHTIME);
        Log.e("Debug>>", home_name+":"+away_name+":"+scores+":"+matchDate);

    }
    */
}
