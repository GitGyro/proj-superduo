package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = "BookDetail";
    public static final String EAN_KEY = "EAN";
    private final int LOADER_ID = 11;
    private View rootView;
    private String ean;
    private String bookTitle;
    private ShareActionProvider shareActionProvider;


    private int displayMode = MainActivity.DISP_SINGLE_PANE;

    public BookDetail(){
    }
    public interface RemoveDeletedBookDetail {
        public void killDetailFragment();
        public void nullBookSelection();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int containerId;
        Bundle arguments = getArguments();
        if (arguments != null) {
            containerId = arguments.getInt(MainActivity.CONTAINER_ID);
            if (containerId == R.id.right_container) {
                displayMode = MainActivity.DISP_DUAL_PANE;
            }
            ean = arguments.getString(BookDetail.EAN_KEY);
        }
        else{
            Log.e(LOG_TAG,"Null arg");
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);

                if (displayMode == MainActivity.DISP_SINGLE_PANE) {
                    ((RemoveDeletedBookDetail) getActivity()).nullBookSelection();
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    ((RemoveDeletedBookDetail) getActivity()).killDetailFragment();
                }

            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        /*
         * BUGFIX::
         * safe to call restart loader here since we know onCreateOptionsMenu
         * is called before this point.
         * This resolves the issue of shareActionProvider being null
         */
        getLoaderManager().restartLoader(LOADER_ID, null, this);

    }
        @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

    }
    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            Log.e(LOG_TAG, "!!! ... No Data ...!!!!");
            // make the entire full book info disappear
            // Use the bew empty text view to display message
            ((LinearLayout) rootView.findViewById(R.id.full_book_layout)).setVisibility(View.INVISIBLE);
            ((TextView) rootView.findViewById(R.id.empty_view)).setVisibility(View.VISIBLE);
            /*
            It disables the action but still visible on actionbar menu
            if (shareActionProvider != null)
                shareActionProvider.setShareIntent(null);
            */
            return;
        }
        ((TextView) rootView.findViewById(R.id.empty_view)).setVisibility(View.GONE);

        bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText(bookTitle);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
        //To catch if our fix did not work and avoid crash
        if (shareActionProvider != null)
          shareActionProvider.setShareIntent(shareIntent);
        else {
            Log.e("Debug>>", "Null intent "+bookTitle);
        }

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText(desc);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        /*
         * BUGFIX::
         * Check for being null
         */
        if (authors == null){
            authors = "Unknown Author";
        }
        String[] authorsArr = authors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage(getContext(),(ImageView) rootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
            rootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onPause() {
        super.onPause();

    }

}