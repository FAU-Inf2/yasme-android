package net.yasme.android.controller;

import android.app.Fragment;

import net.yasme.android.ui.ChatListFragment;

import java.util.ArrayList;

/**
 * Created by martin on 26.06.2014.
 */
public class FragmentObserver2 {

    public static FragmentObserver2 instance;

    public static FragmentObserver2 getInstance() {
        if (instance == null) {
            instance = new FragmentObserver2();
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

    public <T> ArrayList<T> getFragments(T type) {
        ArrayList<T> list = new ArrayList<T>();
        for( Fragment fragment: fragments )
        {
            try {
                T t = (T) fragment;
                list.add(t);
            } catch (Exception e) {

            }
        }
        return list;
    }
}
