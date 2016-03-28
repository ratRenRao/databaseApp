package com.example.ratrenrao.cs3270a7;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class MainActivity extends AppCompatActivity implements
        CoursesFragment.CourseListFragmentListener,
        InfoFragment.InfoFragmentListener,
        UpdateDatabaseFragment.UpdateListener
{

    public static final String ROW_ID = "row_id";

    private CoursesFragment coursesFragment;
    private AssignmentFragment assignmentFragment;
    private InfoFragment infoFragment;
    private boolean landscape = false;

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

            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentMainContainer, coursesFragment)
                    .commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            landscape = true;
            changeToLandscape();
            findViewById(R.id.fragmentMainContainer2).setVisibility(View.VISIBLE);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            landscape = false;
            changeToPortrait();
            findViewById(R.id.fragmentMainContainer2).setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (getFragmentManager().getBackStackEntryCount() <= 0)
            finish();
        getFragmentManager().popBackStack();
        if (landscape)
        {
            getFragmentManager().popBackStack();
            getFragmentManager().popBackStack();
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
        displayInfo(rowId);
    }

    @Override
    public void onCourseLongSelected(long rowId)
    {
        displayAssignments(rowId);
    }

    private void changeToLandscape()
    {
        getFragmentManager().beginTransaction()
                .add(R.id.fragmentMainContainer2, new BlankFragment())
                .commit();

        if (getFragmentManager().findFragmentByTag("IF") != null)
        {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragmentMainContainer2, assignmentFragment)
                .addToBackStack(null)
                .commit();
        }
        else if (getFragmentManager().findFragmentByTag("AF") != null)
        {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentMainContainer2, infoFragment)
                    .replace(R.id.fragmentMainContainer, assignmentFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void changeToPortrait()
    {
        getFragmentManager().popBackStack();

        getFragmentManager().beginTransaction()
                .replace(R.id.fragmentMainContainer2, new BlankFragment())
                .commit();
    }

    private void displayAssignments(long rowId)
    {
        createAssignmentFragment(rowId);
        createInfoFragment(rowId);

        if (landscape)
        {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentMainContainer, infoFragment)
                    .replace(R.id.fragmentMainContainer2, assignmentFragment, "AF")
                    .addToBackStack(null)
                    .commit();

        }
        else
        {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentMainContainer, assignmentFragment, "AF")
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void createAssignmentFragment(long rowId)
    {
        assignmentFragment = new AssignmentFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowId);
        assignmentFragment.setArguments(arguments);
    }

    private void displayInfo(long rowId)
    {
        createInfoFragment(rowId);
        createAssignmentFragment(rowId);

        if (landscape)
        {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentMainContainer, infoFragment, "IF")
                    .replace(R.id.fragmentMainContainer2, assignmentFragment)
                    .addToBackStack(null)
                    .commit();
        }
        else
        {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentMainContainer, infoFragment, "IF")
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void createInfoFragment(long rowId)
    {
        infoFragment = new InfoFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowId);
        infoFragment.setArguments(arguments);
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

        getFragmentManager().beginTransaction()
                .replace(R.id.fragmentMainContainer, updateDatabaseFragment, "UD")
                .addToBackStack(null).commit();
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
        displayUpdateFragment(arguments);
    }

    @Override
    public void onUpdated(long Id)
    {
        getFragmentManager().popBackStack();
        coursesFragment.updateCourseList();
    }
}