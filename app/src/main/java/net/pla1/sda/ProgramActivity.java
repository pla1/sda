package net.pla1.sda;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStream;

public class ProgramActivity extends Activity {
    private Context context;
    private String programID;
    private LinearLayout layout;
    private Program program;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.program_layout);
        context = this;

        layout = (LinearLayout) findViewById(R.id.program_layout);
        setTitle("Program");
        Intent intent = getIntent();
        programID = intent.getStringExtra("programID");
        DbUtils db = new DbUtils(context);
        program = db.getProgram(programID);
        TextView titleTextView = (TextView) findViewById(R.id.title);
        TextView programIDTextView = (TextView) findViewById(R.id.programID);
        titleTextView.setText(program.getTitle120());
        if (Utils.isNotBlank(program.getDescription())) {
            TextView descriptionTextView = (TextView) findViewById(R.id.description);
            descriptionTextView.setText(program.getDescription());
        }
        programIDTextView.setText("Program ID: " + programID);
        if (Utils.isNotBlank(program.getGenres())) {
            TextView genresTextView = (TextView) findViewById(R.id.genres);
            genresTextView.setText("Genres: " + program.getGenres());
        }
        if (program.getOriginalAirDate() != null) {
            TextView originalAirDateTextView = (TextView) findViewById(R.id.originalAirDate);
            originalAirDateTextView.setText("Original air date: " + program.getOriginalAirDate().toString());
        }
        String castAndCrew = program.getCastAndCrewDisplay();
        if (Utils.isNotBlank(castAndCrew)) {
            TextView castAndCrewTextView = (TextView) findViewById(R.id.castAndCrew);
            castAndCrewTextView.setText(castAndCrew);
        }
        Bitmap bitmap = Utils.getProgramBackgroundImageFromDisk(context, programID);
        if (bitmap == null) {
            downloadBackgroundImage();
        } else {
            bitmap = makeTransparent(bitmap, 30);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            layout.setBackgroundColor(Color.WHITE);
            layout.setBackground(drawable);
        }
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    @Override
    protected void onStop() {
        unregisterReceiver(onComplete);
        super.onStop();
    }

    private void downloadBackgroundImage() {
        new AsyncTask<Void, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(Void... voids) {
                JsonArray jsonArray = Utils.downloadProgramMetadata(context, programID);
                if (jsonArray == null) {
                    return null;
                }
                int quantity = jsonArray.size();
                if (quantity > 0) {
                    JsonElement jsonElement = jsonArray.get(0);
                    Log.i(Utils.TAG, "Json Element: " + jsonElement);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    String uri = jsonObject.get("uri").getAsString();
                    int height = jsonObject.get("height").getAsInt();
                    int width = jsonObject.get("width").getAsInt();
                    Log.i(Utils.TAG, "URI: " + uri + " width: " + width + " height: " + height);
                    try {
                        String url;
                        if (uri.startsWith("http")) {
                            url = uri;
                        } else {
                            url = "https://json.schedulesdirect.org/20140530/image/" + uri;
                        }
                        Log.i(Utils.TAG, "URL for image download: " + url);
                        InputStream in = new java.net.URL(url).openStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(in);
                        Utils.saveProgramBackgroundImageToDisk(context, bitmap, programID);
                        bitmap = makeTransparent(bitmap, 30);
                        return new BitmapDrawable(getResources(), bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Drawable d) {
                layout.setBackgroundColor(Color.WHITE);
                layout.setBackground(d);
            }
        }.execute();
    }

    private Bitmap makeTransparent(Bitmap src, int value) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(transBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        final Paint paint = new Paint();
        paint.setAlpha(value);
        canvas.drawBitmap(src, 0, 0, paint);
        return transBitmap;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.program_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_download_assets) {
            saveProgramAssestsToPhotoAlbum();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProgramAssestsToPhotoAlbum() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // Utils.downloadProgramAssestsToPhotoAlbum(context, programID, program.getTitle120());
                downloadProgramAssestsToPhotoAlbum(context, programID, program.getTitle120());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void downloadProgramAssestsToPhotoAlbum(Context context, String programID, String title) {
        JsonArray jsonArray = Utils.downloadProgramMetadata(context, programID);
        if (jsonArray == null) {
            return;
        }
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        int quantity = jsonArray.size();
        Log.i(Utils.TAG, quantity + " assets to download for programID: " + programID + " title: " + title);
        String folderName = "/SDA/" + title.replaceAll(" ", "_");
        for (int i = 0; i < quantity; i++) {
            String uri = jsonArray.get(i).getAsJsonObject().get("uri").getAsString();
            String category = jsonArray.get(i).getAsJsonObject().get("category").getAsString();
            String fileName = category.replaceAll(" ", "_") + i + ".jpg";
            if (!uri.startsWith("http")) {
                uri = "https://json.schedulesdirect.org/20140530/image/" + uri;
            }
            Log.i(Utils.TAG, quantity + " assets to download for programID: " + programID + " title: " + title + " category: " + category + " URI: " + uri);
            Uri downloadUri = Uri.parse(uri);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(title)
                    .setDescription(title)
                    .setDestinationInExternalPublicDir(folderName, fileName);
            downloadManager.enqueue(request);
        }
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Download of program assets completed for title: " + program.getTitle120(), Toast.LENGTH_LONG).show();
        }
    };


}
