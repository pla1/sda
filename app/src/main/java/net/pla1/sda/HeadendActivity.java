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
import android.widget.Toast;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public class HeadendActivity extends Activity {
    private Context context;
    private ArrayList<Headend> headends = new ArrayList<Headend>();
    private HeadendAdapter headendAdapter;
    private String postalCode;
    private DbUtils db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headend_layout);
        context = this;
        db = new DbUtils(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        postalCode = sharedPreferences.getString("postalCode", "");
        setTitle("Headends for postal code " + postalCode);
        headendAdapter = new HeadendAdapter(this);
        ListView listViewHeadend = (ListView) findViewById(R.id.listViewHeadend);
        headendAdapter.notifyDataSetChanged();
        getHeadends();
        listViewHeadend.setAdapter(headendAdapter);
    }

    private void getHeadends() {
        new AsyncTask<Void, Void, ArrayList<Headend>>() {
            @Override
            protected ArrayList<Headend> doInBackground(Void... params) {
                return Utils.getHeadends(context);
            }

            @Override
            protected void onPostExecute(ArrayList<Headend> arrayList) {
                headends.addAll(arrayList);
                setTitle(headends.size() + " headends for postal code " + postalCode);
                headendAdapter.notifyDataSetChanged();

            }
        }.execute(null, null, null);
    }

    private class HeadendAdapter extends ArrayAdapter<Headend> {

        public HeadendAdapter(Activity context) {
            super(context, android.R.layout.simple_list_item_1, headends);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Headend headend = headends.get(position);
            String uri = headend.getFirstLineupUri();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.headend_row_layout, parent, false);
            final CheckBox checkbox = (CheckBox) rowView.findViewById(R.id.headendCheckbox);
            checkbox.setChecked(db.getHeadend(uri).isFound());
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkbox.isChecked()) {
                        lineupAction("put", headend, checkbox);
                    } else {
                        lineupAction("delete", headend, checkbox);
                    }
                }
            });
            TextView typeTextView = (TextView) rowView.findViewById(R.id.type);
            TextView locationTextView = (TextView) rowView.findViewById(R.id.location);
            TextView nameTextView = (TextView) rowView.findViewById(R.id.name);
            TextView lineupNameTextView = (TextView) rowView.findViewById(R.id.lineupName);
            typeTextView.setText(headend.getType());
            locationTextView.setText(headend.getLocation());
            nameTextView.setText(headend.getName());
            ArrayList<Lineup> lineups = headend.getLineups();
            if (lineups.size() > 0) {
                Lineup lineup = lineups.get(0);
                lineupNameTextView.setText(lineup.getName());
            }
            return rowView;
        }

        private void lineupAction(final String action, final Headend headend, final CheckBox checkBox) {
            new AsyncTask<Void, Void, JsonObject>() {

                @Override
                protected JsonObject doInBackground(Void... voids) {
                    return Utils.lineupAction(context, action, headend.getFirstLineupUri());
                }

                @Override
                protected void onPostExecute(JsonObject jsonObject) {
                    if ("OK".equals(jsonObject.get("response"))) {
                        if ("put".equals(action)) {
                            db.updateOrInsert(headend);
                        } else {
                            db.delete(headend);
                        }
                    } else {
                        checkBox.setChecked(!checkBox.isChecked());
                        Toast.makeText(context, "Lineup action failed. Message: " + jsonObject.get("message"), Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(null, null, null);
        }

        @Override
        public int getCount() {
            setTitle(headends.size() + " headends for postal code " + postalCode);
            return headends.size();
        }
    }
}
