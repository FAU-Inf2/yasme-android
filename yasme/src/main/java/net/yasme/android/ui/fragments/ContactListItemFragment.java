package net.yasme.android.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.database.GetContactsTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.User;
import net.yasme.android.ui.fragments.InviteToChatFragment.AllUsersFetchedParam;

import java.util.concurrent.atomic.AtomicInteger;


public class ContactListItemFragment extends Fragment implements AbsListView.OnItemClickListener, NotifiableFragment<AllUsersFetchedParam> {


    private OnFragmentInteractionListener mListener;
    private ContactListContent contactListContent;

    //The fragment's ListView/GridView.
    private AbsListView mListView;

    //The Adapter which will be used to populate the ListView/GridView with Views.
    private SimpleAdapter mAdapter;

    private AtomicInteger bgTasksRunning = new AtomicInteger(0);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactListItemFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         //mAdapter = new ArrayAdapter<ContactListContent.ContactListItem>(getActivity(),
         //       android.R.layout.simple_list_item_1, android.R.id.text1,ContactListContent.ITEMS);

        contactListContent = new ContactListContent();

        //User temp = new User("Stefan","stefan@yasme.net",4);
        //ContactListContent.ContactListItem item = new ContactListContent.ContactListItem(String.valueOf(temp.getId()),temp.getName(),temp.getEmail(),temp);
        //contactListContent.addItem(item);

        mAdapter = new SimpleAdapter(getActivity(), contactListContent.getMap(), android.R.layout.simple_list_item_2, new String[] {"name","mail"}, new int[]{android.R.id.text1,android.R.id.text2});

        // progress bar on
        getActivity().setProgressBarIndeterminateVisibility(true);
        bgTasksRunning.getAndIncrement();
        new GetContactsTask().execute();
        //new GetAllUsersTask(this.getClass()).execute(); //TODO: delete GetAllUsersTask, if not needed anymore
    }


    @Override
    public void onStart() {
        super.onStart();
        FragmentObservable<ContactListItemFragment, AllUsersFetchedParam> obs = ObservableRegistry.getObservable(ContactListItemFragment.class);
        obs.register(this);
    }


    @Override
    public void onStop() {
        FragmentObservable<ContactListItemFragment, AllUsersFetchedParam> obs = ObservableRegistry.getObservable(ContactListItemFragment.class);
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
//    public void setEmptyText(CharSequence emptyText) {
//        View emptyView = mListView.getEmptyView();
//
//        if (emptyText instanceof TextView) {
//            ((TextView) emptyView).setText(emptyText);
//        }
//    }

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


//    private void getContacts(){
//
//        UserDAO userDAO = DatabaseManager.INSTANCE.getUserDAO();
//
//        List<User> userList = userDAO.getContacts();
//        if (userList != null){
//            for(User u:userList){
//                contactListContent.addItem(new ContactListContent.ContactListItem(String.valueOf(u.getId()),u.getName(),u.getEmail(),u));
//            }
//            mAdapter.notifyDataSetChanged();
//        }
//
//    }

    public void notifyFragment(InviteToChatFragment.AllUsersFetchedParam param) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");


        if (param.getSuccess()) {
            for (User u : param.getAllUsers()) {
                contactListContent.addItem(new ContactListContent.ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
            }
            mAdapter.notifyDataSetChanged();
        }

        // Stop spinner if this was the only background task
        if (0 == bgTasksRunning.decrementAndGet()) {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }

    }


//    public static class ContactListItemParam {
//        private Boolean success;
//
//        public ContactListItemParam(Boolean success) {
//            this.success = success;
//        }
//
//        public Boolean getSuccess() {
//            return success;
//        }
//    }


//    private class DownloadAllUsers extends AsyncTask<String,Void,List<User>>{
//
//        @Override
//        protected List<User> doInBackground(String... params) {
//
//            SearchTask search = SearchTask.getInstance();
//            List<User> userList = null;
//
//            try {
//                userList = search.getAllUsers();
//            }catch (RestServiceException rse){
//                rse.getMessage();
//                rse.printStackTrace();
//            }
//
//            return userList;
//        }
//
//
//        protected void onPostExecute(List<User> userList){
//            for(User u: userList){
//               contactListContent.addItem(new ContactListContent.ContactListItem(String.valueOf(u.getId()),u.getName(),u.getEmail(),u));
//            }
//
//            mAdapter.notifyDataSetChanged();
//        }
//
//    }
}
