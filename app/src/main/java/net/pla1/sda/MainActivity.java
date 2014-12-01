package net.pla1.sda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends Activity {
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        context = this;
        DbUtils db = new DbUtils(context);
        int programRowQuantity = db.getTableCount(DbUtils.TABLE_PROGRAM);
        if (programRowQuantity > 0) {
            Intent intent = new Intent(context, ScheduleActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(context, LineupActivity.class);
            startActivity(intent);
        }
        if (Utils.isUserNameBlank(context)) {
            ViewGroup layout = (ViewGroup) findViewById(R.id.main_layout);
            TextView textView = new TextView(context);
            textView.setText("This app requires a Schedules Direct account. Set your user name, password and postal code in Settings.");
            textView.setTextSize(30);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            layout.addView(textView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "https://www.schedulesdirect.org/signup";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_status) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            checkStatus(sharedPreferences);
        }
        if (id == R.id.action_headends) {
            Intent intent = new Intent(context, HeadendActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_lineups) {
            Intent intent = new Intent(context, LineupActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_stations) {
            Intent intent = new Intent(context, StationActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_programs) {
            Intent intent = new Intent(context, ScheduleActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkStatus(final SharedPreferences sharedPreferences) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return Utils.getStatusFromServer(context);
            }

            @Override
            protected void onPostExecute(String message) {
                String accountStatus = message + " " + new Date();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.accountStatusKey), accountStatus);
                editor.apply();
                // Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, StatusActivity.class);
                startActivity(intent);
            }
        }.execute(null, null, null);
    }

    /*
        private void getHeadends(final SharedPreferences sharedPreferences) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    return Utils.getHeadends(context);
                }

                @Override
                protected void onPostExecute(String message) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            }.execute(null, null, null);
        }
    */
    private void getLineups(final SharedPreferences sharedPreferences) {
        new AsyncTask<Void, Void, ArrayList<Lineup>>() {
            @Override
            protected ArrayList<Lineup> doInBackground(Void... params) {
                return Utils.getLineups(context);
            }

            @Override
            protected void onPostExecute(ArrayList<Lineup> lineups) {
                Toast.makeText(context, lineups.size() + " lineups", Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }

}
