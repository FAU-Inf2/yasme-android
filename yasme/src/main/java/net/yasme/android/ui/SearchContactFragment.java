package net.yasme.android.ui;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.SearchUserTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.entities.User;

import java.util.ArrayList;
import java.util.List;

public class SearchContactFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, NotifiableFragment<SearchContactFragment.SearchContactParam> {

    private Spinner searchSpinner;
    private Button searchButton;
    private ListView searchResultView;
    private TextView searchText;
    private SimpleAdapter mAdapter;

    ContactListContent contactListContent;

    private OnSearchFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            new SearchUserTask(searchSpinner,searchText,contactListContent,mAdapter).execute();
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

    /*
    private class SearchUserTask extends AsyncTask<String,Void,List<User>> {

        @Override
        protected List<User> doInBackground(String... params) {

            SearchTask searchTask = SearchTask.getInstance();
            List<User> uList = new ArrayList<User>();

            try {
                switch (searchSpinner.getSelectedItemPosition()) {
                    case 0:
                        uList = searchTask.userByLike(String.valueOf(searchText.getText()));
                        return uList;
                    case 1:
                        uList.add(searchTask.userByMail(String.valueOf(searchText.getText())));
                        return uList;
                    case 2:
                        uList.add(searchTask.userByNumber(String.valueOf(searchText.getText())));
                        return uList;
                }
            }catch(RestServiceException rse){
                rse.getMessage();
            }

           return null;
        }


        protected void onPostExecute(List<User> userList) {

            if (userList != null && userList.size()!=0) {
                for (User u : userList) {
                    contactListContent.addItem(new ContactListContent.ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
                }
                mAdapter.notifyDataSetChanged();
            }else{
                    contactListContent.addItem(new ContactListContent.ContactListItem("null","Sorry, No Contact Found",""));
            }
        }
    }
    */
}
