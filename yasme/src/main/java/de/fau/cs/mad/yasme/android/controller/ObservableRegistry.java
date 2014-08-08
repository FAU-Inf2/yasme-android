package de.fau.cs.mad.yasme.android.controller;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by martin on 27.06.14.
 */
public class ObservableRegistry {

    private static ArrayList<ObservableRegistryEntry> entries = new ArrayList<>();

    public static <T extends NotifiableFragment<P>, P> FragmentObservable<T, P> getObservable(Class fragmentClass) {
        for (ObservableRegistryEntry entry : entries) {
           if (entry.check(fragmentClass)) {
               Log.d("ObserverRegistry","Returned existing observable");
               return (FragmentObservable<T,P>)entry.getObs();
           }
        }
        FragmentObservable<T, P> res = new FragmentObservable<T, P>();
        Log.d("ObserverRegistry","Created new observable");
        entries.add(new ObservableRegistryEntry(res,fragmentClass));
        return res;
    }
}