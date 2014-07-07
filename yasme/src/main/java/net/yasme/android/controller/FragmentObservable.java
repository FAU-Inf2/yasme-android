package net.yasme.android.controller;

import android.app.Fragment;
import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

/**
 * Created by martin on 26.06.2014.
 */
public class FragmentObservable<T extends NotifiableFragment<P>,  P> {
    private ArrayList<T> fragments;

    public FragmentObservable() {
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
            try {
                Log.d("FragmentObserver", "Notify fragment: " + fragment.getClass().getSimpleName());
                fragment.notifyFragment(parameter);
            } catch (Exception e) {
                Log.d("FragmentObserver", "Notify fragment failed: " + fragment.getClass().getSimpleName());
            }
        }
    }

    //do not give the list to others, be paranoid! They may modify it. :)
    //public ArrayList<T> getFragments() {
    //  return fragments;
    // }
}
