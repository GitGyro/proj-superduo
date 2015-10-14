package it.jaschke.alexandria;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.services.BookService;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, Callback ,
        BookDetail.RemoveDeletedBookDetail {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReciever;

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    public static final Integer BARCODE_SCAN_RQ = 100;
    /*
      Need to move it to a common class for constants
      Multi-tracker activity will use it to stuff barcode in the bundle
     */
    public static final String BARCODE_KEY = "BARCODE";
    public static final String LAST_EAN_SCAN = "LAST_EAN_SCAN";
    public static final String CONTAINER_ID = "CONTAINER_ID";
    public static final String LAST_MENU_POS = "LAST_MENU_POS";
    public static final String LAST_BOOK_SELECTION = "LAST_BOOK_SELECTION";

    public static final Integer DISP_SINGLE_PANE =1;
    public static final Integer DISP_DUAL_PANE   =2;

    private int displayMode=DISP_SINGLE_PANE;
    FrameLayout layout;
    String lastScan="";
    Integer currPosition = -1;
    String selectedEan ="";
    /*
       matches index in menu selection
     */
    private String fragmentTags[] = {"BookList","AddBook","AboutApp","BookDetail"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();

       // Log.e(LOG_TAG,"onCreate");//10_13

        if(savedInstanceState!=null){
            lastScan =  savedInstanceState.getString(LAST_EAN_SCAN);
            currPosition =  savedInstanceState.getInt(LAST_MENU_POS);
            selectedEan =  savedInstanceState.getString(LAST_BOOK_SELECTION); //10_13
        }

        setContentView(R.layout.activity_main);
        layout = (FrameLayout)findViewById(R.id.right_container);
        /*
         * which layout has been selected for us, one vs two pane
         */
        if (layout != null){
            displayMode = DISP_DUAL_PANE;
        }
        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever,filter);

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }
    @Override
    public void onResume(){
        //Log.e(LOG_TAG, "Resuming MainActivity");
        super.onResume();
        if (currPosition != -1){
            onNavigationDrawerItemSelected(currPosition);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == BARCODE_SCAN_RQ){

            if (resultCode == Activity.RESULT_OK){
                lastScan = data.getStringExtra(BARCODE_KEY);

                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(this, BookService.class);
                bookIntent.putExtra(BookService.EAN, lastScan);
                bookIntent.setAction(BookService.FETCH_BOOK);
                startService(bookIntent);
                //Log.e(LOG_TAG,"Scan returned ISBN "+lastScan);
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;

        switch (position){
            default:
            case 0:
                nextFragment = new ListOfBooks();
                break;
            case 1:
                nextFragment = new AddBook();
                nullBookSelection();
                break;
            case 2:
                nextFragment = new About();
                nullBookSelection();
                break;

        }
        /*
         * Get rid of detail fragments
         */

        onItemSelected("");
        currPosition = position;
        if (layout != null){
            if (position == 0){
                layout.setVisibility(View.VISIBLE);
            }
            else {
                layout.setVisibility(View.GONE);
            }
        }

        Bundle args = new Bundle();
        args.putString(LAST_EAN_SCAN, lastScan);

        nextFragment.setArguments(args);
        lastScan = "";

        if (displayMode == DISP_DUAL_PANE) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, nextFragment, fragmentTags[position ])
                    .commit();
        }
        else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, nextFragment, fragmentTags[position ])
                    .addToBackStack((String) title) //TODO:: match TAG and title
                    .commit();

        }
        onItemSelected(selectedEan);
    }

    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        //Log.e("Debug>>","Destroying Activity");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }

    public void killDetailFragment(){
        selectedEan = "";
        onItemSelected("");
        onNavigationDrawerItemSelected(0);

    }

    public void nullBookSelection(){
        selectedEan = "";
    }
    @Override
    public void onItemSelected(String ean) {
        /*
         * delete detail fragment if no ean
         * Kind of a Kludge to cleanup detail fragment. Would be better off
         * with new interface.
         */

        if (ean.equals("")){
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentByTag(fragmentTags[3]); //TODO::
            if (fragment != null)
                getSupportFragmentManager().beginTransaction()
                        .remove(fragment)
                        .commit();
          return;
        }
        selectedEan=ean;
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();

        int id = R.id.container;
        if(findViewById(R.id.right_container) != null){
            id = R.id.right_container;
        }
        //Debug>>
        args.putInt(MainActivity.CONTAINER_ID, id);
                fragment.setArguments(args);

        FragmentManager fm = getSupportFragmentManager();
        /*
        for(int entry = 0; entry < fm.getBackStackEntryCount(); entry++){
            Log.e("Debug>>", "Found fragment: " + fm.getBackStackEntryAt(entry).getId());
        }
        */

        if (id == R.id.right_container) {

            getSupportFragmentManager().beginTransaction()
                    .replace(id, fragment, fragmentTags[3])  //TODO::
                    .commit();
        }
        else{
            getSupportFragmentManager().beginTransaction()
                    .replace(id, fragment)
                    .addToBackStack("Book Detail")
                    .commit();

        }
    }

    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                //Log.e(LOG_TAG,"Msg: "+intent.getStringExtra(MESSAGE_KEY));

                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void goBack(View view){
        //Log.e("Debug>>","__goBack__ Count "+getSupportFragmentManager().getBackStackEntryCount());
        selectedEan="";
        getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()<2){
            finish();
        }
        else{
            //Get rid of book detail fragment
            selectedEan ="";
            onItemSelected("");
        }
        super.onBackPressed();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LAST_EAN_SCAN, lastScan);
        outState.putString(LAST_BOOK_SELECTION, selectedEan); //10_13
        outState.putInt(LAST_MENU_POS, currPosition);
        super.onSaveInstanceState(outState);

    }
}
