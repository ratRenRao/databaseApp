package com.example.ratrenrao.cs3270a7;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
                public void onCourseDelete();

                // called to pass Bundle of course's info for editing
                public void onEditCourse(Bundle arguments);
        }

        private InfoFragmentListener listener;

        private long rowID = -1; // selected course's rowID
        private TextView idTextView; // displays course's id
        private TextView nameTextView; // displays course's name
        private TextView courseCodeTextView; // displays course's course code
        private TextView startTextView; // displays course's start at
        private TextView endTextView; // displays course's end at

        // set InfoFragmentListener when fragment attached
        @Override
        public void onAttach(Activity activity)
        {
                super.onAttach(activity);
                listener = (InfoFragmentListener) activity;
        }

        // remove InfoFragmentListener when fragment detached
        @Override
        public void onDetach()
        {
                super.onDetach();
                listener = null;
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
                idTextView = (TextView) view.findViewById(R.id.textIdValue);
                nameTextView = (TextView) view.findViewById(R.id.textNameValue);
                courseCodeTextView = (TextView) view.findViewById(R.id.textCourseCodeValue);
                startTextView = (TextView) view.findViewById(R.id.textStartValue);
                endTextView = (TextView) view.findViewById(R.id.textEndValue);
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
                        case R.id.action_edit:
                                // create Bundle containing course data to edit
                                Bundle arguments = new Bundle();
                                arguments.putLong(MainActivity.ROW_ID, rowID);
                                arguments.putCharSequence("id", idTextView.getText());
                                arguments.putCharSequence("name", nameTextView.getText());
                                arguments.putCharSequence("course_code", courseCodeTextView.getText());
                                arguments.putCharSequence("start_at", startTextView.getText());
                                arguments.putCharSequence("end_at", endTextView.getText());
                                listener.onEditCourse(arguments); // pass Bundle to listener
                                return true;
                        case R.id.action_delete:
                                deleteCourse();
                                return true;
                }

                return super.onOptionsItemSelected(item);
        }

        // performs database query outside GUI thread
        private class LoadCourseTask extends AsyncTask<Long, Object, Cursor>
        {
                DatabaseFragment databaseConnector =
                        new DatabaseFragment(getActivity());

                // open database & get Cursor representing specified course's data
                @Override
                protected Cursor doInBackground(Long... params)
                {
                        databaseConnector.open();
                        return databaseConnector.getOneCourse(params[0]);
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
                        idTextView.setText(result.getString(idIndex));
                        nameTextView.setText(result.getString(nameIndex));
                        courseCodeTextView.setText(result.getString(courseCodeIndex));
                        startTextView.setText(result.getString(startIndex));
                        endTextView.setText(result.getString(endIndex));

                        result.close(); // close the result cursor
                        databaseConnector.close(); // close database connection
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
                                                        final DatabaseFragment databaseConnector =
                                                                new DatabaseFragment(getActivity());

                                                        // AsyncTask deletes course and notifies listener
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
                                                                                listener.onCourseDelete();
                                                                        }
                                                                }; // end new AsyncTask

                                                        // execute the AsyncTask to delete course at rowID
                                                        deleteTask.execute(new Long[] { rowID });
                                                } // end method onClick
                                        } // end anonymous inner class
                                ); // end call to method setPositiveButton

                                builder.setNegativeButton(R.string.stringCancel, null);
                                return builder.create(); // return the AlertDialog
                        }
                }; // end DialogFragment anonymous inner class
} // end class DetailsFragment
