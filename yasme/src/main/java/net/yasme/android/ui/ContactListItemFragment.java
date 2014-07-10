package net.yasme.android.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import net.yasme.android.R;
import net.yasme.android.connection.SearchTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContactListItemFragment extends Fragment implements AbsListView.OnItemClickListener, NotifiableFragment<ContactListItemFragment.ContactListItemParam> {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Long userId;
    private String accessToken;

    private ContactListContent contactListContent;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    //private ListAdapter mAdapter;
    private SimpleAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static ContactListItemFragment newInstance(String param1, String param2) {
        ContactListItemFragment fragment = new ContactListItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactListItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Bundle b = this.getArguments();
        userId = b.getLong("userId");
        accessToken = b.getString("accessToken");

         //mAdapter = new ArrayAdapter<ContactListContent.ContactListItem>(getActivity(),
         //       android.R.layout.simple_list_item_1, android.R.id.text1,ContactListContent.ITEMS);

        contactListContent = new ContactListContent();

        //User temp = new User("Stefan","stefan@yasme.net",4);
        //ContactListContent.ContactListItem item = new ContactListContent.ContactListItem(String.valueOf(temp.getId()),temp.getName(),temp.getEmail(),temp);

        //contactListContent.addItem(item);


        mAdapter = new SimpleAdapter((ContactActivity)getActivity() ,
                contactListContent.getMap(), android.R.layout.simple_list_item_2, new String[] {"name","mail"}, new int[]{android.R.id.text1,android.R.id.text2});

        DownloadAllUsers task = new DownloadAllUsers();
        task.execute();

        //this.getContacts();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contactlistitem, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);


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

            mListener.onFragmentInteraction(contactListContent.items.get(position).user, view);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
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


    private void getContacts(){

        DatabaseManager db = DatabaseManager.INSTANCE;

        List<User> userList = db.getContactsFromDB();

        if (userList != null){
            for(User u:userList){
                contactListContent.addItem(new ContactListContent.ContactListItem(String.valueOf(u.getId()),u.getName(),u.getEmail(),u));
            }
            mAdapter.notifyDataSetChanged();
        }

    }

    public void notifyFragment(ContactListItemParam param) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
    }


    public static class ContactListItemParam {
        private Boolean success;
        private Long userId;
        private String accessToken;

        public ContactListItemParam(Boolean success, Long userId, String accessToken) {
            this.success = success;
            this.userId = userId;
            this.accessToken = accessToken;
        }

        public Long getUserId() {
            return userId;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public Boolean getSuccess() {
            return success;
        }
    }


    private class DownloadAllUsers extends AsyncTask<String,Void,List<User>>{

        @Override
        protected List<User> doInBackground(String... params) {

            SearchTask search = SearchTask.getInstance();
            List<User> userList = null;

            try {
                userList = search.getAllUsers();
            }catch (RestServiceException rse){
                rse.getMessage();
                rse.printStackTrace();
            }

            return userList;
        }


        protected void onPostExecute(List<User> userList){
            for(User u: userList){
               contactListContent.addItem(new ContactListContent.ContactListItem(String.valueOf(u.getId()),u.getName(),u.getEmail(),u));
            }

            mAdapter.notifyDataSetChanged();
        }

    }
}
