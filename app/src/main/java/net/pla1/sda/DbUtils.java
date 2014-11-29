package net.pla1.sda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DbUtils extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 13;
    private Context context;
    private static final String TABLE_LINEUP = "lineup";
    private static final String[] COLUMNS_LINEUP = {"name", "type", "location", "uri"};

    private static final String TABLE_HEADEND = "headend";
    private static final String[] COLUMNS_HEADEND = {"name", "type", "location", "uri"};

    private static final String TABLE_STATION = "station";
    private static final String[] COLUMNS_STATION = {"stationID", "md5"};

    private static final String TABLE_SCHEDULE = "schedule";
    private static final String[] COLUMNS_SCHEDULE = {"stationID", "programID", "md5", "airDateTime", "duration", "liveTapeDelay", "newShowing"};

    private static final String TABLE_PROGRAM = "program";
    private static final String[] COLUMNS_PROGRAM = {"programID", "md5", "showType", "duration", "episodeTitle150", "title120", "originalAirDate"};


    public DbUtils(Context context) {
        super(context, context.getPackageName(), null, DATABASE_VERSION);
        this.context = context;
        Log.i(Utils.TAG, "DbUtils version is: " + DATABASE_VERSION + " data name built from package name is: " + context.getPackageName() + " this.getDatabaseName() returns: " + this.getDatabaseName());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(Utils.TAG, "DbUtils onCreate");
        db.execSQL(getTableCreateStatement(TABLE_HEADEND, COLUMNS_HEADEND));
        db.execSQL(getTableCreateStatement(TABLE_LINEUP, COLUMNS_LINEUP));
        db.execSQL(getTableCreateStatement(TABLE_STATION, COLUMNS_STATION));
        db.execSQL(getTableCreateStatement(TABLE_SCHEDULE, COLUMNS_SCHEDULE));
        db.execSQL(getTableCreateStatement(TABLE_PROGRAM, COLUMNS_PROGRAM));
    }

    private String getTableCreateStatement(String tableName, String[] columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("create table ");
        sb.append(tableName);
        sb.append(" (");
        int columnQuantity = columns.length;
        String comma = "";
        for (int i = 0; i < columnQuantity; i++) {
            sb.append(comma);
            comma = ",";
            sb.append(columns[i]);
            sb.append(" text");
        }
        sb.append(")");
        Log.i(Utils.TAG, "Create table SQL statement: " + sb.toString());
        return sb.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(Utils.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("drop table if exists " + TABLE_HEADEND);
        db.execSQL("drop table if exists " + TABLE_LINEUP);
        db.execSQL("drop table if exists " + TABLE_STATION);
        db.execSQL("drop table if exists " + TABLE_SCHEDULE);
        db.execSQL("drop table if exists " + TABLE_PROGRAM);
        onCreate(db);
    }

    public Headend getHeadend(String uri) {
        Headend headend = new Headend();
        SQLiteDatabase db = getWritableDatabase();
        String[] arguments = {uri};
        Cursor cursor = db.query(TABLE_HEADEND, COLUMNS_HEADEND, "uri=?", arguments, null, null, null);
        if (cursor.moveToFirst()) {
            int i = 0;
            headend.setName(cursor.getString(i++));
            headend.setType(cursor.getString(i++));
            headend.setLocation(cursor.getString(i++));
            Lineup lineup = new Lineup();
            lineup.setUri(cursor.getString(i++));
            ArrayList<Lineup> lineups = new ArrayList<Lineup>();
            lineups.add(lineup);
            headend.setLineups(lineups);
            headend.setFound(true);
        }
        cursor.close();
        Log.i(Utils.TAG, "getHeadend " + uri);
        return headend;
    }

    public void delete(Headend headend) {
        SQLiteDatabase db = getWritableDatabase();
        String[] arguments = {headend.getFirstLineupUri()};
        db.delete(TABLE_HEADEND, "uri = ?", arguments);
    }

    public void updateOrInsert(Headend headend) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = transfer(headend);
        String[] arguments = {headend.getFirstLineupUri()};
        int rowsUpdated = db.update(TABLE_HEADEND, contentValues, "uri=?", arguments);
        if (rowsUpdated == 0) {
            long rowId = db.insert(TABLE_HEADEND, null, contentValues);
        }
    }

    public boolean isSubscribedStation(String stationId) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"stationID"};
        String[] arguments = {stationId};
        Cursor cursor = db.query(TABLE_STATION, columns, "stationID=?", arguments, null, null, null);
        boolean subscribed = cursor.moveToFirst();
        //  Log.i(Utils.TAG, "Subscribed to " + stationId + " " + subscribed);
        cursor.close();
        return subscribed;
    }

    public void deleteStation(String stationID) {
        SQLiteDatabase db = getWritableDatabase();
        String[] arguments = {stationID};
        int rowsDeleted = db.delete(TABLE_STATION, "stationID=?", arguments);
        Log.i(Utils.TAG, rowsDeleted + " stations deleted.");
    }

    private ContentValues transfer(Schedule schedule) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("stationID", schedule.getStationID());
        contentValues.put("programID", schedule.getProgramID());
        contentValues.put("duration", schedule.getDuration());
        contentValues.put("liveTapeDelay", schedule.getLiveTapeDelay());
        contentValues.put("md5", schedule.getMd5());
        contentValues.put("airDateTime", schedule.getAirDateTime().getTime());
        contentValues.put("newShowing", schedule.isNewShowing());
        return contentValues;
    }

    private int getTableCount(String tableName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + tableName, null);
        int quantity = 0;
        if (cursor.moveToFirst()) {
            quantity = cursor.getInt(0);
        }
        db.close();
        return quantity;
    }

    public String getTableCounts() {
        StringBuilder sb = new StringBuilder();
        sb.append(TABLE_SCHEDULE).append(" table quantity: ").append(getTableCount(TABLE_SCHEDULE)).append("\n");
        sb.append(TABLE_STATION).append(" table quantity: ").append(getTableCount(TABLE_STATION)).append("\n");
        sb.append(TABLE_PROGRAM).append(" table quantity: ").append(getTableCount(TABLE_PROGRAM)).append("\n");
        return sb.toString();
    }

    public void storeSchedule(String stationID, ArrayList<Schedule> schedules) {
        SQLiteDatabase db = getWritableDatabase();
        String[] arguments = {stationID};
        int quantity = db.delete(TABLE_SCHEDULE, "stationID=?", arguments);
        Log.i(Utils.TAG, quantity + " rows deleted from schedule table.");
        for (Schedule schedule : schedules) {
            ContentValues contentValues = transfer(schedule);
            contentValues.put("stationID", stationID);
            db.insert(TABLE_SCHEDULE, null, contentValues);
        }
    }

    public void updateOrInsertStation(String stationID, String md5) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("stationID", stationID);
        contentValues.put("md5", md5);
        String[] arguments = {stationID};
        int rowsUpdated = db.update(TABLE_STATION, contentValues, "stationID=?", arguments);
        if (rowsUpdated > 0) {
            Log.i(Utils.TAG, rowsUpdated + " stations updated");
        }
        if (rowsUpdated == 0) {
            long rowId = db.insert(TABLE_STATION, null, contentValues);
            if (rowId > 0) {
                Log.i(Utils.TAG, "Station inserted. Row ID: " + rowId);
            } else {
                Log.i(Utils.TAG, "Station not added or updated.");
            }
        }
    }

    public String getProgramRequest(String stationID) {
        String sqlStatement = "select programID " +
                "from schedule as a " +
                "where not exists " +
                "(select * from program as b where a.programID = b.programID) " +
                "and stationID =?";
        String[] arguments = {stationID};
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        String comma = "";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlStatement, arguments);
        if (cursor.moveToFirst()) {
            do {
                sb.append(comma);
                sb.append("\"").append(cursor.getString(0)).append("\"");
                comma = ",";
            } while (cursor.moveToNext());
        } else {
            Log.i(Utils.TAG, "DbUtils.getProgramRequest - No records found in schedule. StationID: " + stationID + " SQL: " + sqlStatement);
            return null;
        }
        sb.append("]");
        Log.i(Utils.TAG, "Program request: " + sb.toString());
        return sb.toString();
    }

    public void storeProgram(JSONObject jsonObject) {
        SQLiteDatabase db = getWritableDatabase();
        String programID = Utils.getString(jsonObject, "programID");
        String[] arguments = {programID};
        db.delete(TABLE_PROGRAM, "programID=?", arguments);
        ContentValues values = new ContentValues();
        try {
            values.put("programID", programID);
            values.put("episodeTitle150", Utils.getString(jsonObject, "episodeTitle150"));
            values.put("md5", jsonObject.getString("md5"));
            values.put("originalAirDate", Utils.getString(jsonObject, "originalAirDate"));
            JSONArray jsonArray = jsonObject.getJSONArray("titles");
            if (jsonArray != null && jsonArray.length() > 0) {
                JSONObject titleRow = (JSONObject) jsonArray.get(0);
                values.put("title120", titleRow.getString("title120"));
            }
            db.insert(TABLE_PROGRAM, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ContentValues transfer(Headend headend) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", headend.getName());
        contentValues.put("location", headend.getLocation());
        contentValues.put("type", headend.getType());
        contentValues.put("uri", headend.getFirstLineupUri());
        return contentValues;
    }
}
