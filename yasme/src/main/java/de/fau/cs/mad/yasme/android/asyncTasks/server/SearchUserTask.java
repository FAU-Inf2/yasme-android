package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.connection.SearchTask;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.ui.fragments.SearchContactFragment;

/**
 * Created by Florian Winklmeier <f.winklmeier@t-online.de> on 07.07.14.
 */

public class SearchUserTask extends AsyncTask<String, Void, List<User>> {


    private SearchBy searchBy;
    private String searchText;
    private Class classToNotify;

    public SearchUserTask(SearchBy searchBy, String searchText, Class classToNotify) {
        this.searchBy = searchBy;
        this.searchText = searchText;
        this.classToNotify = classToNotify;
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
            Log.e(this.getClass().getSimpleName(),rse.getMessage());
        }

        return null;
    }


    protected void onPostExecute(List<User> userList) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if(classToNotify == SearchContactFragment.class) {
            ObservableRegistry.getObservable(SearchContactFragment.class).notifyFragments(userList);
        }
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
            }
            return UNKNOWN;
        }
    }
}

