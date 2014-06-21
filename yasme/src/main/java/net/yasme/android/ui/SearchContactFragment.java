package net.yasme.android.ui;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.yasme.android.R;

public class SearchContactFragment extends Fragment implements ContactListItemFragment.OnFragmentInteractionListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_search_contact, null);

        return layout;
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
