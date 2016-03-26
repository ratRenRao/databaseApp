package com.example.ratrenrao.cs3270a7;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class AssignmentFragment extends ListFragment
{
    private CursorAdapter assignmentAdapter;
    private long rowID = -1;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null)
            rowID = savedInstanceState.getLong(MainActivity.ROW_ID);
        else
        {
            Bundle arguments = getArguments();

            if (arguments != null)
                rowID = arguments.getLong(MainActivity.ROW_ID);
        }

        setEmptyText(getResources().getString(R.string.stringNoAssignments));

        String[] from = new String[]{"name"};
        int[] to = new int[]{android.R.id.text1};

        assignmentAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, null, from, to, 0);
        setListAdapter(assignmentAdapter);
    }

    public void updateAssignmentList()
    {
        new getCourseAssignmentsTask().execute(Long.toString(rowID));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        new GetAssignmentTask().execute((Object[]) null);
        new getCourseAssignmentsTask().execute(Long.toString(rowID));
    }

    @Override
    public void onStop()
    {
        Cursor cursor = assignmentAdapter.getCursor();
        assignmentAdapter.changeCursor(null);

        if (cursor != null)
            cursor.close();

        super.onStop();
    }

    private Assignment[] jsonParse(String rawJson)
    {
        GsonBuilder gsonb = new GsonBuilder();
        Gson gson = gsonb.create();

        Assignment[] assignments = null;

        try
        {
            assignments = gson.fromJson(rawJson, Assignment[].class);
        } catch (Exception ignored)
        {

        }
        return assignments;
    }

    private class GetAssignmentTask extends AsyncTask<Object, Object, Cursor>
    {
        final DatabaseHelper databaseConnector =
                new DatabaseHelper(getActivity());

        @Override
        protected Cursor doInBackground(Object... params)
        {
            databaseConnector.open();
            return databaseConnector.getAllAssignments(rowID);
        }

        @Override
        protected void onPostExecute(Cursor result)
        {
            assignmentAdapter.changeCursor(result);
            databaseConnector.close();
        }
    }

    protected class getCourseAssignmentsTask extends AsyncTask<String, Integer, String>
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
                URL url = new URL("https://weber.instructure.com/api/v1/courses/" + params[0] + "/assignments");
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

            //databaseHelper.open();

            try
            {
                Assignment[] assignments = jsonParse(result);
                for (Assignment assignment : assignments)
                {
                    databaseHelper.insertAssignment(assignment.id, assignment.name, assignment.due_at);
                }
            } catch (Exception ignored)
            {

            }
            updateAssignmentList();
            databaseHelper.close();
        }


        private int getRowCourseId(int id)
        {
            // databaseHelper.open();
            Cursor course = databaseHelper.getOneCourse(id);

            course.moveToFirst();

            int courseCode = course.getColumnIndex("course_code");

            course.close();
            //databaseHelper.close();

            return courseCode;
        }
    }

    class Assignment
    {
        String id;
        String name;
        String due_at;
    }
}
