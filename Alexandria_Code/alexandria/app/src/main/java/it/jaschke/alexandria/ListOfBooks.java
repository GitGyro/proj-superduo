package it.jaschke.alexandria;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private BookListAdapter bookListAdapter;
    private ListView bookList;
    private int position = ListView.INVALID_POSITION;
    private EditText searchText;

    private final int LOADER_ID = 10;

    public ListOfBooks() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }
    //Debug>>
    /*
    @Override
    public void onPrepareOptionsMenu(Menu menu){
        Log.e("Debug>>", "I am in options " + menu.size());
        for(int i =0; i< menu.size();i++){
            if (menu.getItem(i).getItemId() == R.id.action_share){
                Log.e("debug>>","list prepate Action share @ "+i);
            }
        }
             if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                 //menu.clear();

                // 'Find' the menu items that should not be displayed - each Fragment's menu has been contributed at this point.
                 MenuItem menuItem = menu.findItem(R.id.action_share);
                 if(menuItem != null)
                  menuItem.setVisible(false);

                 Log.e("Debug>>","I am in portait mode");

            }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.e("Debug>>", "Calling me");
        //menu.clear();//Debug>>
        for(int i =0; i< menu.size();i++){
            if (menu.getItem(i).getItemId() == R.id.action_share){
                Log.e("debug>>","create list Action share @ "+i);
            }
        }

    }
    */
    //10_13 refresh book list
    @Override
    public void onResume(){
        super.onResume();
        restartLoader();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );


        bookListAdapter = new BookListAdapter(getActivity(), cursor, 0);
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        searchText = (EditText) rootView.findViewById(R.id.searchText);
        rootView.findViewById(R.id.searchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListOfBooks.this.restartLoader();
                    }
                }
        );

        bookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        bookList.setAdapter(bookListAdapter);

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = bookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    //Log.e("Debug>> ", "Sz "+cursor.getCount()+" Position: "+position);
                    //Debug>> bookList.setItemChecked(position,true);
                    ((Callback)getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
                else {
                    Log.e("Debug>> ", "Null cursor");
                }
            }
        });

        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection = AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
        String searchString =searchText.getText().toString();

        if(searchString.length()>0){
            //Log.e("Debug>>", "Searching for "+searchString); //10_14
            searchString = "%"+searchString+"%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString,searchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookListAdapter.swapCursor(data);
        if (position != ListView.INVALID_POSITION) {
            bookList.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.books);
    }
}
