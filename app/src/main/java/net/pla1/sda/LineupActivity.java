package net.pla1.sda;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public class LineupActivity extends Activity {
    private Context context;
    private DbUtils db;
    private ArrayAdapter<Lineup> lineupAdapter;
    private ArrayList<Lineup> lineups = new ArrayList<Lineup>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lineup_layout);
        context = this;
        db = new DbUtils(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setTitle("Lineups");
        lineupAdapter = new LineupAdapter(this);
        ListView listViewLineup = (ListView) findViewById(R.id.listViewLineup);
        lineupAdapter.notifyDataSetChanged();
        getLineups();
        listViewLineup.setAdapter(lineupAdapter);

    }

    private void getLineups() {
        new AsyncTask<Void, Void, ArrayList<Lineup>>() {
            @Override
            protected ArrayList<Lineup> doInBackground(Void... params) {
                return Utils.getLineups(context);
            }

            @Override
            protected void onPostExecute(ArrayList<Lineup> arrayList) {
                lineups.addAll(arrayList);
                setTitle(lineups.size() + " lineups");
                lineupAdapter.notifyDataSetChanged();

            }
        }.execute(null, null, null);
    }

    private class LineupAdapter extends ArrayAdapter<Lineup> {

        public LineupAdapter(Activity context) {
            super(context, android.R.layout.simple_list_item_1, lineups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Lineup lineup = lineups.get(position);
            String uri = lineup.getUri();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.lineup_row_layout, parent, false);

            TextView nameTextView = (TextView) rowView.findViewById(R.id.name);
            nameTextView.setText(lineup.getName());

            TextView locationTextView = (TextView) rowView.findViewById(R.id.location);
            locationTextView.setText(lineup.getLocation());

            TextView typeTextView = (TextView) rowView.findViewById(R.id.type);
            nameTextView.setText(lineup.getType());

            return rowView;
        }

        private void lineupAction(final String action, final Lineup lineup, final CheckBox checkBox) {
            new AsyncTask<Void, Void, JsonObject>() {

                @Override
                protected JsonObject doInBackground(Void... voids) {
                    return Utils.lineupAction(context, action, lineup.getUri());
                }

                @Override
                protected void onPostExecute(JsonObject jsonObject) {

                }
            }.execute(null, null, null);
        }

        @Override
        public int getCount() {
            setTitle(lineups.size() + " lineups.");
            return lineups.size();
        }
    }
}
