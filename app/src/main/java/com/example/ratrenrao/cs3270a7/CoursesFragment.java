package com.example.ratrenrao.cs3270a7;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class CoursesFragment extends ListFragment
{
    // callback methods implemented by MainActivity
    public interface CourseListFragmentListener
    {
        // called when user selects a course
        void onCourseSelected(long rowID);

        void onCourseLongSelected(long rowID);

        // called when user decides to add a course
        void onAddCourse();
    }

    private CourseListFragmentListener courseListListener;

    private ListView courseListView; // the ListActivity's ListView
    private CursorAdapter courseAdapter; // adapter for ListView

    // set courseListFragmentListener when fragment attached
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof Activity)
            courseListListener = (CourseListFragmentListener) context;
    }


    @Override
    public void onDetach()
    {
        super.onDetach();
        courseListListener = null;
    }

    // called after View is created
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        setHasOptionsMenu(true); // this fragment has menu items to display

        // set text to display when there are no courses
        setEmptyText(getResources().getString(R.string.stringNoResult));

        // get ListView reference and configure ListView
        courseListView = getListView();
        courseListView.setOnItemClickListener(viewCourseListener);
        courseListView.setOnItemLongClickListener(viewAssignmentListener);
        courseListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // map each course's name to a TextView in the ListView layout
        String[] from = new String[] { "name" };
        int[] to = new int[] { android.R.id.text1 };
        courseAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, null, from, to, 0);
        setListAdapter(courseAdapter); // set adapter that supplies data
    }

    // responds to the user touching a course's name in the ListView
    AdapterView.OnItemClickListener viewCourseListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id)
        {
            courseListListener.onCourseSelected(id); // pass selection to MainActivity
        }
    }; // end viewcourseListener

    AdapterView.OnItemLongClickListener viewAssignmentListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            courseListListener.onCourseLongSelected(id);
            return true;
        }
    };

    // when fragment resumes, use a GetcoursesTask to load courses
    @Override
    public void onResume()
    {
        super.onResume();
        new GetCourseTask().execute((Object[]) null);
    }

    // performs database query outside GUI thread
    private class GetCourseTask extends AsyncTask<Object, Object, Cursor>
    {
        DatabaseHelper databaseConnector =
                new DatabaseHelper(getActivity());

        // open database and return Cursor for all courses
        @Override
        protected Cursor doInBackground(Object... params)
        {
            databaseConnector.open();
            return databaseConnector.getAllCourses();
        }

        // use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result)
        {
            courseAdapter.changeCursor(result); // set the adapter's Cursor
            databaseConnector.close();
        }
    } // end class GetcoursesTask

    // when fragment stops, close Cursor and remove from courseAdapter
    @Override
    public void onStop()
    {
        Cursor cursor = courseAdapter.getCursor(); // get current Cursor
        courseAdapter.changeCursor(null); // adapter now has no Cursor

        if (cursor != null)
            cursor.close(); // release the Cursor's resources

        super.onStop();
    }

    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.course_menu, menu);
    }

    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add:
                courseListListener.onAddCourse();
                return true;
            case R.id.action_import:
                onImportCourses();
                return true;
        }

        return super.onOptionsItemSelected(item); // call super's method
    }

    // update data set
    public void updateCourseList()
    {
        new GetCourseTask().execute((Object[]) null);
    }

    public void onImportCourses()
    {
        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        new getCanvasCourses().execute("");
    }

    public void onGetImportAssignments()
    {
        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        new getCourseAssignments().execute("");
    }

    private Course[] jsonParse(String rawJson){
        GsonBuilder gsonb = new GsonBuilder();
        Gson gson = gsonb.create();

        Course[] courses = null;

        try{
            courses = gson.fromJson(rawJson, Course[].class);
        }catch (Exception e){

        }
        return courses;
    }


    public class getCanvasCourses extends AsyncTask<String, Integer, String>
    {
        DatabaseHelper databaseConnector =
                new DatabaseHelper(getActivity());

        String AUTH_TOKEN = DatabaseHelper.AUTH_TOKEN;
        String rawJSON = "";

        @Override
        protected String doInBackground(String... params){
            try{
                URL url = new URL("https://weber.instructure.com/api/v1/courses");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + AUTH_TOKEN);
                conn.connect();
                int status = conn.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        rawJSON = br.readLine();
                }
            } catch (MalformedURLException e) {
                Log.d("test", e.getMessage());
            } catch (IOException e) {
                Log.d("test", e.getMessage());
            }
            return rawJSON;
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            databaseConnector.open();

            try{
                Course[] courses = jsonParse(result);
                for (Course course : courses) {
                    databaseConnector.insertCourse(course.id, course.name, course.course_code, course.start_at, course.end_at);
                }
            }catch (Exception e){

            }
            updateCourseList();
            databaseConnector.close();
        }
    }

    public class getCourseAssignments extends AsyncTask<String, Integer, String>
    {
        DatabaseHelper databaseConnector =
                new DatabaseHelper(getActivity());

        String AUTH_TOKEN = DatabaseHelper.AUTH_TOKEN;
        String rawJSON = "";

        @Override
        protected String doInBackground(String... params){
            try{
                Log.d("Test", params[0]);
                URL url = new URL("https://weber.instructure.com/api/v1/courses/"+params+"/assignments");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + AUTH_TOKEN);
                conn.connect();
                int status = conn.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        rawJSON = br.readLine();
                }
            } catch (MalformedURLException e) {
                Log.d("test", e.getMessage());
            } catch (IOException e) {
                Log.d("test", e.getMessage());
            }
            return rawJSON;
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            databaseConnector.open();

            try{
                Course[] courses = jsonParse(result);
                for (Course course : courses) {
                    databaseConnector.insertCourse(course.id, course.name, course.course_code, course.start_at, course.end_at);
                }
            }catch (Exception e){

            }
            updateCourseList();
            databaseConnector.close();
        }
    }

    protected class Course
    {
        protected String id;
        protected String sis_course_id;
        protected String name;
        protected String course_code;
        protected String account_id;
        protected String start_at;
        protected String end_at;
        protected String syllabus_body;
        protected String needs_grading_count;
        protected Enrollment[] enrollments;
        protected Calendar calendar;
        protected Term term;
    }

    protected class Term{
        protected String id;
        protected String name;
        protected String start_at;
        protected String end_at;
    }

    protected class Calendar{
        protected String ics;
    }

    protected class Enrollment{
        protected String type;
        protected String role;
        protected String computed_final_score;
        protected String computed_current_score;
        protected String computed_final_grade;
    }
}
