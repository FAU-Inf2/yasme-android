package net.yasme.android.controller;

import android.app.Fragment;

import net.yasme.android.ui.ChatListFragment;

import java.util.ArrayList;

/**
 * Created by martin on 26.06.2014.
 */
public class FragmentObserver1<T extends Fragment> {

    private ArrayList<T> fragments;

    public void register(T fragment) {
        fragments.add(fragment);
    }

    public void remove(T fragment) {
        fragments.remove(fragment);
    }

    public ArrayList<T> getFragments() {
      return fragments;
    }
}
