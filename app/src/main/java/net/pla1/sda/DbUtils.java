package net.pla1.sda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DbUtils extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private Context context;
    private static final String TABLE_LINEUP = "lineup";
    private static final String[] COLUMNS_LINEUP = {"name", "type", "location", "uri"};
    private static final String TABLE_HEADEND = "headend";
    private static final String[] COLUMNS_HEADEND = {"name", "type", "location", "uri"};

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
        Log.w(Utils.TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("drop table if exists " + TABLE_HEADEND);
        db.execSQL("drop table if exists " + TABLE_LINEUP);
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
        db.close();
        Log.i(Utils.TAG, "getHeadend " + uri);
        return headend;
    }

    public void delete(Headend headend) {
        SQLiteDatabase db = getWritableDatabase();
        String[] arguments = {headend.getFirstLineupUri()};
        db.delete(TABLE_HEADEND, "uri = ?", arguments);
        db.close();
    }

    public void updateOrInsert(Headend headend) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = transfer(headend);
        String[] arguments = {headend.getFirstLineupUri()};
        int rowsUpdated = db.update(TABLE_HEADEND, contentValues, "uri=?", arguments);
        if (rowsUpdated == 0) {
            long rowId = db.insert(TABLE_HEADEND, null, contentValues);
        }
        db.close();
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
