package de.fau.cs.mad.yasme.android.controller;

import de.fau.cs.mad.yasme.android.controller.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Martin Sturm <martin@sturms.name> on 26.06.2014.
 */
public class FragmentObservable<T extends NotifiableFragment<P>,  P> {
    private Set<T> fragments;
    private P buffer;

    public FragmentObservable() {
        fragments = new HashSet<T>();
    }

    public void register(T fragment) {
        fragments.add(fragment);
        if (buffer != null) {
            notifyFragments(buffer);
        }
    }

    public void remove(T fragment) {
        fragments.remove(fragment);
    }

    //addIfNotExists
    public void notifyFragments(P parameter) {
        buffer = parameter;
        for (T fragment : fragments) {
            try {
                Log.d(this.getClass().getSimpleName(), "Notify fragment: " + fragment.getClass().getSimpleName());
                fragment.notifyFragment(parameter);
                buffer = null;
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Notify fragment failed: " + fragment.getClass().getSimpleName());
            }
        }
    }
}
