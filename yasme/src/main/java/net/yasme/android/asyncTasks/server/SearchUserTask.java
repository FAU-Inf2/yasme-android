package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.yasme.android.connection.SearchTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by florianwinklmeier on 07.07.14.
 */

public class SearchUserTask extends AsyncTask<String, Void, List<User>> {

    private Spinner searchSpinner;
    private TextView searchText;
    private ContactListContent contactListContent;
    private SimpleAdapter mAdapter;

    public SearchUserTask(Spinner searchSpinner, TextView searchText,
                          ContactListContent contactListContent, SimpleAdapter mAdapter) {
        this.searchSpinner = searchSpinner;
        this.searchText = searchText;
        this.contactListContent = contactListContent;
        this.mAdapter = mAdapter;
    }

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
        } catch (RestServiceException rse) {
            rse.getMessage();
        }

        return null;
    }


    protected void onPostExecute(List<User> userList) {

        if (userList != null && userList.size() != 0) {
            for (User u : userList) {
                contactListContent.addItem(new ContactListContent.ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
            }
            mAdapter.notifyDataSetChanged();
        } else {
            contactListContent.addItem(new ContactListContent.ContactListItem("null", "Sorry, No Contact Found", ""));
        }
    }
}

