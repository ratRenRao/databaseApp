package com.example.ratrenrao.cs3270a7;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

public class CoursesFragment extends ListFragment
{
    public interface CourseListFragmentListener
    {
        void onCourseSelected(long rowID);

        void onCourseLongSelected(long rowID);

        void onAddCourse();
    }

    private CourseListFragmentListener courseListListener;

    private CursorAdapter courseAdapter;

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        setEmptyText(getResources().getString(R.string.stringNoResult));

        ListView courseListView = getListView();
        courseListView.setOnItemClickListener(viewCourseListener);
        courseListView.setOnItemLongClickListener(viewAssignmentListener);
        courseListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        String[] from = new String[]{"name"};
        int[] to = new int[]{android.R.id.text1};

        courseAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, null, from, to, 0);
        setListAdapter(courseAdapter);
    }

    private final AdapterView.OnItemClickListener viewCourseListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id)
        {
            courseListListener.onCourseSelected(id);
        }
    };

    private final AdapterView.OnItemLongClickListener viewAssignmentListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            courseListListener.onCourseLongSelected(id);
            return true;
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        new GetCourseTask().execute((Object[]) null);
    }

    private class GetCourseTask extends AsyncTask<Object, Object, Cursor>
    {
        final DatabaseHelper databaseConnector =
                new DatabaseHelper(getActivity());

        @Override
        protected Cursor doInBackground(Object... params)
        {
            databaseConnector.open();
            return databaseConnector.getAllCourses();
        }

        @Override
        protected void onPostExecute(Cursor result)
        {
            courseAdapter.changeCursor(result);
            databaseConnector.close();
        }
    }

    @Override
    public void onStop()
    {
        Cursor cursor = courseAdapter.getCursor();
        courseAdapter.changeCursor(null);

        if (cursor != null)
            cursor.close();

        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.course_menu, menu);
    }

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

        return super.onOptionsItemSelected(item);
    }

    public void updateCourseList()
    {
        new GetCourseTask().execute((Object[]) null);
    }

    private void onImportCourses()
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

    private Course[] jsonParse(String rawJson)
    {
        GsonBuilder gsonb = new GsonBuilder();
        Gson gson = gsonb.create();

        Course[] courses = null;

        try
        {
            courses = gson.fromJson(rawJson, Course[].class);
        } catch (Exception ignored)
        {

        }
        return courses;
    }


    public class getCanvasCourses extends AsyncTask<String, Integer, String>
    {
        final DatabaseHelper databaseConnector =
                new DatabaseHelper(getActivity());

        final String AUTH_TOKEN = DatabaseHelper.AUTH_TOKEN;
        String rawJSON = "";

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                URL url = new URL("https://weber.instructure.com/api/v1/courses");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + AUTH_TOKEN);
                conn.connect();
                int status = conn.getResponseCode();
                switch (status)
                {
                    case 200:
                    case 201:
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        rawJSON = br.readLine();
                }
            } catch (IOException e)
            {
                Log.d("test", e.getMessage());
            }
            return rawJSON;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            databaseConnector.open();

            try
            {
                Course[] courses = jsonParse(result);
                for (Course course : courses)
                {
                    databaseConnector.insertCourse(course.id, course.name, course.courseCode, course.start, course.end);
                }
            } catch (Exception ignored)
            {

            }
            updateCourseList();
            databaseConnector.close();
        }
    }

    public class getCourseAssignments extends AsyncTask<String, Integer, String>
    {
        final DatabaseHelper databaseConnector =
                new DatabaseHelper(getActivity());

        final String AUTH_TOKEN = DatabaseHelper.AUTH_TOKEN;
        String rawJSON = "";

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                Log.d("Test", params[0]);
                URL url = new URL("https://weber.instructure.com/api/v1/courses/" + Arrays.toString(params) + "/assignments");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + AUTH_TOKEN);
                conn.connect();
                int status = conn.getResponseCode();
                switch (status)
                {
                    case 200:
                    case 201:
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        rawJSON = br.readLine();
                }
            } catch (IOException e)
            {
                Log.d("test", e.getMessage());
            }
            return rawJSON;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            databaseConnector.open();

            try
            {
                Course[] courses = jsonParse(result);
                for (Course course : courses)
                {
                    databaseConnector.insertCourse(course.id, course.name, course.courseCode, course.start, course.end);
                }
            } catch (Exception ignored)
            {

            }
            updateCourseList();
            databaseConnector.close();
        }
    }

    class Course
    {
        String id;
        protected String sisCourseId;
        String name;
        String courseCode;
        protected String accountId;
        String start;
        String end;
        protected String syllabusBody;
        protected String gradingCount;
        protected Enrollment[] enrollments;
        protected Calendar calendar;
        protected Term term;
    }

    protected class Term
    {
        protected String id;
        protected String name;
        protected String start;
        protected String end;
    }

    protected class Calendar
    {
        protected String ics;
    }

    protected class Enrollment
    {
        protected String type;
        protected String role;
        protected String finalScore;
        protected String currentScore;
        protected String finalGrade;
    }
}
