package com.example.ratrenrao.cs3270a7;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements
        CoursesFragment.CourseListFragmentListener,
        InfoFragment.InfoFragmentListener,
        UpdateDatabaseFragment.UpdateListener
{

    public static final String ROW_ID = "row_id";

    private CoursesFragment coursesFragment;
    private AssignmentFragment assignmentFragment;
    private InfoFragment infoFragment;
    private boolean landscapable = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changeToLandscape();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            changeToPortrait();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null)
            return;

        if (findViewById(R.id.fragmentMainContainer1) != null)
        {
            coursesFragment = new CoursesFragment();

            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentMainContainer1, coursesFragment, "CF")
                    .add(R.id.fragmentMainContainer2, new BlankFragment(), "NN")
                    .commit();
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

    private void changeToLandscape()
    {
        if (getFragmentManager().findFragmentByTag("IF") != null)
            findViewById(R.id.fragmentMainContainer2).setVisibility(View.VISIBLE);
        else if (getFragmentManager().findFragmentByTag("AF") != null)
        {
            findViewById(R.id.fragmentMainContainer1).setVisibility(View.VISIBLE);

            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentMainContainer1, infoFragment, "NN")
                    .addToBackStack(null)
                    .replace(R.id.fragmentMainContainer2, assignmentFragment, "AF")
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void changeToPortrait()
    {
        if (getFragmentManager().findFragmentByTag("IF") != null)
            findViewById(R.id.fragmentMainContainer2).setVisibility(View.GONE);
        else if (getFragmentManager().findFragmentByTag("AF") != null)
        {
            findViewById(R.id.fragmentMainContainer2).setVisibility(View.GONE);

            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentMainContainer1, assignmentFragment, "AF")
                    .addToBackStack(null)
                    .replace(R.id.fragmentMainContainer2, infoFragment, "NN")
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void displayAssignments(long rowId)
    {
        createAssignmentFragment(rowId);
        createInfoFragment(rowId);

        getFragmentManager().beginTransaction()
            .replace(R.id.fragmentMainContainer1, assignmentFragment, "AF")
            .addToBackStack(null)
            .replace(R.id.fragmentMainContainer2, infoFragment, "NN")
            .addToBackStack(null)
            .commit();
    }

    private void createAssignmentFragment(long rowId)
    {
        assignmentFragment = new AssignmentFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowId);
        assignmentFragment.setArguments(arguments);
    }

    private void displayCourse(long rowId)
    {
        createInfoFragment(rowId);
        createAssignmentFragment(rowId);

        getFragmentManager().beginTransaction()
            .replace(R.id.fragmentMainContainer1, infoFragment, "IF")
            .addToBackStack(null)
            .replace(R.id.fragmentMainContainer2, assignmentFragment, "NN")
            .addToBackStack(null)
            .commit();
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
                .replace(R.id.fragmentMainContainer1, updateDatabaseFragment, "UD")
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
        getFragmentManager().popBackStack();
        coursesFragment.updateCourseList();
    }
}