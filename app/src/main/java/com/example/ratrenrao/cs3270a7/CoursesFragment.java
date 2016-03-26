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

import javax.net.ssl.HttpsURLConnection;

public class CoursesFragment extends ListFragment
{
    private Course[] courses;

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

        setEmptyText(getResources().getString(R.string.stringNoCourses));

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
        new GetDbCourse().execute((Object[]) null);
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
        new GetDbCourse().execute((Object[]) null);
    }

    private void onImportCourses()
    {
        setEmptyText(getResources().getString(R.string.stringGettingData));

        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        new GetCanvasData().execute("");
    }

    private Course[] parseCourseJson(String rawJson)
    {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        Course[] parsedCourses = null;

        try
        {
            parsedCourses = gson.fromJson(rawJson, Course[].class);
        } catch (Exception ignored)
        {

        }
        return parsedCourses;
    }

    private Assignment[] parseAssignmentJson(String rawJson)
    {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        Assignment[] parsedAssignments = null;

        try
        {
            parsedAssignments = gson.fromJson(rawJson, Assignment[].class);
        } catch (Exception ignored)
        {

        }
        return parsedAssignments;
    }

    private class GetDbCourse extends AsyncTask<Object, Object, Cursor>
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
            setEmptyText(getResources().getString(R.string.stringNoCourses));
        }
    }

    public class GetCanvasData extends AsyncTask<String, Integer, String>
    {
        final DatabaseHelper databaseHelper =
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

            databaseHelper.open();

            try
            {
                databaseHelper.deleteAllCourses();
                databaseHelper.deleteAllAssignments();
                courses = parseCourseJson(result);
                for (Course course : courses)
                {
                    long rowId = databaseHelper.insertCourse(course.id, course.name, course.course_code, course.start_at, course.end_at);
                    new GetAssignmentsApi().execute(new Long[] {Long.parseLong(course.id), rowId});
                }
            } catch (Exception ignored)
            {

            }

            try
            {
                for (Course course : courses)
                {
                    //new GetAssignmentsApi().execute(Long.parseLong(course.id));
                }
            } catch (Exception ignored)
            {

            }
            updateCourseList();
            databaseHelper.close();
        }
    }

    protected class GetAssignmentsApi extends AsyncTask<Long, Integer, String>
    {
        final DatabaseHelper databaseHelper =
                new DatabaseHelper(getActivity());

        final String AUTH_TOKEN = DatabaseHelper.AUTH_TOKEN;
        private String courseId, rowId;
        String rawJSON = "";

        @Override
        protected String doInBackground(Long... params)
        {
            try
            {
                courseId = Long.toString(params[0]);
                rowId = Long.toString(params[1]);
                URL url = new URL("https://weber.instructure.com/api/v1/courses/" + courseId + "/assignments");
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
            if (result.isEmpty())
                return;

            super.onPostExecute(result);

            try
            {
                Assignment[] assignments = parseAssignmentJson(result);
                for (Assignment assignment : assignments)
                {
                    databaseHelper.insertAssignment(rowId, assignment.id, assignment.name, assignment.due_at);
                }
            } catch (Exception ignored)
            {

            }
            databaseHelper.close();
        }
    }

    class Course
    {
        String id;
        String name;
        String course_code;
        String start_at;
        String end_at;
    }


    protected class Assignment
    {
        String id;
        String name;
        String due_at;
    }
}
