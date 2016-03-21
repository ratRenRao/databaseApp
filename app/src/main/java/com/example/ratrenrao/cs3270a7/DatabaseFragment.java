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

public class DatabaseFragment{

    // database name
    private static final String DATABASE_NAME = "Courses";
    public static String AUTH_TOKEN = "";

    private SQLiteDatabase database; // for interacting with the database
    private DatabaseOpenHelper databaseOpenHelper; // creates the database

    // public constructor for DatabaseConnector
    public DatabaseFragment(Context context)
    {
        // create a new DatabaseOpenHelper
        databaseOpenHelper =
                new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
    }

    // open the database connection
    public void open() throws SQLException
    {
        // create or open a database for reading/writing
        database = databaseOpenHelper.getWritableDatabase();
    }

    // close the database connection
    public void close()
    {
        if (database != null)
            database.close(); // close the database connection
    }

    // inserts a new course in the database
    public long insertCourse(String id, String name, String course_code,
                             String start_at, String end_at)
    {
        ContentValues newCourse = new ContentValues();
        newCourse.put("id", id);
        newCourse.put("name", name);
        newCourse.put("course_code", course_code);
        newCourse.put("start_at", start_at);
        newCourse.put("end_at", end_at);

        open(); // open the database
        long _id = database.insert("courses", null, newCourse);
        close(); // close the database
        return _id;
    }

    // updates an existing course in the database
    public void updateCourse(long _id, String id, String name,
                             String course_code, String start_at, String end_at)
    {
        ContentValues editCourse = new ContentValues();
        editCourse.put("id", id);
        editCourse.put("name", name);
        editCourse.put("course_code", course_code);
        editCourse.put("start_at", start_at);
        editCourse.put("end_at", end_at);

        open(); // open the database
        database.update("courses", editCourse, "_id=" + _id, null);
        close(); // close the database
    } // end method updatecourse

    // return a Cursor with all course names in the database
    public Cursor getAllCourses()
    {
        return database.query("courses", new String[] {"_id", "name"},null,null,null,null,"name");
    }

    // return a Cursor containing specified course's information
    public Cursor getOneCourse(long id)
    {
        return database.query(
                "courses", null, "_id=" + id, null, null, null, null);
    }

    // delete the course specified by the given String name
    public void deleteCourse(long id)
    {
        open(); // open the database
        database.delete("courses", "_id=" + id, null);
        close(); // close the database
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper
    {
        // constructor
        public DatabaseOpenHelper(Context context, String name,
                                  SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        // creates the courses table when the database is created
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            // query to create a new table named courses
            String createQuery = "CREATE TABLE courses" +
                    "(_id integer primary key autoincrement," +
                    "id TEXT, name TEXT, course_code TEXT, " +
                    "start_at TEXT, end_at TEXT);";

            db.execSQL(createQuery); // execute query to create the database
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
        }
    } // end class DatabaseOpenHelper
} // end class DatabaseConnector
