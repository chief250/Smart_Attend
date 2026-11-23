package com.smartattend.app.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartattend.app.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    private final DatabaseHelper dbHelper;

    public CourseDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insertCourses(List<Course> courses) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Course course : courses) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_COURSE_ID, course.getId());
                cv.put(DatabaseHelper.COL_COURSE_CODE, course.getCourseCode());
                cv.put(DatabaseHelper.COL_COURSE_NAME, course.getCourseName());
                cv.put(DatabaseHelper.COL_LECTURER_ID, course.getLecturerId());
                cv.put(DatabaseHelper.COL_ATTENDANCE_PERCENTAGE, course.getAttendancePercentage());
                cv.put(DatabaseHelper.COL_UPDATED_AT, String.valueOf(System.currentTimeMillis()));
                db.insertWithOnConflict(DatabaseHelper.TABLE_CACHED_COURSES, null, cv,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CACHED_COURSES,
                null, null, null, null, null,
                DatabaseHelper.COL_COURSE_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Course course = new Course();
                course.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE_ID)));
                course.setCourseCode(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE_CODE)));
                course.setCourseName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE_NAME)));
                course.setLecturerId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LECTURER_ID)));
                course.setAttendancePercentage(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ATTENDANCE_PERCENTAGE)));
                courses.add(course);
            }
            cursor.close();
        }
        return courses;
    }

    public void clearCourses() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CACHED_COURSES, null, null);
    }

    public List<Course> getAllCourses() {
        return getCourses();
    }

    public void saveCourses(List<Course> courses) {
        insertCourses(courses);
    }
}
