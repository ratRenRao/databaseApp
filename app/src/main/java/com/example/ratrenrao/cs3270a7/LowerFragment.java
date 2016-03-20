package com.example.ratrenrao.cs3270a7;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class LowerFragment extends android.app.Fragment
{

    View rootView;

    public LowerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_lower, container, false);

        DatabaseFragment dbHelper = new DatabaseFragment(getActivity(),"Music",null,1);
        Cursor cursor = dbHelper.getAllSongs();
        String[] columns = new String[] {"title"};
        int[] views = new int[] {android.R.id.text1};
        SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1,
                        cursor, columns, views,
                        CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(adapter);

        return rootView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        MainActivity ma = (MainActivity) getActivity();
        ma.populateSong(id);
    }
}
