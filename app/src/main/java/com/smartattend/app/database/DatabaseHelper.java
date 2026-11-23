package com.smartattend.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smart_attend.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_CACHED_COURSES = "cached_courses";
    public static final String TABLE_CACHED_ATTENDANCE = "cached_attendance";
    public static final String TABLE_APP_SETTINGS = "app_settings";

    // cached_courses columns
    public static final String COL_COURSE_ID = "id";
    public static final String COL_COURSE_CODE = "course_code";
    public static final String COL_COURSE_NAME = "course_name";
    public static final String COL_LECTURER_ID = "lecturer_id";
    public static final String COL_ATTENDANCE_PERCENTAGE = "attendance_percentage";
    public static final String COL_UPDATED_AT = "updated_at";

    // cached_attendance columns
    public static final String COL_ATTENDANCE_ID = "id";
    public static final String COL_SESSION_ID = "session_id";
    public static final String COL_STATUS = "status";
    public static final String COL_CHECK_IN_TIME = "check_in_time";
    public static final String COL_ATT_COURSE_NAME = "course_name";
    public static final String COL_ATT_COURSE_CODE = "course_code";
    public static final String COL_SESSION_DATE = "session_date";
    public static final String COL_VERIFICATION_STATUS = "verification_status";

    // app_settings columns
    public static final String COL_KEY = "key";
    public static final String COL_VALUE = "value";

    private static final String CREATE_TABLE_COURSES =
            "CREATE TABLE " + TABLE_CACHED_COURSES + " (" +
            COL_COURSE_ID + " INTEGER PRIMARY KEY, " +
            COL_COURSE_CODE + " TEXT, " +
            COL_COURSE_NAME + " TEXT, " +
            COL_LECTURER_ID + " INTEGER, " +
            COL_ATTENDANCE_PERCENTAGE + " REAL DEFAULT 0, " +
            COL_UPDATED_AT + " TEXT" +
            ")";

    private static final String CREATE_TABLE_ATTENDANCE =
            "CREATE TABLE " + TABLE_CACHED_ATTENDANCE + " (" +
            COL_ATTENDANCE_ID + " INTEGER PRIMARY KEY, " +
            COL_SESSION_ID + " INTEGER, " +
            COL_STATUS + " TEXT, " +
            COL_CHECK_IN_TIME + " TEXT, " +
            COL_ATT_COURSE_NAME + " TEXT, " +
            COL_ATT_COURSE_CODE + " TEXT, " +
            COL_SESSION_DATE + " TEXT, " +
            COL_VERIFICATION_STATUS + " TEXT, " +
            COL_UPDATED_AT + " TEXT" +
            ")";

    private static final String CREATE_TABLE_SETTINGS =
            "CREATE TABLE " + TABLE_APP_SETTINGS + " (" +
            COL_KEY + " TEXT PRIMARY KEY, " +
            COL_VALUE + " TEXT" +
            ")";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_COURSES);
        db.execSQL(CREATE_TABLE_ATTENDANCE);
        db.execSQL(CREATE_TABLE_SETTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CACHED_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CACHED_ATTENDANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_SETTINGS);
        onCreate(db);
    }

    public void saveSetting(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put(COL_KEY, key);
        cv.put(COL_VALUE, value);
        db.insertWithOnConflict(TABLE_APP_SETTINGS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = getReadableDatabase();
        android.database.Cursor cursor = db.query(TABLE_APP_SETTINGS,
                new String[]{COL_VALUE},
                COL_KEY + "=?", new String[]{key},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String value = cursor.getString(0);
            cursor.close();
            return value;
        }
        if (cursor != null) cursor.close();
        return defaultValue;
    }
}
