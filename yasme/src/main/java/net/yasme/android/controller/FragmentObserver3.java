package net.yasme.android.controller;

import android.app.Fragment;

import java.util.ArrayList;

/**
 * Created by martin on 26.06.2014.
 */
public class FragmentObserver3 {

    public static FragmentObserver3 instance;

    public static FragmentObserver3 getInstance() {
        if (instance == null) {
            instance = new FragmentObserver3();
        }
        return instance;
    }

    private ArrayList<Fragment> fragments;

    public void register(Fragment fragment) {
        fragments.add(fragment);
    }

    public void remove(Fragment fragment) {
        fragments.remove(fragment);
    }

    public ArrayList<Fragment> getFragments(Class cl) {
        ArrayList<Fragment> list = new ArrayList<Fragment>();
        for( Fragment fragment: fragments )
        {
           if (fragment.getClass().equals(cl)) {
               list.add(fragment);
           }
        }
        return list;
    }
}
