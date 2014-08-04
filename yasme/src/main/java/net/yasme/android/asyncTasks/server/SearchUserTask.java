package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.yasme.android.connection.SearchTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.controller.SpinnerObservable;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.fragments.LoginFragment;
import net.yasme.android.ui.fragments.SearchContactFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by florianwinklmeier on 07.07.14.
 */

public class SearchUserTask extends AsyncTask<String, Void, List<User>> {

    //private Spinner searchSpinner;
    //private TextView searchText;
    //private ContactListContent contactListContent;
    //private SimpleAdapter mAdapter;
    private SearchBy searchBy;
    private String searchText;

    /*
    public SearchUserTask(Spinner searchSpinner, TextView searchText,
                          ContactListContent contactListContent, SimpleAdapter mAdapter) {
        this.searchSpinner = searchSpinner;
        this.searchText = searchText;
        this.contactListContent = contactListContent;
        this.mAdapter = mAdapter;
    }
    */
    public SearchUserTask(SearchBy searchBy, String searchText) {
        this.searchBy = searchBy;
        this.searchText = searchText;
    }

    @Override
    protected List<User> doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        SearchTask searchTask = SearchTask.getInstance();
        List<User> uList = new ArrayList<User>();

        try {
            switch (searchBy) {
                case LIKE:
                    uList = searchTask.userByLike(String.valueOf(searchText));
                    return uList;
                case MAIL:
                    uList.add(searchTask.userByMail(String.valueOf(searchText)));
                    return uList;
                case NUMBER:
                    uList.add(searchTask.userByNumber(String.valueOf(searchText)));
                    return uList;
                default:
                    return uList;
            }
        } catch (RestServiceException rse) {
            rse.getMessage();
        }

        return null;
    }


    protected void onPostExecute(List<User> userList) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        ObservableRegistry.getObservable(SearchContactFragment.class).notifyFragments(userList);
    }

    public enum SearchBy {
        LIKE,
        MAIL,
        NUMBER,
        UNKNOWN;

        public static SearchBy getSearchBy(int searchBy) {
            switch (searchBy) {
                case 0:
                    return LIKE;
                case 1:
                    return MAIL;
                case 2:
                    return NUMBER;
            }
            return UNKNOWN;
        }
    }
}

