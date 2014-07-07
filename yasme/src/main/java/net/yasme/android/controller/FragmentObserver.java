package net.yasme.android.controller;

import android.app.Fragment;

import java.util.ArrayList;

/**
 * Created by martin on 26.06.2014.
 */
public class FragmentObserver<T extends NotifiableFragment<P>,  P> {

    private ArrayList<T> fragments;

    public FragmentObserver() {
        fragments = new ArrayList<T>();
    }

    public void register(T fragment) {
        fragments.add(fragment);
    }

    public void remove(T fragment) {
        fragments.remove(fragment);
    }

    //add
    public void notifyFragments(P parameter) {
        for (T fragment : fragments) {
            fragment.notifyFragment(parameter);
        }
    }

    //do not give the list to others, be paranoid! They may modify it. :)
    //public ArrayList<T> getFragments() {
    //  return fragments;
    // }
}
