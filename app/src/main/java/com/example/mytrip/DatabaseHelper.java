package com.example.mytrip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mytrip.db";
    private static final int DATABASE_VERSION = 1;

    // Trips Table
    public static final String TABLE_TRIPS = "trips";
    public static final String TRIP_ID = "id";
    public static final String TRIP_DESTINATION = "destination";
    public static final String TRIP_DATE = "date";

    // Items Table
    public static final String TABLE_ITEMS = "items";
    public static final String ITEM_ID = "id";
    public static final String ITEM_TRIP_ID = "trip_id";
    public static final String ITEM_NAME = "item_name";
    public static final String ITEM_PRIORITY = "priority";
    public static final String ITEM_CHECKED = "is_checked";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TRIPS_TABLE = "CREATE TABLE " + TABLE_TRIPS + "("
                + TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TRIP_DESTINATION + " TEXT,"
                + TRIP_DATE + " TEXT" + ")";

        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "("
                + ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ITEM_TRIP_ID + " INTEGER,"
                + ITEM_NAME + " TEXT,"
                + ITEM_PRIORITY + " TEXT,"
                + ITEM_CHECKED + " INTEGER DEFAULT 0" + ")";

        db.execSQL(CREATE_TRIPS_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    // ==================== Trip Functions ====================

    public long insertTrip(String destination, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRIP_DESTINATION, destination);
        values.put(TRIP_DATE, date);
        long id = db.insert(TABLE_TRIPS, null, values);
        db.close();
        return id;
    }

    public void deleteTrip(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRIPS, TRIP_ID + "=?", new String[]{String.valueOf(id)});
        db.delete(TABLE_ITEMS, ITEM_TRIP_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<String[]> getAllTrips() {
        List<String[]> tripList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRIPS, null);
        if (cursor.moveToFirst()) {
            do {
                String[] trip = new String[]{
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)
                };
                tripList.add(trip);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tripList;
    }

    public List<String[]> searchTrips(String keyword) {
        List<String[]> tripList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRIPS
                        + " WHERE " + TRIP_DESTINATION + " LIKE ?",
                new String[]{"%" + keyword + "%"});
        if (cursor.moveToFirst()) {
            do {
                String[] trip = new String[]{
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)
                };
                tripList.add(trip);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tripList;
    }

    // ==================== Item Functions ====================

    public long insertItem(int tripId, String itemName, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ITEM_TRIP_ID, tripId);
        values.put(ITEM_NAME, itemName);
        values.put(ITEM_PRIORITY, priority);
        values.put(ITEM_CHECKED, 0);
        long id = db.insert(TABLE_ITEMS, null, values);
        db.close();
        return id;
    }

    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, ITEM_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<String[]> getItemsByTrip(int tripId) {
        List<String[]> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS
                        + " WHERE " + ITEM_TRIP_ID + "=?",
                new String[]{String.valueOf(tripId)});
        if (cursor.moveToFirst()) {
            do {
                String[] item = new String[]{
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                };
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return itemList;
    }

    public List<String[]> searchItems(int tripId, String keyword) {
        List<String[]> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS
                        + " WHERE " + ITEM_TRIP_ID + "=? AND "
                        + ITEM_NAME + " LIKE ?",
                new String[]{String.valueOf(tripId), "%" + keyword + "%"});
        if (cursor.moveToFirst()) {
            do {
                String[] item = new String[]{
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                };
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return itemList;
    }

    public void updateItemChecked(int id, boolean isChecked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ITEM_CHECKED, isChecked ? 1 : 0);
        db.update(TABLE_ITEMS, values, ITEM_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // ==================== Statistics Functions ====================

    public int getTotalTrips() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TRIPS, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getTotalItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ITEMS, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getPreparedItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ITEMS
                + " WHERE " + ITEM_CHECKED + "=1", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public String getNextTrip() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + TRIP_DESTINATION
                + " FROM " + TABLE_TRIPS
                + " ORDER BY " + TRIP_ID + " DESC LIMIT 1", null);
        String name = "No trips yet";
        if (cursor.moveToFirst()) name = cursor.getString(0);
        cursor.close();
        db.close();
        return name;
    }
}