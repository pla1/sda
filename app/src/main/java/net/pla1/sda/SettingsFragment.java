package net.pla1.sda;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private Context context;
    private String systemStatus;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //    dumpSharedPreferences();
        Log.i(Utils.TAG, "Add preferences from resource.");
        context = getActivity();
        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(Utils.TAG, "onSharedPreferenceChanged key: " + key + " value changed to: " + sharedPreferences.getString(key, ""));
        if (key.equals(getString(R.string.usernameKey)) || key.equals(getString(R.string.passwordKey))) {
            String userName = sharedPreferences.getString(getString(R.string.usernameKey), "");
            String password = sharedPreferences.getString(getString(R.string.passwordKey), "");
            sharedPreferences.edit().putString(getString(R.string.usernameKey), userName).commit();
            if (Utils.isNotBlank(userName) && Utils.isNotBlank(password)) {
                setToken(sharedPreferences);
                checkStatus(sharedPreferences);
                Toast.makeText(context, "System status: " + systemStatus, Toast.LENGTH_LONG);
                getHeadends(sharedPreferences);
            }
        }
    }

    private void setToken(final SharedPreferences sharedPreferences) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return Utils.setToken(context);
            }

            @Override
            protected void onPostExecute(String message) {
                String accountStatus = message + " " + new Date();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.accountStatusKey), accountStatus);
                editor.apply();
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
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
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }

    private void getHeadends(final SharedPreferences sharedPreferences) {
        new AsyncTask<Void, Void, ArrayList<Headend>>() {
            @Override
            protected ArrayList<Headend> doInBackground(Void... params) {
                return Utils.getHeadends(context);
            }

            @Override
            protected void onPostExecute(ArrayList<Headend> headends) {
                Toast.makeText(getActivity(), headends.size() + " headends.", Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        Log.i(Utils.TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        Log.i(Utils.TAG, "onPause");
    }

    private void dumpSharedPreferences() {
        Log.i(Utils.TAG, "Dumping shared preferences");
        Map<String, ?> keys = getPreferenceManager().getSharedPreferences().getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Log.i(Utils.TAG, "Shared Preferences Map key: " + entry.getKey() + " Map value: " + entry.getValue().toString());
        }
    }
}
