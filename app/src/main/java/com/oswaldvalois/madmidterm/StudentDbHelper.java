package com.oswaldvalois.madmidterm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite helper that owns the students table and every CRUD operation
 * (CREATE on install, INSERT, UPDATE, DELETE, READ).
 */
public class StudentDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "student_record.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_STUDENTS = "students";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_STUDENT_ID = "student_id";
    public static final String COL_ENROLLED = "enrolled";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_STUDENTS + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_STUDENT_ID + " TEXT NOT NULL UNIQUE, "
                    + COL_ENROLLED + " INTEGER NOT NULL DEFAULT 0)";

    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_STUDENTS;

    public StudentDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
    }

    /** INSERT - returns the new row id, or -1 when the insert failed. */
    public long insertStudent(String name, String studentId, boolean enrolled) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_ENROLLED, enrolled ? 1 : 0);
        return getWritableDatabase().insert(TABLE_STUDENTS, null, values);
    }

    /** UPDATE - returns the number of rows changed. */
    public int updateStudent(long id, String name, String studentId, boolean enrolled) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_ENROLLED, enrolled ? 1 : 0);
        return getWritableDatabase().update(
                TABLE_STUDENTS,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /** DELETE - returns the number of rows removed. */
    public int deleteStudent(long id) {
        return getWritableDatabase().delete(
                TABLE_STUDENTS,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /** READ all records, ordered by name. */
    public List<Student> getAllStudents() {
        List<Student> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_STUDENTS,
                null,
                null,
                null,
                null,
                null,
                COL_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                result.add(fromCursor(cursor));
            }
        }
        return result;
    }

    /** READ a single record by its row id. */
    public Student getStudentById(long id) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_STUDENTS,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }
        return null;
    }

    /**
     * READ - is this student ID already stored?
     *
     * @param studentId the ID to look for
     * @param excludeId ignore this row (used while editing); pass -1 when adding
     */
    public boolean studentIdExists(String studentId, long excludeId) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_STUDENTS,
                new String[]{COL_ID},
                COL_STUDENT_ID + " = ? AND " + COL_ID + " <> ?",
                new String[]{studentId, String.valueOf(excludeId)},
                null,
                null,
                null)) {
            return cursor.getCount() > 0;
        }
    }

    private Student fromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
        String studentId = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_ID));
        boolean enrolled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ENROLLED)) == 1;
        return new Student(id, name, studentId, enrolled);
    }
}