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

    private CoursesFragment coursesFragment;
    private AssignmentFragment assignmentFragment;

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
        displayCourse(rowId);
    }

    @Override
    public void onCourseLongSelected(long rowId)
    {
        displayAssignments(rowId);
    }

    private void displayAssignments(long rowId)
    {
        //CoursesFragment clf = new CoursesFragment();
        //new CoursesFragment().onGetImportAssignments(rowId);

        assignmentFragment = new AssignmentFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowId);
        assignmentFragment.setArguments(arguments);

        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentMainContainer, assignmentFragment).addToBackStack(null).commit();
    }

    private void displayCourse(long rowId)
    {
        InfoFragment infoFragment = new InfoFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowId);
        infoFragment.setArguments(arguments);

        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentMainContainer, infoFragment).addToBackStack(null).commit();
    }

    @Override
    public void onAddCourse()
    {
        displayUpdateFragment(null);
    }

    private void displayUpdateFragment(Bundle arguments)
    {
        UpdateDatabaseFragment updateDatabaseFragment = new UpdateDatabaseFragment();

        if (arguments != null)
            updateDatabaseFragment.setArguments(arguments);

        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentMainContainer, updateDatabaseFragment).addToBackStack(null).commit();
    }

    @Override
    public void onCourseDelete()
    {
        //getFragmentManager().popBackStack();

        coursesFragment.updateCourseList();
    }

    @Override
    public void onEditCourse(Bundle arguments)
    {
        displayUpdateFragment(arguments);
    }

    @Override
    public void onUpdated(long Id)
    {
        //getFragmentManager().popBackStack();
        //getFragmentManager().popBackStack();
        coursesFragment.updateCourseList();
    }
}