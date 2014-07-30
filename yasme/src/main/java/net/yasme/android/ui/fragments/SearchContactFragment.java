package net.yasme.android.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.SearchUserTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.activities.ContactActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchContactFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, NotifiableFragment<ArrayList<User>> {

    private Spinner searchSpinner;
    private Button searchButton;
    private ListView searchResultView;
    private TextView searchText;
    private SimpleAdapter mAdapter;

    private AtomicInteger bgTasksRunning = new AtomicInteger(0);

    ContactListContent contactListContent;

    private OnSearchFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO register in onStart method
        //FragmentObservable<SearchContactFragment, ArrayList<User>> obs = ObservableRegistry.getObservable(SearchContactFragment.class);
        //obs.register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        FragmentObservable<SearchContactFragment, ArrayList<User>> obs = ObservableRegistry.getObservable(SearchContactFragment.class);
        obs.register(this);
    }

    @Override
    public void onStop() {
        FragmentObservable<SearchContactFragment, ArrayList<User>> obs = ObservableRegistry.getObservable(SearchContactFragment.class);
        obs.remove(this);
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_search_contact, null);

        mListener = (OnSearchFragmentInteractionListener) getActivity();

        searchSpinner = (Spinner) layout.findViewById(R.id.search_spinner);
        searchButton = (Button) layout.findViewById(R.id.search_button);
        searchText = (TextView) layout.findViewById(R.id.search_text);
        searchResultView = (ListView) layout.findViewById(R.id.search_listView);

        this.loadSearchSpinner();

        contactListContent = new ContactListContent();

        mAdapter = new SimpleAdapter((ContactActivity)getActivity() ,
                contactListContent.getMap(), android.R.layout.simple_list_item_2, new String[] {"name","mail"}, new int[]{android.R.id.text1,android.R.id.text2});

        searchResultView.setAdapter(mAdapter);

        searchResultView.setOnItemClickListener(this);

        searchButton.setOnClickListener(this);

        return layout;
    }


    private void loadSearchSpinner(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.search_spinner_content, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        searchSpinner.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

        if(searchText.getText().equals("")){

        }else{
            contactListContent.clearItems();
            //new SearchUserTask(searchSpinner,searchText,contactListContent,mAdapter).execute();
            getActivity().setProgressBarIndeterminateVisibility(true);
            bgTasksRunning.incrementAndGet();
            new SearchUserTask(SearchUserTask.SearchBy.getSearchBy(searchSpinner.getSelectedItemPosition()),searchText.getText().toString()).execute();

            // Hide keyboard
            InputMethodManager inputManager =
                    (InputMethodManager) DatabaseManager.INSTANCE.getContext().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(
                    getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null && !contactListContent.items.get(position).id.equals("null")) {
            mListener.onSearchFragmentInteraction(contactListContent.items.get(position).user);
        }
    }

    public interface OnSearchFragmentInteractionListener {
        public void onSearchFragmentInteraction(User user);
    }

    /*
    public void notifyFragment(SearchContactParam param) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
    }


    public static class SearchContactParam {
        private Boolean success;
        private Long userId;
        private String accessToken;

        public SearchContactParam(Boolean success, Long userId, String accessToken) {
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
    */

    @Override
    public void notifyFragment(ArrayList<User> userList) {
        Log.d(getClass().getSimpleName(),"SearchContactFragment has been notified!");
        if (0 == bgTasksRunning.decrementAndGet()) {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }

        if (userList != null && userList.size() != 0) {
            for (User u : userList) {
                contactListContent.addItem(new ContactListContent.ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
            }
            mAdapter.notifyDataSetChanged();
        } else {
            String noResults = DatabaseManager.INSTANCE.getContext().getResources().getString(R.string.search_no_results);
            contactListContent.addItem(new ContactListContent.ContactListItem("null", noResults, ""));
        }
    }
}
