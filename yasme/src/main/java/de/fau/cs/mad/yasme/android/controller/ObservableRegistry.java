package de.fau.cs.mad.yasme.android.controller;

import de.fau.cs.mad.yasme.android.controller.Log;

import java.util.ArrayList;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 27.06.14.
 */
public class ObservableRegistry {

    private static ArrayList<ObservableRegistryEntry> entries = new ArrayList<ObservableRegistryEntry>();

    public static <T extends NotifiableFragment<P>, P> FragmentObservable<T, P> getObservable(Class fragmentClass) {
        for (ObservableRegistryEntry entry : entries) {
            if (entry.check(fragmentClass)) {
                Log.d("ObserverRegistry","Returned existing observable");
                return (FragmentObservable<T,P>) entry.getObs(); // no idea how to solve this... 
            }
        }

        FragmentObservable<T, P> res = new FragmentObservable<T, P>();
        Log.d("ObserverRegistry","Created new observable");
        entries.add(new ObservableRegistryEntry<P>(res,fragmentClass));
        return res;
    }
}
