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
        // callback methods implemented by MainActivity
        public interface InfoFragmentListener
        {
                // called when a course is deleted
                void onCourseDelete();

                // called to pass Bundle of course's info for editing
                void onEditCourse(Bundle arguments);
        }

        private InfoFragmentListener infoListener;

        private long rowID = -1; // selected course's rowID
        private TextView textId; // displays course's id
        private TextView textName; // displays course's name
        private TextView textCourseCode; // displays course's course code
        private TextView textStart; // displays course's start at
        private TextView textEnd; // displays course's end at

        // set InfoFragmentListener when fragment attached
        @Override
        public void onAttach(Context context)
        {
                super.onAttach(context);
                if (context instanceof Activity)
                    infoListener = (InfoFragmentListener) context;
        }

        // remove InfoFragmentListener when fragment detached
        @Override
        public void onDetach()
        {
                super.onDetach();
                infoListener = null;
        }

        // called when InfoFragmentListener's view needs to be created
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
                super.onCreateView(inflater, container, savedInstanceState);
                setRetainInstance(true); // save fragment across config changes

                // if DetailsFragment is being restored, get saved row ID
                if (savedInstanceState != null)
                        rowID = savedInstanceState.getLong(MainActivity.ROW_ID);
                else
                {
                        // get Bundle of arguments then extract the course's row ID
                        Bundle arguments = getArguments();

                        if (arguments != null)
                                rowID = arguments.getLong(MainActivity.ROW_ID);
                }

                // inflate DetailsFragment's layout
                View view =
                        inflater.inflate(R.layout.fragment_info, container, false);
                setHasOptionsMenu(true); // this fragment has menu items to display

                // get the EditTexts
                textId = (TextView) view.findViewById(R.id.textIdValue);
                textName = (TextView) view.findViewById(R.id.textNameValue);
                textCourseCode = (TextView) view.findViewById(R.id.textCourseCodeValue);
                textStart = (TextView) view.findViewById(R.id.textStartValue);
                textEnd = (TextView) view.findViewById(R.id.textEndValue);
                return view;
        }

        // called when the DetailsFragment resumes
        @Override
        public void onResume()
        {
                super.onResume();
                new LoadCourseTask().execute(rowID); // load course at rowID
        }

        // save currently displayed course's row ID
        @Override
        public void onSaveInstanceState(Bundle outState)
        {
                super.onSaveInstanceState(outState);
                outState.putLong(MainActivity.ROW_ID, rowID);
        }

        // display this fragment's menu items
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
        {
                super.onCreateOptionsMenu(menu, inflater);
                inflater.inflate(R.menu.info_menu, menu);
        }

        // handle menu item selections
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
                switch (item.getItemId())
                {
                        case R.id.edit:
                                // create Bundle containing course data to edit
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

        // performs database query outside GUI thread
        private class LoadCourseTask extends AsyncTask<Long, Object, Cursor>
        {
                DatabaseHelper databaseHelper =
                        new DatabaseHelper(getActivity());

                // open database & get Cursor representing specified course's data
                @Override
                protected Cursor doInBackground(Long... params)
                {
                        databaseHelper.open();
                        return databaseHelper.getOneCourse(params[0]);
                }

                // use the Cursor returned from the doInBackground method
                @Override
                protected void onPostExecute(Cursor result)
                {
                        super.onPostExecute(result);
                        result.moveToFirst(); // move to the first item

                        // get the column index for each data item
                        int idIndex = result.getColumnIndex("id");
                        int nameIndex = result.getColumnIndex("name");
                        int courseCodeIndex = result.getColumnIndex("course_code");
                        int startIndex = result.getColumnIndex("start_at");
                        int endIndex = result.getColumnIndex("end_at");

                        // fill TextViews with the retrieved data
                        textId.setText(result.getString(idIndex));
                        textName.setText(result.getString(nameIndex));
                        textCourseCode.setText(result.getString(courseCodeIndex));
                        textStart.setText(result.getString(startIndex));
                        textEnd.setText(result.getString(endIndex));

                        result.close(); // close the result cursor
                        databaseHelper.close(); // close database connection
                } // end method onPostExecute
        } // end class LoadcourseTask

        // delete a course
        private void deleteCourse()
        {
                // use FragmentManager to display the confirmDelete DialogFragment
                confirmDelete.show(getFragmentManager(), "confirm delete");
        }

        // DialogFragment to confirm deletion of course
        private DialogFragment confirmDelete =
                new DialogFragment()
                {
                        // create an AlertDialog and return it
                        @Override
                        public Dialog onCreateDialog(Bundle bundle)
                        {
                                // create a new AlertDialog Builder
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

                                                        // AsyncTask deletes course and notifies infoListener
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
                                                                }; // end new AsyncTask

                                                        // execute the AsyncTask to delete course at rowID
                                                        deleteTask.execute(new Long[]{rowID});
                                                } // end method onClick
                                        } // end anonymous inner class
                                ).setNegativeButton(R.string.stringCancel, null); // end call to method setPositiveButton

                                return builder.create(); // return the AlertDialog
                        }
                };
}
