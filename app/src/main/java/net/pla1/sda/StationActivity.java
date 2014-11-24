package net.pla1.sda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

public class StationActivity extends Activity {
    private Context context;
    private ProgressBar progressBar;
    private TextView progressBarTextView;
    private ArrayList<Station> stations = new ArrayList<Station>();
    private String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.station_layout);
        setTitle("Stations");
        ListView listViewLineup = (ListView) findViewById(R.id.listViewStation);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarTextView = (TextView) findViewById(R.id.progressBarText);
        Intent intent = getIntent();
        uri = intent.getStringExtra("uri");
        getStations();
        ListView listViewStation = (ListView) findViewById(R.id.listViewStation);
        listViewStation.setAdapter(new StationAdapter(this));
    }

    private void getStations() {
        progressBarTextView.setText("Retrieving stations...");
        new AsyncTask<Void, Void, ArrayList<Station>>() {
            @Override
            protected ArrayList<Station> doInBackground(Void... params) {
                return Utils.getStations(context, uri);
            }

            @Override
            protected void onPostExecute(ArrayList<Station> arrayList) {
                stations.addAll(arrayList);
                progressBar.setVisibility(View.GONE);
                progressBarTextView.setText("");
                progressBarTextView.setVisibility(View.GONE);
            }
        }.execute(null, null, null);
    }

    private class StationAdapter extends ArrayAdapter<Station> {
        public StationAdapter(Activity context) {
            super(context, android.R.layout.simple_list_item_1, stations);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Station station = stations.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.station_row_layout, parent, false);

            TextView nameTextView = (TextView) rowView.findViewById(R.id.name);
            nameTextView.setText(station.getName());

            TextView channelTextView = (TextView) rowView.findViewById(R.id.channel);
            channelTextView.setText(station.getChannel());

            TextView callsignTextView = (TextView) rowView.findViewById(R.id.callsign);
            callsignTextView.setText(station.getCallsign());

            String logoUrl = station.getLogoUrl();
            if (Utils.isNotBlank(logoUrl)) {
                ImageView logoImageView = (ImageView) rowView.findViewById(R.id.logo);
                Bitmap bitmap = Utils.getImageFromDisk(context, station);
                if (bitmap == null) {
                   // Utils.saveImageToDisk(context, station);
                    new ImageDownloader(logoImageView).execute(station);
                } else {
                    logoImageView.setImageBitmap(bitmap);
                }
            }
            return rowView;
        }

        public int getCount() {
            setTitle(stations.size() + " stations for " + uri);
            return stations.size();
        }
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
                Utils.saveImageToDisk(context, mIcon, station[0]);
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
