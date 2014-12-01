package net.pla1.sda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

public class StationActivity extends Activity {
    private Context context;
    private ProgressBar progressBar;
    private TextView progressBarTextView;
    private ArrayList<Station> stationsAll = new ArrayList<Station>();
    private ArrayList<Station> stationsFiltered = new ArrayList<Station>();
    private String uri;
    private DbUtils db;
    private StationAdapter stationAdapter;
    private boolean showSubscribedOnly = false;
    private Menu optionsMenu;
    private EditText filterField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.station_layout);
        setTitle("Stations");
        db = new DbUtils(context);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarTextView = (TextView) findViewById(R.id.progressBarText);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        filterField = (EditText) findViewById(R.id.filterTextField);
        filterField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                stationAdapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        Intent intent = getIntent();
        uri = intent.getStringExtra("uri");
        getStations();
        ListView listViewStation = (ListView) findViewById(R.id.listViewStation);
        stationAdapter = new StationAdapter(this, stationsFiltered);
        listViewStation.setAdapter(stationAdapter);
        listViewStation.setTextFilterEnabled(true);
        registerForContextMenu(listViewStation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        optionsMenu = menu;
        menu.add(0, 1, Menu.NONE, "Show subscribed only");
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.i(Utils.TAG, "onOptionsItemSelected id: " + id);
        if (id == 1) {
            optionsMenu.clear();
            if (showSubscribedOnly) {
                optionsMenu.add(0, 1, Menu.NONE, "Show subscribed only");
            } else {
                optionsMenu.add(0, 1, Menu.NONE, "Show all");
            }
            showSubscribedOnly = !showSubscribedOnly;
            stationAdapter.getFilter().filter(filterField.getText());
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = info.position;
        Station station = stationsFiltered.get(position);
        menu.setHeaderTitle("Station Menu");
        if (db.isSubscribedStation(station.getStationID())) {
            menu.add(0, Menu.FIRST + 1, 0, "Un-subscribe");
        } else {
            menu.add(0, Menu.FIRST + 1, 0, "Subscribe");
        }
        if (db.isSubscribedStation(station.getStationID())) {
            menu.add(0, Menu.FIRST + 2, 0, "List programs");
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Station station = stationsFiltered.get(info.position);
        Log.i(Utils.TAG, "Station selected: " + station);
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                if (db.isSubscribedStation(station.getStationID())) {
                    db.deleteStation(station.getStationID());
                } else {
                    db.updateOrInsertStation(station);
                    downloadPrograms(station);
                }
                stationAdapter.notifyDataSetChanged();
                return true;
            case Menu.FIRST + 2:
                downloadPrograms(station);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void downloadPrograms(final Station station) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.downloadSchedule(context, station);
                Utils.downloadPrograms(context, station);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(context, "Programs downloaded for station: " + station.getChannel() + " " + station.getName(), Toast.LENGTH_SHORT).show();
            }
        }.execute(null, null, null);
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
                stationsAll.addAll(arrayList);
                stationsFiltered.addAll(arrayList);
                progressBar.setVisibility(View.GONE);
                progressBarTextView.setText("");
                progressBarTextView.setVisibility(View.GONE);
                if (stationAdapter != null) {
                    stationAdapter.getFilter().filter(filterField.getText());
                }
            }
        }.execute(null, null, null);
    }


    private class StationAdapter extends ArrayAdapter<Station> implements Filterable {
        private Filter stationFilter;
        private ArrayList<Station> stations;

        public StationAdapter(Activity context, ArrayList<Station> stations) {
            super(context, android.R.layout.simple_list_item_1, stations);
            this.stations = stations;
        }

        @Override
        public Filter getFilter() {
            if (stationFilter == null) {
                stationFilter = new StationFilter();
            }
            return stationFilter;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Station station = stationsFiltered.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.station_row_layout, parent, false);


            ImageView starredImageView = (ImageView) rowView.findViewById(R.id.starred);
            if (!db.isSubscribedStation(station.getStationID())) {
                starredImageView.setVisibility(View.GONE);
            }

            TextView nameTextView = (TextView) rowView.findViewById(R.id.name);
            nameTextView.setText(station.getName());

            TextView channelTextView = (TextView) rowView.findViewById(R.id.channel);
            channelTextView.setText(station.getChannel());

            TextView callsignTextView = (TextView) rowView.findViewById(R.id.callsign);
            callsignTextView.setText(station.getCallsign());

            String logoUrl = station.getLogoUrl();
            if (Utils.isNotBlank(logoUrl)) {
                ImageView logoImageView = (ImageView) rowView.findViewById(R.id.logo);
                Bitmap bitmap = Utils.getStationLogoFromDisk(context, station);
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

        private class StationFilter extends Filter {
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                String filterText = charSequence.toString().toLowerCase();
                ArrayList<Station> arrayList = new ArrayList<Station>();
                if (showSubscribedOnly) {
                    for (Station station : stations) {
                        if (db.isSubscribedStation(station.getStationID())) {
                            if (contains(station, filterText)) {
                                arrayList.add(station);
                            }
                        }
                    }
                } else {
                    arrayList.addAll(search(stationsAll, filterText));
                }
                filterResults.count = arrayList.size();
                filterResults.values = arrayList;
                return filterResults;
            }

            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                ArrayList<Station> filteredResults = (ArrayList<Station>) filterResults.values;
                clear();
                if (filteredResults != null) {
                    addAll(filteredResults);
                    notifyDataSetChanged();
                }
            }
        }
    }

    private String lowerIfNotNull(String s) {
        if (s == null) {
            return "";
        }
        return s.toLowerCase();
    }

    private boolean contains(Station station, String searchText) {
        String stationID = lowerIfNotNull(station.getStationID());
        String name = lowerIfNotNull(station.getName());
        String callsign = lowerIfNotNull(station.getCallsign());
        String channel = lowerIfNotNull(station.getChannel());
        if (stationID.contains(searchText)
                || name.contains(searchText)
                || channel.contains(searchText)
                || callsign.contains(searchText)) {
            return true;
        }
        return false;
    }

    private ArrayList<Station> search(ArrayList<Station> input, String s) {
        ArrayList<Station> output = new ArrayList<Station>();
        for (Station station : input) {
            if (contains(station, s)) {
                output.add(station);
            }
        }
        return output;
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
