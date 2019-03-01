package com.ubicomp.stopit.stopit.model;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

public class Provider extends ContentProvider {

    /**
     * Authority of this content provider
     */
    public static String AUTHORITY = "com.ubicomp.stopit.stopit.model.provider.stopit";

    /**
     * ContentProvider database version. Increment every time you modify the database structure
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Database stored in external folder: /AWARE/stopit.db
     */
    public static final String DATABASE_NAME = "stopit.db";

    //Database table names
    public static final String DB_DRAWING = "drawing";
    public static final String DB_SCREENSHOT = "screenshot";

    //ContentProvider query indexes
    private static final int TABLE_DRAWING_DIR = 1;
    private static final int TABLE_DRAWING_ITEM = 2;
    private static final int TABLE_SCREENSHOT_DIR = 3;
    private static final int TABLE_SCREENSHOT_ITEM = 4;

    /**
     * Database tables:
     * - drawing test data, screenshot image
     */
    public static final String[] DATABASE_TABLES = {
            DB_DRAWING, DB_SCREENSHOT
    };

    //These are columns that we need to sync data, don't change this!
    public interface AWAREColumns extends BaseColumns {
        String _ID = "_id";
        String TIMESTAMP = "timestamp";
        String DEVICE_ID = "device_id";
    }

    /**
     * Drawing data table
     */
    public static final class Drawing_Data implements AWAREColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_DRAWING);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.ubicomp.stopit.stopit.model.provider.drawing";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.ubicomp.stopit.stopit.model.provider.drawing";

        public static final String DATA = "data";
    }

    //Drawing table fields
    private static final String DB_TBL_DRAWING_FIELDS =
            Drawing_Data._ID + " integer primary key autoincrement," +
                    Drawing_Data.TIMESTAMP + " real default 0," +
                    Drawing_Data.DEVICE_ID + " text default ''," +
                    Drawing_Data.DATA + " longtext default ''";

    /**
     * Screenshot table
     */
    public static final class Screenshot_Data implements AWAREColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_SCREENSHOT);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.ubicomp.stopit.stopit.model.provider.screenshot";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.ubicomp.stopit.stopit.model.provider.screenshot";

        public static final String IMAGE = "image";
    }

    //Screenshot table fields
    private static final String DB_TBL_SCREENSHOT_FIELDS =
            Screenshot_Data._ID + " integer primary key autoincrement," +
                    Screenshot_Data.TIMESTAMP + " real default 0," +
                    Screenshot_Data.DEVICE_ID + " text default ''," +
                    Screenshot_Data.IMAGE + " blob default null";


    /**
     * Share the fields with AWARE so we can replicate the table schema on the server
     */
    public static final String[] TABLES_FIELDS = {
            DB_TBL_DRAWING_FIELDS, DB_TBL_SCREENSHOT_FIELDS
    };

    //Helper variables for ContentProvider - DO NOT CHANGE
    private UriMatcher sUriMatcher;
    private DatabaseHelper dbHelper;
    private static SQLiteDatabase database;
    private void initialiseDatabase() {
        if (dbHelper == null)
            dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        if (database == null)
            database = dbHelper.getWritableDatabase();
    }

    //For each table, create a hashmap needed for database queries
    private HashMap<String, String> tableDrawingHash;
    private HashMap<String, String> tableScreenshotHash;

    /**
     * Returns the provider authority that is dynamic
     * @return
     */
    public static String getAuthority(Context context) {
        AUTHORITY = context.getPackageName() + ".model.provider.stopit";
        return AUTHORITY;
    }

    @Override
    public boolean onCreate() {
        //This is a hack to allow providers to be reusable in any application/plugin by making the authority dynamic using the package name of the parent app
        AUTHORITY = getContext().getPackageName() + ".model.provider.stopit";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //Drawing table indexes DIR and ITEM
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], TABLE_DRAWING_DIR);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", TABLE_DRAWING_ITEM);

        //Screenshot table indexes DIR and ITEM
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1], TABLE_SCREENSHOT_DIR);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1] + "/#", TABLE_SCREENSHOT_ITEM);

        //Drawing table HasMap
        tableDrawingHash = new HashMap<>();
        tableDrawingHash.put(Drawing_Data._ID, Drawing_Data._ID);
        tableDrawingHash.put(Drawing_Data.TIMESTAMP, Drawing_Data.TIMESTAMP);
        tableDrawingHash.put(Drawing_Data.DEVICE_ID, Drawing_Data.DEVICE_ID);
        tableDrawingHash.put(Drawing_Data.DATA, Drawing_Data.DATA);

        //Screenshot table HasMap
        tableScreenshotHash = new HashMap<>();
        tableScreenshotHash.put(Screenshot_Data._ID, Screenshot_Data._ID);
        tableScreenshotHash.put(Screenshot_Data.TIMESTAMP, Screenshot_Data.TIMESTAMP);
        tableScreenshotHash.put(Screenshot_Data.DEVICE_ID, Screenshot_Data.DEVICE_ID);
        tableScreenshotHash.put(Screenshot_Data.IMAGE, Screenshot_Data.IMAGE);

        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (sUriMatcher.match(uri)) {

            case TABLE_DRAWING_DIR:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;

            case TABLE_SCREENSHOT_DIR:
                count = database.delete(DATABASE_TABLES[1], selection, selectionArgs);
                break;

            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        initialiseDatabase();

        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();

        database.beginTransaction();

        switch (sUriMatcher.match(uri)) {

            case TABLE_DRAWING_DIR:
                long drawing_id = database.insert(DATABASE_TABLES[0], Drawing_Data.DEVICE_ID, values);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (drawing_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Drawing_Data.CONTENT_URI, drawing_id);
                    getContext().getContentResolver().notifyChange(dataUri, null, false);
                    return dataUri;
                }
                database.endTransaction();
                throw new SQLException("Failed to insert row into " + uri);

            case TABLE_SCREENSHOT_DIR:
                long screenshot_id = database.insert(DATABASE_TABLES[1], Screenshot_Data.DEVICE_ID, values);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (screenshot_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Screenshot_Data.CONTENT_URI, screenshot_id);
                    getContext().getContentResolver().notifyChange(dataUri, null, false);
                    return dataUri;
                }
                database.endTransaction();
                throw new SQLException("Failed to insert row into " + uri);

            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        initialiseDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {

            case TABLE_DRAWING_DIR:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(tableDrawingHash); //the hashmap of the table
                break;

            case TABLE_SCREENSHOT_DIR:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(tableScreenshotHash); //the hashmap of the table
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        //Don't change me
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {

            case TABLE_DRAWING_DIR:
                return Drawing_Data.CONTENT_TYPE;
            case TABLE_DRAWING_ITEM:
                return Drawing_Data.CONTENT_ITEM_TYPE;

            case TABLE_SCREENSHOT_DIR:
                return Screenshot_Data.CONTENT_TYPE;
            case TABLE_SCREENSHOT_ITEM:
                return Screenshot_Data.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (sUriMatcher.match(uri)) {

            case TABLE_DRAWING_DIR:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;

            case TABLE_SCREENSHOT_DIR:
                count = database.update(DATABASE_TABLES[1], values, selection, selectionArgs);
                break;

            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        database.setTransactionSuccessful();
        database.endTransaction();

        getContext().getContentResolver().notifyChange(uri, null, false);

        return count;
    }
}
