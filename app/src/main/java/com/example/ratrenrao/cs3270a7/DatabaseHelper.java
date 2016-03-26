package com.example.ratrenrao.cs3270a7;

/**
 * A simple {@link Fragment} subclass.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper
{

    public static final String AUTH_TOKEN = "14~zvrD78kx23GiCnDCiZkWUFjEQRRL7DsiIudGO1bJ8XGACI2WWQibJqRxk4U1hFgj";
    private static final String DATABASE_NAME = "Courses";

    private SQLiteDatabase db;
    private final DatabaseOpenHelper databaseOpenHelper;

    private SQLException sqlException;

    public DatabaseHelper(Context context)
    {
        databaseOpenHelper =
                new DatabaseOpenHelper(context);

    }

    public SQLiteDatabase open() throws SQLException
    {
        db = databaseOpenHelper.getWritableDatabase();
        return db;
    }

    public void close()
    {
        if (db != null)
            db.close();
    }

    public long insertCourse(String id, String name, String courseCode,
                             String start, String end)
    {
        ContentValues newCourse = new ContentValues();
        newCourse.put("id", id);
        newCourse.put("name", name);
        newCourse.put("course_code", courseCode);
        newCourse.put("start_at", start);
        newCourse.put("end_at", end);

        open();
        long _id = db.insert("courses", null, newCourse);
        close();
        return _id;
    }

    public void updateCourse(long _id, String id, String name,
                             String courseCode, String start, String end)
    {
        ContentValues editCourse = new ContentValues();
        editCourse.put("id", id);
        editCourse.put("name", name);
        editCourse.put("course_code", courseCode);
        editCourse.put("start_at", start);
        editCourse.put("end_at", end);

        open();
        db.update("courses", editCourse, "_id=" + _id, null);
        close();
    }

    public Cursor getOneCourse(long id)
    {
        return db.query(
                "courses", null, "_id=" + Long.toString(id), null, null, null, null);
    }

    public void deleteCourse(long id)
    {
        open();
        db.delete("courses", "_id=" + id, null);
        close();
    }

    public void deleteAllCourses()
    {
        open();
        db.delete("courses", null, null);
        close();
    }

    public void deleteAllAssignments()
    {
        open();
        db.delete("assignments", null, null);
        close();
    }

    public Cursor getAllCourses()
    {
        return db.query("courses", new String[]{"_id", "name"}, null, null, null, null, "name");
    }

    public Cursor getAllAssignments(long id)
    {
        Cursor result = null;
        try
        {
            result = db.query("assignments", new String[]{"name", "due_at"}, "course_row_id=" + Long.toString(id), null, null, null, null);
        }
        catch (SQLException ex)
        {
            sqlException = ex;
        }

        return result;
    }

    public void insertAssignment(String courseRowId, String id, String name, String due)
    {
        ContentValues newAssignment = new ContentValues();
        newAssignment.put("course_row_id", courseRowId);
        newAssignment.put("id", id);
        newAssignment.put("name", name);
        newAssignment.put("due_at", due);

        open();
        db.insert("assignments", null, newAssignment);
        close();
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper
    {
        public DatabaseOpenHelper(Context context)
        {
            super(context, DatabaseHelper.DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            String createQuery = "CREATE TABLE courses("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "id TEXT,"
                    + "name TEXT,"
                    + "course_code TEXT,"
                    + "start_at TEXT,"
                    + "end_at TEXT);";

            db.execSQL(createQuery);

            createQuery = "CREATE TABLE assignments("
                    + "course_row_id TEXT,"
                    + "id TEXT,"
                    + "name TEXT,"
                    + "due_at TEXT);";

            db.execSQL(createQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
            String createQuery = "CREATE TABLE courses("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "id TEXT,"
                    + "name TEXT,"
                    + "course_code TEXT,"
                    + "start_at TEXT,"
                    + "end_at TEXT);";

            db.execSQL(createQuery);

            createQuery = "CREATE TABLE assignments("
                    + "course_row_id TEXT"
                    + "id TEXT,"
                    + "name TEXT,"
                    + "due_at TEXT);";

            db.execSQL(createQuery);
        }
    }
}
