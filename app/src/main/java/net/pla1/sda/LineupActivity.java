package net.pla1.sda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class LineupActivity extends Activity {
    private Context context;
    private ArrayAdapter<Lineup> lineupAdapter;
    private ArrayList<Lineup> subscribedLineups = new ArrayList<Lineup>();
    private ArrayList<Lineup> availableLineups = new ArrayList<Lineup>();
    private ProgressBar progressBar;
    private TextView progressBarTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lineup_layout);
        context = this;
        setTitle("Lineups");
        lineupAdapter = new LineupAdapter(this);
        ListView listViewLineup = (ListView) findViewById(R.id.listViewLineup);
        lineupAdapter.notifyDataSetChanged();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarTextView = (TextView) findViewById(R.id.progressBarText);
        getHeadends();
        listViewLineup.setAdapter(lineupAdapter);
        registerForContextMenu(listViewLineup);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = info.position;
        Lineup lineup = availableLineups.get(position);
        if (lineup.isSubscribed()) {
            menu.add(0, Menu.FIRST + 1, 0, "Un-subscribe");
        } else {
            menu.add(0, Menu.FIRST + 1, 0, "Subscribe");
        }
        if (lineup.isSubscribed()) {
            menu.add(0, Menu.FIRST + 2, 0, "List channels");
            menu.setHeaderTitle("iTV Device Menu");
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Lineup lineup = availableLineups.get(info.position);
        Log.i(Utils.TAG, "Line selected: " + lineup);
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                if (lineup.isSubscribed()) {
                    lineupAction("delete", lineup);
                } else {
                    lineupAction("put", lineup);
                }
                return true;
            case Menu.FIRST + 2:
                Intent intent = new Intent(this, StationActivity.class);
                intent.putExtra("uri", lineup.getUri());
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void getSubscribedLineups() {
        progressBarTextView.setText("Retrieving subscribed lineups...");
        new AsyncTask<Void, Void, ArrayList<Lineup>>() {
            @Override
            protected ArrayList<Lineup> doInBackground(Void... params) {
                return Utils.getLineups(context);
            }

            @Override
            protected void onPostExecute(ArrayList<Lineup> arrayList) {
                subscribedLineups.addAll(arrayList);
                setTitle(subscribedLineups.size() + " subscribed lineups");
                lineupAdapter.notifyDataSetChanged();
                setSubscribed();
                progressBar.setVisibility(View.GONE);
                progressBarTextView.setText("");
                progressBarTextView.setVisibility(View.GONE);
            }
        }.execute(null, null, null);
    }

    private void getHeadends() {
        progressBarTextView.setText("Retrieving headends...");
        new AsyncTask<Void, Void, ArrayList<Headend>>() {
            @Override
            protected ArrayList<Headend> doInBackground(Void... params) {
                return Utils.getHeadends(context);
            }

            @Override
            protected void onPostExecute(ArrayList<Headend> arrayList) {
                availableLineups.clear();
                for (Headend headend : arrayList) {
                    availableLineups.addAll(headend.getLineups());
                }
                lineupAdapter.notifyDataSetChanged();
                getSubscribedLineups();
            }
        }.execute(null, null, null);
    }


    private class LineupAdapter extends ArrayAdapter<Lineup> {

        public LineupAdapter(Activity context) {
            //   super(context, android.R.layout.simple_list_item_1, availableLineups);
            super(context, R.layout.lineup_row_layout, availableLineups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Lineup lineup = availableLineups.get(position);
            String uri = lineup.getUri();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.lineup_row_layout, parent, false);
/*
            final CheckBox checkbox = (CheckBox) rowView.findViewById(R.id.lineupCheckbox);
            checkbox.setChecked(subscribedLineups.contains(lineup));

            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkbox.isChecked()) {
                        lineupAction("put", lineup, checkbox);
                    } else {
                        lineupAction("delete", lineup, checkbox);
                    }
                }
            });
*/
            TextView nameTextView = (TextView) rowView.findViewById(R.id.name);
            nameTextView.setText(lineup.getName());

            TextView locationTextView = (TextView) rowView.findViewById(R.id.location);
            locationTextView.setText(lineup.getLocation());

            TextView typeTextView = (TextView) rowView.findViewById(R.id.type);
            typeTextView.setText(lineup.getType());

            TextView uriTextView = (TextView) rowView.findViewById(R.id.uri);
            uriTextView.setText(lineup.getUri());

            if (!lineup.isSubscribed()) {
                ImageView starred = (ImageView) rowView.findViewById(R.id.starred);
                starred.setVisibility(View.GONE);
            }
            return rowView;
        }


        @Override
        public int getCount() {
            setTitle(subscribedLineups.size() + " out of " + availableLineups.size() + " lineups subscribed");
            return availableLineups.size();
        }
    }

    private void setSubscribed() {
        for (Lineup lineup : availableLineups) {
            lineup.setSubscribed(subscribedLineups.contains(lineup));
        }
    }

    private void lineupAction(final String action, final Lineup lineup) {
        new AsyncTask<Void, Void, JsonObject>() {

            @Override
            protected JsonObject doInBackground(Void... voids) {
                return Utils.lineupAction(context, action, lineup.getUri());
            }

            @Override
            protected void onPostExecute(JsonObject jsonObject) {
                JsonElement codeElement = jsonObject.get("code");
                Log.i(Utils.TAG, "Code element: " + codeElement);
                if (codeElement != null && codeElement.getAsInt() > 0) {
                    JsonElement messageElement = jsonObject.get("message");
                    Toast.makeText(context, messageElement.getAsString(), Toast.LENGTH_LONG).show();
                } else {
                    if ("put".equals(action)) {
                        lineup.setSubscribed(true);
                        subscribedLineups.add(lineup);
                    }
                    if ("delete".equals(action)) {
                        lineup.setSubscribed(false);
                        subscribedLineups.remove(lineup);
                    }
                }
                lineupAdapter.notifyDataSetChanged();
            }
        }.execute(null, null, null);
    }
}
