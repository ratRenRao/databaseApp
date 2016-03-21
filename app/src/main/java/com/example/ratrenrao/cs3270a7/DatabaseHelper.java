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

    // public constructor for DatabaseConnector
    public DatabaseHelper(Context context)
    {
        // create a new DatabaseOpenHelper
        databaseOpenHelper =
                new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
    }

    // open the connection
    public SQLiteDatabase open() throws SQLException
    {
        // create or open a db for reading/writing
        db = databaseOpenHelper.getWritableDatabase();
        return db;
    }

    // close the connection
    public void close()
    {
        if (db != null)
            db.close();
    }

    // inserts a new course in the db
    public long insertCourse(String id, String name, String course_code,
                             String start_at, String end_at)
    {
        ContentValues newCourse = new ContentValues();
        newCourse.put("id", id);
        newCourse.put("name", name);
        newCourse.put("course_code", course_code);
        newCourse.put("start_at", start_at);
        newCourse.put("end_at", end_at);

        open(); // open the db
        long _id = db.insert("courses", null, newCourse);
        close(); // close the db
        return _id;
    }

    // updates an existing course in the db
    public void updateCourse(long _id, String id, String name,
                             String course_code, String start_at, String end_at)
    {
        ContentValues editCourse = new ContentValues();
        editCourse.put("id", id);
        editCourse.put("name", name);
        editCourse.put("course_code", course_code);
        editCourse.put("start_at", start_at);
        editCourse.put("end_at", end_at);

        open(); // open the db
        db.update("courses", editCourse, "_id=" + _id, null);
        close(); // close the db
    } // end method updatecourse

    // return a Cursor with all course names in the db
    public Cursor getAllCourses()
    {
        return db.query("courses", new String[] {"_id", "name"}, null, null, null, null, "name");
    }

    // return a Cursor containing specified course's information
    public Cursor getOneCourse(long id)
    {
        return db.query(
                "courses", null, "_id=" + id, null, null, null, null);
    }

    // delete the course specified by the given String name
    public void deleteCourse(long id)
    {
        open(); // open the db
        db.delete("courses", "_id=" + id, null);
        close(); // close the db
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper
    {
        // constructor
        public DatabaseOpenHelper(Context context, String name,
                                  SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        // creates the courses table when the db is created
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            // query to create a new table named courses
            String createQuery = "CREATE TABLE courses("
                    + "_id integer primary key autoincrement,"
                    + "id TEXT,"
                    + "name TEXT,"
                    + "course_code TEXT,"
                    + "start_at TEXT,"
                    + "end_at TEXT);";

            db.execSQL(createQuery); // execute query to create the database
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
        }
    }
}
