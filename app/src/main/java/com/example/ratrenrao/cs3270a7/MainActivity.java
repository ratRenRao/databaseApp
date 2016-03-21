package com.example.ratrenrao.cs3270a7;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements
        CoursesFragment.CourseListFragmentListener,
        InfoFragment.InfoFragmentListener,
        UpdateDatabaseFragment.UpdateListener {

    public static final String ROW_ID = "row_id";

    CoursesFragment coursesFragment; // displays course list

    // display courseListFragment when MainActivity first loads
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // return if Activity is being restored, no need to recreate GUI
        if (savedInstanceState != null)
            return;

        // check whether layout contains fragmentMainContainer (phone layout);
        // courseListFragment is always displayed
        if (findViewById(R.id.fragmentMainContainer) != null)
        {
            // create courseListFragment
            coursesFragment = new CoursesFragment();

            // add the fragment to the FrameLayout
            FragmentTransaction transaction =
                    getFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentMainContainer, coursesFragment);
            transaction.commit(); // causes courseListFragment to display
        }
    }

    // called when MainActivity resumes
    @Override
    protected void onResume()
    {
        super.onResume();
    }

    // display DetailsFragment for selected course
    @Override
    public void onCourseSelected(long rowId)
    {
        displayCourse(rowId, R.id.fragmentMainContainer);
    }

    @Override
    public void onCourseLongSelected(long rowId) { displayAssignments(rowId, R.id.fragmentMainContainer); }

    // display a course
    private void displayAssignments(long rowId, int viewId){
        CoursesFragment clf = new CoursesFragment();
        clf.onGetImportAssignments();
    }

    private void displayCourse(long rowId, int viewId)
    {
        InfoFragment infoFragment = new InfoFragment();

        // specify rowId as an argument to the DetailsFragment
        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowId);
        infoFragment.setArguments(arguments);

        // use a FragmentTransaction to display the DetailsFragment
        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(viewId, infoFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes DetailsFragment to display
    }

    // display the AddEditFragment to add a new course
    @Override
    public void onAddCourse()
    {
        displayUpdateFragment(R.id.fragmentMainContainer, null);
    }

    // display fragment for adding a new or editing an existing course
    private void displayUpdateFragment(int viewId, Bundle arguments)
    {
        UpdateDatabaseFragment updateDatabaseFragment = new UpdateDatabaseFragment();

        if (arguments != null) // editing existing course
            updateDatabaseFragment.setArguments(arguments);

        // use a FragmentTransaction to display the AddEditFragment
        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(viewId, updateDatabaseFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes AddEditFragment to display
    }

    // return to course list when displayed course deleted
    @Override
    public void onCourseDelete()
    {
        getFragmentManager().popBackStack(); // removes top of back stack

        coursesFragment.updateCourseList();
    }

    // display the AddEditFragment to edit an existing course
    @Override
    public void onEditCourse(Bundle arguments)
    {
        displayUpdateFragment(R.id.fragmentMainContainer, arguments);
    }

    // update GUI after new course or updated course saved
    @Override
    public void onUpdate(long rowId)
    {
        getFragmentManager().popBackStack(); // removes top of back stack
        getFragmentManager().popBackStack(); // removes top of back stack
        coursesFragment.updateCourseList(); // refresh courses
    }
}