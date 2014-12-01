package net.pla1.sda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class ScheduleActivity extends Activity {
    private Context context;
    private ArrayList<Schedule> scheduleArrayListAll = new ArrayList<Schedule>();
    private ArrayList<Schedule> scheduleArrayListFiltered = new ArrayList<Schedule>();
    private ScheduleAdapter scheduleAdapter;
    private EditText filterField;
    private boolean nowPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_layout);
        context = this;
        setTitle("Schedule");
        ListView listView = (ListView) findViewById(R.id.listView);
        filterField = (EditText) findViewById(R.id.filterTextField);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        scheduleAdapter = new ScheduleAdapter(this, scheduleArrayListFiltered);
        listView.setAdapter(scheduleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Schedule schedule = scheduleArrayListFiltered.get(position);
                Log.i(Utils.TAG, "SELECTED: " + schedule);
                Intent intent = new Intent(context, ProgramActivity.class);
                intent.putExtra("programID", schedule.getProgramID());
                startActivity(intent);
            }
        });
        loadSchedule();
        listView.setTextFilterEnabled(true);
        filterField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                scheduleAdapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.schedule_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_now_playing) {
            nowPlaying = !nowPlaying;
            item.setChecked(nowPlaying);
            scheduleAdapter.getFilter().filter(filterField.getText());
            scheduleAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadSchedule() {
        DbUtils db = new DbUtils(context);
        scheduleArrayListAll.clear();
        scheduleArrayListAll.addAll(db.getSchedule());
        Collections.sort(scheduleArrayListAll);
        scheduleArrayListFiltered.addAll(scheduleArrayListAll);
        scheduleAdapter.notifyDataSetChanged();
        if (scheduleAdapter != null) {
            scheduleAdapter.getFilter().filter(filterField.getText());
        }
    }

    private class ScheduleAdapter extends ArrayAdapter<Schedule> implements Filterable {
        private ScheduleFilter filter;
        private ArrayList<Schedule> scheduleArrayList;

        public ScheduleAdapter(Activity context, ArrayList<Schedule> scheduleArrayList) {
            super(context, R.layout.schedule_row_layout, scheduleArrayList);
            this.scheduleArrayList = scheduleArrayList;
        }

        public Filter getFilter() {
            if (filter == null) {
                filter = new ScheduleFilter();
            }
            return filter;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final Schedule schedule = scheduleArrayListFiltered.get(position);
            final Program program = schedule.getProgram();
            final Station station = schedule.getStation();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.schedule_row_layout, parent, false);
            TextView titleTextView = (TextView) rowView.findViewById(R.id.title);
            TextView dateTextView = (TextView) rowView.findViewById(R.id.date);
            TextView channelTextView = (TextView) rowView.findViewById(R.id.channel);
            TextView nameTextView = (TextView) rowView.findViewById(R.id.name);
            TextView callsignTextView = (TextView) rowView.findViewById(R.id.callsign);
            String episodeTitle150 = program.getEpisodeTitle150();
            String title120 = program.getTitle120();
            StringBuilder sb = new StringBuilder();
            sb.append(title120);
            if (episodeTitle150 != null) {
                sb.append(" - ").append(episodeTitle150);
            }
            titleTextView.setText(sb.toString());
            dateTextView.setText(schedule.getAirDateTimeDisplay());
            channelTextView.setText(station.getChannelDisplay());
            nameTextView.setText(station.getName());
            if (station.getName() != null
                    && station.getCallsign() != null
                    && !station.getName().equals(station.getCallsign())) {
                callsignTextView.setText(station.getCallsign());
            }
            //  Log.i(Utils.TAG, "Has artwork :" + program.isHasImageArtwork());
            return rowView;
        }

        public int getCount() {
            setTitle(scheduleArrayList.size() + " programs");
            return scheduleArrayList.size();
        }

        private class ScheduleFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                String filterText = charSequence.toString().toLowerCase();
                ArrayList<Schedule> arrayList = new ArrayList<Schedule>();
                arrayList.addAll(search(scheduleArrayListAll, filterText));
                filterResults.count = arrayList.size();
                filterResults.values = arrayList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                ArrayList<Schedule> filteredResults = (ArrayList<Schedule>) filterResults.values;
                clear();
                if (filteredResults != null) {
                    addAll(filteredResults);
                    notifyDataSetChanged();
                }
            }

            private ArrayList<Schedule> search(ArrayList<Schedule> input, String s) {
                ArrayList<Schedule> output = new ArrayList<Schedule>();
                for (Schedule schedule : input) {
                    if (contains(schedule, s)) {
                        if (nowPlaying) {
                            if (schedule.isNowPlaying()) {
                                output.add(schedule);
                            }
                        } else {
                            output.add(schedule);
                        }
                    }
                }
                return output;
            }

            private boolean contains(Schedule schedule, String searchText) {
                Program program = schedule.getProgram();
                Station station = schedule.getStation();
                String title120 = lowerIfNotNull(program.getTitle120());
                String genres = lowerIfNotNull(program.getGenres());
                String episodeTitle150 = lowerIfNotNull(program.getEpisodeTitle150());
                String channel = lowerIfNotNull(station.getChannelDisplay());
                String name = lowerIfNotNull(station.getName());
                String callsign = lowerIfNotNull(station.getCallsign());
                if (title120.contains(searchText)
                        || episodeTitle150.contains(searchText)
                        || channel.contains(searchText)
                        || genres.contains(searchText)
                        || name.contains(searchText)
                        || callsign.contains(searchText)) {
                    return true;
                }
                return false;
            }

            private String lowerIfNotNull(String s) {
                if (s == null) {
                    return "";
                }
                return s.toLowerCase();
            }
        }
    }


}
