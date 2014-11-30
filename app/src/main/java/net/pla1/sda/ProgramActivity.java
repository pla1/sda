package net.pla1.sda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStream;

public class ProgramActivity extends Activity {
    private Context context;
    private String programID;
    private LinearLayout layout;

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
        Program program = db.getProgram(programID);
        TextView titleTextView = (TextView) findViewById(R.id.title);
        TextView programIDTextView = (TextView) findViewById(R.id.programID);
        TextView castAndCrewTextView = (TextView) findViewById(R.id.castAndCrew);
        titleTextView.setText(program.getTitle120());
        if (Utils.isNotBlank(program.getDescription())) {
            TextView descriptionTextView = (TextView) findViewById(R.id.description);
            descriptionTextView.setText(program.getDescription());
        }
        programIDTextView.setText(programID);
        if (Utils.isNotBlank(program.getGenres())) {
            TextView genresTextView = (TextView) findViewById(R.id.genres);
            genresTextView.setText("Genres: " + program.getGenres());
        }
        if (program.getOriginalAirDate() != null) {
            TextView originalAirDateTextView = (TextView) findViewById(R.id.originalAirDate);
            originalAirDateTextView.setText(program.getOriginalAirDate().toString());
        }
        castAndCrewTextView.setText(program.getCastAndCrewDisplay());
        downloadMetadata();
    }

    private void downloadMetadata() {
        new AsyncTask<Void, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(Void... voids) {
                JsonArray jsonArray = Utils.downloadProgramMetadata(context, programID);
                if (jsonArray == null) {
                    return null;
                }
                int quantity = jsonArray.size();
                for (int i = 0; i < quantity; i++) {
                    JsonElement jsonElement = jsonArray.get(i);

                    Log.i(Utils.TAG, i + " Json Element: " + jsonElement);
                    if (i == 0) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        JsonElement uriElement = jsonObject.get("uri");
                        String uri = uriElement.getAsString();
                        try {
                            String url;
                            if (uri.startsWith("http")) {
                                url = uri;
                            } else {
                                url = "https://json.schedulesdirect.org/20140530/image/" + uri;
                            }
                            Log.i(Utils.TAG, "URL for image download: " + url);
                            InputStream in = new java.net.URL(url).openStream();
                            Bitmap mIcon = BitmapFactory.decodeStream(in);
                            mIcon = Bitmap.createScaledBitmap(mIcon, 800, 450, true);
                            mIcon = makeTransparent(mIcon, 30);
                            return new BitmapDrawable(getResources(), mIcon);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Drawable d) {


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

    class ImageDownloader extends AsyncTask<Station, Void, Bitmap> {
        ImageView bmImage;

        public ImageDownloader(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(Station... station) {
            String url = station[0].getLogoUrl();
            Bitmap mIcon = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon = BitmapFactory.decodeStream(in);
                mIcon = Bitmap.createScaledBitmap(mIcon, 100, 75, true);
                Utils.saveStationLogoToDisk(context, mIcon, station[0]);
            } catch (Exception e) {
                Log.i(Utils.TAG, e.getMessage());
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
