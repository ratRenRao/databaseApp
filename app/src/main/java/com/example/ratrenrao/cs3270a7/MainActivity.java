package com.example.ratrenrao.cs3270a7;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements
        CoursesFragment.CourseListFragmentListener,
        InfoFragment.InfoFragmentListener,
        UpdateDatabaseFragment.UpdateListener
{

    public static final String ROW_ID = "row_id";

    CoursesFragment coursesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null)
            return;

        if (findViewById(R.id.fragmentMainContainer) != null)
        {
            coursesFragment = new CoursesFragment();

            FragmentTransaction transaction =
                    getFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentMainContainer, coursesFragment);
            transaction.commit();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void onCourseSelected(long rowId)
    {
        displayCourse(rowId, R.id.fragmentMainContainer);
    }

    @Override
    public void onCourseLongSelected(long rowId)
    {
        displayAssignments(rowId, R.id.fragmentMainContainer);
    }

    private void displayAssignments(long rowId, int viewId)
    {
        CoursesFragment clf = new CoursesFragment();
        clf.onGetImportAssignments();
    }

    private void displayCourse(long rowId, int viewId)
    {
        InfoFragment infoFragment = new InfoFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowId);
        infoFragment.setArguments(arguments);

        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(viewId, infoFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onAddCourse()
    {
        displayUpdateFragment(R.id.fragmentMainContainer, null);
    }

    private void displayUpdateFragment(int viewId, Bundle arguments)
    {
        UpdateDatabaseFragment updateDatabaseFragment = new UpdateDatabaseFragment();

        if (arguments != null)
            updateDatabaseFragment.setArguments(arguments);

        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(viewId, updateDatabaseFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCourseDelete()
    {
        getFragmentManager().popBackStack();

        coursesFragment.updateCourseList();
    }

    @Override
    public void onEditCourse(Bundle arguments)
    {
        displayUpdateFragment(R.id.fragmentMainContainer, arguments);
    }

    @Override
    public void onUpdate(long rowId)
    {
        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        coursesFragment.updateCourseList();
    }
}