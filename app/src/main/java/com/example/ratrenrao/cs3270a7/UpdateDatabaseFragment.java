package com.example.ratrenrao.cs3270a7;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;


public class UpdateDatabaseFragment extends android.app.Fragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_update_database, container, false);

        idEditText = (EditText) view.findViewById(R.id.editId);
        nameEditText = (EditText) view.findViewById(R.id.editName);
        courseCodeEditText = (EditText) view.findViewById(R.id.editCourseCode);
        startEditText = (EditText) view.findViewById(R.id.editStart);
        endEditText = (EditText) view.findViewById(R.id.editEnd);

        courseInfoBundle = getArguments();

        if (courseInfoBundle != null)
        {
            rowID = courseInfoBundle.getLong(MainActivity.ROW_ID);
            idEditText.setText(courseInfoBundle.getString("id"));
            nameEditText.setText(courseInfoBundle.getString("name"));
            courseCodeEditText.setText(courseInfoBundle.getString("course_code"));
            startEditText.setText(courseInfoBundle.getString("start_at"));
            endEditText.setText(courseInfoBundle.getString("end_at"));
        }

        Button buttonSave =
                (Button) view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(saveCourseButtonClicked);
        return view;
    }

    public interface UpdateListener
    {
        void onUpdate(long rowID);
    }

    private UpdateListener listener;

    private long rowID;
    private Bundle courseInfoBundle;

    private EditText idEditText;
    private EditText nameEditText;
    private EditText courseCodeEditText;
    private EditText startEditText;
    private EditText endEditText;

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

    View.OnClickListener saveCourseButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (nameEditText.getText().toString().trim().length() != 0)
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

                                listener.onUpdate(rowID);
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
        DatabaseHelper databaseConnector =
                new DatabaseHelper(getActivity());

        if (courseInfoBundle == null)
        {
            rowID = databaseConnector.insertCourse(
                    idEditText.getText().toString(),
                    nameEditText.getText().toString(),
                    courseCodeEditText.getText().toString(),
                    startEditText.getText().toString(),
                    endEditText.getText().toString()
            );
        } else
        {
            databaseConnector.updateCourse(rowID,
                    idEditText.getText().toString(),
                    nameEditText.getText().toString(),
                    courseCodeEditText.getText().toString(),
                    startEditText.getText().toString(),
                    endEditText.getText().toString());
        }
    }
}
