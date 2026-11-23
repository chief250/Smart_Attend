package com.smartattend.app.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartattend.app.model.AttendanceRecord;

import java.util.ArrayList;
import java.util.List;

public class AttendanceDao {
    private final DatabaseHelper dbHelper;

    public AttendanceDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insertAttendanceRecords(List<AttendanceRecord> records) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (AttendanceRecord record : records) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_ATTENDANCE_ID, record.getId());
                cv.put(DatabaseHelper.COL_SESSION_ID, record.getSessionId());
                cv.put(DatabaseHelper.COL_STATUS, record.getStatus());
                cv.put(DatabaseHelper.COL_CHECK_IN_TIME, record.getCheckInTime());
                cv.put(DatabaseHelper.COL_ATT_COURSE_NAME, record.getCourseName());
                cv.put(DatabaseHelper.COL_ATT_COURSE_CODE, record.getCourseCode());
                cv.put(DatabaseHelper.COL_SESSION_DATE, record.getSessionDate());
                cv.put(DatabaseHelper.COL_VERIFICATION_STATUS, record.getVerificationStatus());
                cv.put(DatabaseHelper.COL_UPDATED_AT, String.valueOf(System.currentTimeMillis()));
                db.insertWithOnConflict(DatabaseHelper.TABLE_CACHED_ATTENDANCE, null, cv,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<AttendanceRecord> getAttendanceRecords() {
        return getAttendanceRecords(null);
    }

    public List<AttendanceRecord> getAttendanceRecords(String courseCode) {
        List<AttendanceRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = null;
        String[] selectionArgs = null;
        if (courseCode != null && !courseCode.isEmpty()) {
            selection = DatabaseHelper.COL_ATT_COURSE_CODE + "=?";
            selectionArgs = new String[]{courseCode};
        }

        Cursor cursor = db.query(DatabaseHelper.TABLE_CACHED_ATTENDANCE,
                null, selection, selectionArgs, null, null,
                DatabaseHelper.COL_SESSION_DATE + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                AttendanceRecord record = new AttendanceRecord();
                record.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ATTENDANCE_ID)));
                record.setSessionId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_ID)));
                record.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATUS)));
                record.setCheckInTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CHECK_IN_TIME)));
                record.setCourseName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ATT_COURSE_NAME)));
                record.setCourseCode(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ATT_COURSE_CODE)));
                record.setSessionDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_DATE)));
                record.setVerificationStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_VERIFICATION_STATUS)));
                records.add(record);
            }
            cursor.close();
        }
        return records;
    }

    public void clearAttendance() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CACHED_ATTENDANCE, null, null);
    }

    public List<AttendanceRecord> getAllRecords() {
        return getAttendanceRecords();
    }

    public void saveRecords(List<AttendanceRecord> records) {
        insertAttendanceRecords(records);
    }
}
