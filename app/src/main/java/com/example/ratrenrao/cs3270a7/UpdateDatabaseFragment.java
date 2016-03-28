package com.example.ratrenrao.cs3270a7;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;


public class UpdateDatabaseFragment extends Fragment
{

    private UpdateListener listener;

    private long rowID;
    private Bundle courseInfoBundle;

    private EditText editId;
    private EditText editName;
    private EditText editCourseCode;
    private EditText editStart;
    private EditText editEnd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_update_database, container, false);

        editId = (EditText) view.findViewById(R.id.editId);
        editName = (EditText) view.findViewById(R.id.editName);
        editCourseCode = (EditText) view.findViewById(R.id.editCourseCode);
        editStart = (EditText) view.findViewById(R.id.editStart);
        editEnd = (EditText) view.findViewById(R.id.editEnd);

        courseInfoBundle = getArguments();

        if (courseInfoBundle != null)
        {
            rowID = courseInfoBundle.getLong(MainActivity.ROW_ID);
            editId.setText(courseInfoBundle.getString("id"));
            editName.setText(courseInfoBundle.getString("name"));
            editCourseCode.setText(courseInfoBundle.getString("course_code"));
            editStart.setText(courseInfoBundle.getString("start_at"));
            editEnd.setText(courseInfoBundle.getString("end_at"));
        }

        Button buttonSave =
                (Button) view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(saveCourseButtonClicked);
        return view;
    }

    public interface UpdateListener
    {
        void onUpdated(long id);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof Activity)
            listener = (UpdateListener) context;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }

    private final View.OnClickListener saveCourseButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (editName.getText().toString().trim().length() != 0)
            {
                AsyncTask<Object, Object, Object> savecourseTask =
                        new AsyncTask<Object, Object, Object>()
                        {
                            @Override
                            protected Object doInBackground(Object... params)
                            {
                                saveCourse();
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object result)
                            {
                                InputMethodManager imm = (InputMethodManager)
                                        getActivity().getSystemService(
                                                Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(
                                        getView().getWindowToken(), 0);

                                listener.onUpdated(rowID);
                            }
                        };

                savecourseTask.execute((Object[]) null);
            } else
            {
                DialogFragment errorSaving =
                        new DialogFragment()
                        {
                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState)
                            {
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(getActivity());
                                builder.setMessage(R.string.stringError);
                                builder.setPositiveButton("OK", null);
                                return builder.create();
                            }
                        };

                errorSaving.show(getFragmentManager(), "error saving course");
            }
        }
    };

    private void saveCourse()
    {
        DatabaseHelper databaseHelper =
                new DatabaseHelper(getActivity());

        if (courseInfoBundle == null)
        {
            rowID = databaseHelper.insertCourse(
                    editId.getText().toString(),
                    editName.getText().toString(),
                    editCourseCode.getText().toString(),
                    editStart.getText().toString(),
                    editEnd.getText().toString()
            );
        } else
        {
            databaseHelper.updateCourse(rowID,
                    editId.getText().toString(),
                    editName.getText().toString(),
                    editCourseCode.getText().toString(),
                    editStart.getText().toString(),
                    editEnd.getText().toString());
        }
    }
}
