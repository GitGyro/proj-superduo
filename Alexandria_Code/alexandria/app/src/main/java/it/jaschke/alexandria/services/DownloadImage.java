package it.jaschke.alexandria.services;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;

/**
 * Created by saj on 11/01/15.
 */
public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    private Context context;

    public DownloadImage(Context context,ImageView bmImage) {
        this.context = context.getApplicationContext();
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urlDisplay = urls[0];
        Bitmap bookCover = null;
        try {
            InputStream in = new java.net.URL(urlDisplay).openStream();
            bookCover = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            /*
             * This message will stick around for a while, probably due to retries.
             */
            Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
            messageIntent.putExtra(MainActivity.MESSAGE_KEY, context.getResources().getString(R.string.network_error));
            LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);

           // e.printStackTrace();
        }
        return bookCover;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}

