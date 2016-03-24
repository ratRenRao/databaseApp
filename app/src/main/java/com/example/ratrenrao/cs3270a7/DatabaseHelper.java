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

public class DatabaseHelper
{

    public static String AUTH_TOKEN = "";
    private static final String DATABASE_NAME = "Courses";

    private SQLiteDatabase db;
    private DatabaseOpenHelper databaseOpenHelper;

    public DatabaseHelper(Context context)
    {
        databaseOpenHelper =
                new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
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

        open(); // open the db
        long _id = db.insert("courses", null, newCourse);
        close(); // close the db
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

    public Cursor getAllCourses()
    {
        return db.query("courses", new String[]{"_id", "name"}, null, null, null, null, "name");
    }

    public Cursor getOneCourse(long id)
    {
        return db.query(
                "courses", null, "_id=" + id, null, null, null, null);
    }

    public void deleteCourse(long id)
    {
        open();
        db.delete("courses", "_id=" + id, null);
        close();
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper
    {
        public DatabaseOpenHelper(Context context, String name,
                                  SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);
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
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
        }
    }
}
