package net.pla1.sda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class DbUtils extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 33;
    private Context context;
    public static final String TABLE_LINEUP = "lineup";
    public static final String[] COLUMNS_LINEUP = {"name", "type", "location", "uri"};

    public static final String TABLE_HEADEND = "headend";
    public static final String[] COLUMNS_HEADEND = {"name", "type", "location", "uri"};

    public static final String TABLE_STATION = "station";
    public static final String[] COLUMNS_STATION = {"stationID", "md5", "name", "callsign", "channel"};

    public static final String TABLE_SCHEDULE = "schedule";
    public static final String[] COLUMNS_SCHEDULE = {"stationID", "programID", "md5", "airDateTime", "duration", "liveTapeDelay", "newShowing"};

    public static final String TABLE_PROGRAM = "program";
    public static final String[] COLUMNS_PROGRAM = {"programID", "md5", "showType", "duration", "episodeTitle150", "title120", "originalAirDate", "hasImageArtwork", "description", "genres"};

    public static final String TABLE_CAST = "cast";
    public static final String[] COLUMNS_CAST = {"programID", "personId", "nameId", "name", "role", "billingOrder"};

    public static final String TABLE_CREW = "crew";
    public static final String[] COLUMNS_CREW = {"programID", "personId", "nameId", "name", "role", "billingOrder"};

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
        db.execSQL(getTableCreateStatement(TABLE_CREW, COLUMNS_CREW));
        db.execSQL(getTableCreateStatement(TABLE_CAST, COLUMNS_CAST));
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
        db.execSQL("drop table if exists " + TABLE_CAST);
        db.execSQL("drop table if exists " + TABLE_CREW);
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

    public int getTableCount(String tableName) {
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
        sb.append(TABLE_CREW).append(" table quantity: ").append(getTableCount(TABLE_CREW)).append("\n");
        sb.append(TABLE_CAST).append(" table quantity: ").append(getTableCount(TABLE_CAST)).append("\n");
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

    public void updateOrInsertStation(Station station) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("stationID", station.getStationID());
        contentValues.put("md5", station.getMd5());
        contentValues.put("name", station.getName());
        contentValues.put("callsign", station.getCallsign());
        contentValues.put("channel", station.getChannel());
        String[] arguments = {station.getStationID()};
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

    private ContentValues transfer(JSONObject jsonObject, String[] columns) throws JSONException {
        ContentValues values = new ContentValues();
        for (String column : columns) {
            values.put(column, Utils.getString(jsonObject, column));
        }
        return values;
    }

    public void storeCrew(String programID, SQLiteDatabase db, JSONArray jsonArray) {
        int rowQuantity = jsonArray.length();
        try {
            for (int i = 0; i < rowQuantity; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String personId = jsonObject.getString("personId");
                String[] arguments = {personId};
                db.delete(TABLE_CREW, "personId=?", arguments);
                ContentValues values = transfer(jsonObject, COLUMNS_CREW);
                values.put("programID", programID);
                db.insert(TABLE_CREW, null, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void storeCast(String programID, SQLiteDatabase db, JSONArray jsonArray) {
        int rowQuantity = jsonArray.length();
        try {
            for (int i = 0; i < rowQuantity; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String personId = jsonObject.getString("personId");
                String[] arguments = {personId};
                db.delete(TABLE_CAST, "personId=?", arguments);
                ContentValues values = transfer(jsonObject, COLUMNS_CAST);
                values.put("programID", programID);
                db.insert(TABLE_CAST, null, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
            values.put("hasImageArtwork", Utils.getString(jsonObject, "hasImageArtwork"));
            JSONArray jsonArray = jsonObject.getJSONArray("titles");
            if (jsonArray != null && jsonArray.length() > 0) {
                JSONObject titleRow = (JSONObject) jsonArray.get(0);
                values.put("title120", titleRow.getString("title120"));
            }
            JSONObject descriptionsObject = Utils.getJSONObject(jsonObject, "descriptions");
            if (descriptionsObject != null) {
                jsonArray = descriptionsObject.getJSONArray("description1000");
                if (jsonArray != null && jsonArray.length() > 0) {
                    JSONObject descriptionRow = (JSONObject) jsonArray.get(0);
                    values.put("description", descriptionRow.getString("description"));
                }
            }
            jsonArray = Utils.getJSONArray(jsonObject, "genres");
            if (jsonArray != null) {
                String genres = jsonArray.toString();
                if (genres != null) {
                    genres = genres.replaceAll("[\"\\[\\]]", "");
                    values.put("genres", genres);
                }
            }
            jsonArray = Utils.getJSONArray(jsonObject, "crew");
            if (jsonArray != null) {
                storeCrew(programID, db, jsonArray);
            }
            jsonArray = Utils.getJSONArray(jsonObject, "cast");
            if (jsonArray != null) {
                storeCast(programID, db, jsonArray);
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

    public ArrayList<Schedule> getSchedule() {
        ArrayList<Schedule> scheduleArrayList = new ArrayList<Schedule>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select a.programID, a.airDateTime, episodeTitle150, b.title120, c.channel, c.name, c.callsign, a.duration, b.hasImageArtwork from schedule as a join program as b on a.programID = b.programID join station as c on a.stationID = c.stationID", null, null);
        if (cursor.moveToFirst()) {
            do {
                Schedule schedule = new Schedule();
                int col = 0;
                Program program = new Program();
                schedule.setProgram(program);
                Station station = new Station();
                schedule.setStation(station);
                String programID = cursor.getString(col++);
                schedule.setProgramID(programID);
                program.setProgramID(programID);
                long airDateMs = cursor.getLong(col++);
                schedule.setAirDateTime(new Date(airDateMs));
                program.setEpisodeTitle150(cursor.getString(col++));
                program.setTitle120(cursor.getString(col++));
                station.setChannel(cursor.getString(col++));
                station.setName(cursor.getString(col++));
                station.setCallsign(cursor.getString(col++));
                schedule.setDuration(cursor.getInt(col++));
                program.setHasImageArtwork("true".equalsIgnoreCase(cursor.getString(col++)));
                program.setFound(true);
                scheduleArrayList.add(schedule);
            } while (cursor.moveToNext());
        }
        return scheduleArrayList;
    }


    public ArrayList<Crew> getCrew(String programID) {
        ArrayList<Crew> arrayList = new ArrayList<Crew>();
        SQLiteDatabase db = getReadableDatabase();
        String[] arguments = {programID};
        Cursor cursor = db.query(TABLE_CREW, COLUMNS_CREW, "programID=?", arguments, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Crew crew = new Crew();
                int col = 1;
                crew.setPersonId(cursor.getString(col++));
                crew.setNameId(cursor.getString(col++));
                crew.setName(cursor.getString(col++));
                crew.setRole(cursor.getString(col++));
                crew.setBillingOrder(cursor.getString(col++));
                arrayList.add(crew);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return arrayList;
    }

    public ArrayList<Cast> getCast(String programID) {
        ArrayList<Cast> arrayList = new ArrayList<Cast>();
        SQLiteDatabase db = getReadableDatabase();
        String[] arguments = {programID};
        Cursor cursor = db.query(TABLE_CREW, COLUMNS_CAST, "programID=?", arguments, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Cast cast = new Cast();
                int col = 1;
                cast.setPersonId(cursor.getString(col++));
                cast.setNameId(cursor.getString(col++));
                cast.setName(cursor.getString(col++));
                cast.setRole(cursor.getString(col++));
                cast.setBillingOrder(cursor.getString(col++));
                arrayList.add(cast);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return arrayList;
    }

    public Program getProgram(String programID) {
        Program program = new Program();
        SQLiteDatabase db = getReadableDatabase();
        String[] arguments = {programID};
        Cursor cursor = db.query(TABLE_PROGRAM, COLUMNS_PROGRAM, "programID=?", arguments, null, null, null);
        if (cursor.moveToFirst()) {
            int col = 0;
            program.setProgramID(cursor.getString(col++));
            program.setMd5(cursor.getString(col++));
            program.setShowType(cursor.getString(col++));
            program.setDuration(cursor.getInt(col++));
            program.setEpisodeTitle150(cursor.getString(col++));
            program.setTitle120(cursor.getString(col++));
            String originalAirDateString = cursor.getString(col++);
            //  program.setOriginalAirDate();
            program.setHasImageArtwork("true".equalsIgnoreCase(cursor.getString(col++)));
            program.setDescription(cursor.getString(col++));
            program.setGenres(cursor.getString(col++));
            program.setCrew(getCrew(programID));
            program.setCast(getCast(programID));
            program.setFound(true);
        }
        cursor.close();
        return program;
    }

}
