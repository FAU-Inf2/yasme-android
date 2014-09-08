package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetContactsTask;
import de.fau.cs.mad.yasme.android.contacts.ContactListContent;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.ui.UserAdapter;
import de.fau.cs.mad.yasme.android.ui.fragments.InviteToChatFragment.AllUsersFetchedParam;

/**
 * Created by Stefan Ettl <stefan.ettl@fau.de>
 */

public class ContactListFragment extends Fragment implements AbsListView.OnItemClickListener, NotifiableFragment<AllUsersFetchedParam> {


    private OnFragmentInteractionListener mListener;
    private ContactListContent contactListContent;
    private List<User> contacts;

    //The fragment's ListView/GridView.
    private AbsListView mListView;

    //The Adapter which will be used to populate the ListView/GridView with Views.
    private UserAdapter mAdapter;

    //private AtomicInteger bgTasksRunning = new AtomicInteger(0);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactListFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contacts = new ArrayList<>();
        contactListContent = new ContactListContent();

        mAdapter = new UserAdapter(getActivity(), R.layout.user_item, contacts);
                /*new SimpleAdapter(
                getActivity(),
                contactListContent.getMap(),
                android.R.layout.simple_list_item_2,
                new String[] {"name", "email"},
                new int[]{ android.R.id.text1, android.R.id.text2});*/

        new GetContactsTask(this.getClass()).execute();

    }


    @Override
    public void onStart() {
        super.onStart();
        FragmentObservable<ContactListFragment, AllUsersFetchedParam> obs = ObservableRegistry.getObservable(ContactListFragment.class);
        obs.register(this);
    }


    @Override
    public void onStop() {
        FragmentObservable<ContactListFragment, AllUsersFetchedParam> obs = ObservableRegistry.getObservable(ContactListFragment.class);
        obs.remove(this);
        super.onStop();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contactlistitem, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        TextView emptyContactsNotice = (TextView) view.findViewById(R.id.empty_contacts_notice_swipe);
        mListView.setEmptyView(emptyContactsNotice);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.

            //mListener.onFragmentInteraction(contactListContent.items.get(position).user, view);
            mListener.onFragmentInteraction(contacts.get(position), view);
        }
    }

    public void notifyFragment(InviteToChatFragment.AllUsersFetchedParam param) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");

        if (param.getSuccess()) {
            /*contactListContent.clearItems();
            for (User u : param.getAllUsers()) {
                contactListContent.addItem(new ContactListContent.ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
            }*/
            mAdapter.clear();
            mAdapter.addAll(param.getAllUsers());
            contacts = param.getAllUsers();
            mAdapter.notifyDataSetChanged();
        } else {
            Log.e(this.getClass().getSimpleName(), "Failed to fetch all users");
        }
    }


    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(User user, View view);
    }
}
