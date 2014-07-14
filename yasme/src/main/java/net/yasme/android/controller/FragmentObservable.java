package net.yasme.android.controller;

import android.app.Fragment;
import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by martin on 26.06.2014.
 */
public class FragmentObservable<T extends NotifiableFragment<P>,  P> {
    private Set<T> fragments;

    public FragmentObservable() {
        fragments = new HashSet<T>();
    }

    public void register(T fragment) {
        fragments.add(fragment);
    }

    //public boolean isRegistered(T fragment) {
    //    return fragments.contains(fragment);
    //}

    public void remove(T fragment) {
        fragments.remove(fragment);
    }

    //addIfNotExists
    public void notifyFragments(P parameter) {
        for (T fragment : fragments) {
            try {
                Log.d(this.getClass().getSimpleName(), "Notify fragment: " + fragment.getClass().getSimpleName());
                fragment.notifyFragment(parameter);
            } catch (Exception e) {
                Log.d(this.getClass().getSimpleName(), "Notify fragment failed: " + fragment.getClass().getSimpleName());
                Log.e(this.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    //do not give the list to others, be paranoid! They may modify it. :)
    //public ArrayList<T> getFragments() {
    //  return fragments;
    // }
}
