package com.example.ratrenrao.cs3270a7;

import android.app.ActionBar;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Text;

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

        String[] from = new String[]{"label"};
        int[] to = new int[]{android.R.id.text1};

        assignmentAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.activity_list_item, null, from, to, 0);
        setListAdapter(assignmentAdapter);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        new GetAssignmentsDb().execute((Object[]) null);
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

    private class GetAssignmentsDb extends AsyncTask<Object, Object, Cursor>
    {
        final DatabaseHelper databaseHelper =
                new DatabaseHelper(getActivity());

        @Override
        protected Cursor doInBackground(Object... params)
        {
            databaseHelper.open();
            return databaseHelper.getAllAssignments(rowID);
        }

        @Override
        protected void onPostExecute(Cursor result)
        {
            assignmentAdapter.changeCursor(result);
            databaseHelper.close();
        }
    }

}
