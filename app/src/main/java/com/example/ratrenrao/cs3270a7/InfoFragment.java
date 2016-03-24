package com.example.ratrenrao.cs3270a7;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class InfoFragment extends android.app.Fragment
{
    public interface InfoFragmentListener
    {
        void onCourseDelete();

        void onEditCourse(Bundle arguments);
    }

    private InfoFragmentListener infoListener;

    private long rowID = -1;
    private TextView textId;
    private TextView textName;
    private TextView textCourseCode;
    private TextView textStart;
    private TextView textEnd;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof Activity)
            infoListener = (InfoFragmentListener) context;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        infoListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null)
            rowID = savedInstanceState.getLong(MainActivity.ROW_ID);
        else
        {
            Bundle arguments = getArguments();

            if (arguments != null)
                rowID = arguments.getLong(MainActivity.ROW_ID);
        }

        View view =
                inflater.inflate(R.layout.fragment_info, container, false);
        setHasOptionsMenu(true);

        textId = (TextView) view.findViewById(R.id.textIdValue);
        textName = (TextView) view.findViewById(R.id.textNameValue);
        textCourseCode = (TextView) view.findViewById(R.id.textCourseCodeValue);
        textStart = (TextView) view.findViewById(R.id.textStartValue);
        textEnd = (TextView) view.findViewById(R.id.textEndValue);
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        new LoadCourseTask().execute(rowID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(MainActivity.ROW_ID, rowID);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.info_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.edit:
                Bundle arguments = new Bundle();
                arguments.putLong(MainActivity.ROW_ID, rowID);
                arguments.putCharSequence("id", textId.getText());
                arguments.putCharSequence("name", textName.getText());
                arguments.putCharSequence("course_code", textCourseCode.getText());
                arguments.putCharSequence("start_at", textStart.getText());
                arguments.putCharSequence("end_at", textEnd.getText());
                infoListener.onEditCourse(arguments); // pass Bundle to infoListener
                return true;
            case R.id.delete:
                deleteCourse();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadCourseTask extends AsyncTask<Long, Object, Cursor>
    {
        final DatabaseHelper databaseHelper =
                new DatabaseHelper(getActivity());

        @Override
        protected Cursor doInBackground(Long... params)
        {
            databaseHelper.open();
            return databaseHelper.getOneCourse(params[0]);
        }

        @Override
        protected void onPostExecute(Cursor result)
        {
            super.onPostExecute(result);
            result.moveToFirst();

            int idIndex = result.getColumnIndex("id");
            int nameIndex = result.getColumnIndex("name");
            int courseCodeIndex = result.getColumnIndex("course_code");
            int startIndex = result.getColumnIndex("start_at");
            int endIndex = result.getColumnIndex("end_at");

            textId.setText(result.getString(idIndex));
            textName.setText(result.getString(nameIndex));
            textCourseCode.setText(result.getString(courseCodeIndex));
            textStart.setText(result.getString(startIndex));
            textEnd.setText(result.getString(endIndex));

            result.close();
            databaseHelper.close();
        }
    }

    private void deleteCourse()
    {
        confirmDelete.show(getFragmentManager(), "confirm delete");
    }

    private final DialogFragment confirmDelete =
            new DialogFragment()
            {
                // create an AlertDialog and return it
                @Override
                public Dialog onCreateDialog(Bundle bundle)
                {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(getActivity());

                    builder.setTitle(R.string.stringConfirm);
                    builder.setMessage(R.string.stringConfirmMessage);

                    // provide an OK button that simply dismisses the dialog
                    builder.setPositiveButton(R.string.stringDelete,
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(
                                        DialogInterface dialog, int button)
                                {
                                    final DatabaseHelper databaseConnector =
                                            new DatabaseHelper(getActivity());

                                    AsyncTask<Long, Object, Object> deleteTask =
                                            new AsyncTask<Long, Object, Object>()
                                            {
                                                @Override
                                                protected Object doInBackground(Long... params)
                                                {
                                                    databaseConnector.deleteCourse(params[0]);
                                                    return null;
                                                }

                                                @Override
                                                protected void onPostExecute(Object result)
                                                {
                                                    infoListener.onCourseDelete();
                                                }
                                            };

                                    deleteTask.execute(rowID);
                                }
                            }
                    ).setNegativeButton(R.string.stringCancel, null);

                    return builder.create();
                }
            };
}
