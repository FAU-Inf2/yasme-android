package de.fau.cs.mad.yasme.android.controller;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 27.06.14.
 */
public class ObservableRegistryEntry<P> {

    private FragmentObservable<?, ?> obs; // can be <T,P> ?
    private Class fragment;
    private Class param;

    public ObservableRegistryEntry(FragmentObservable<?, ?> obs, Class fragment) {
        this.obs = obs;
        this.fragment = fragment;
    }

    public FragmentObservable<?, ?> getObs() {
        return obs;
    }

    public Class getFragment() {
        return fragment;
    }

    public Class getParam() {
        return param;
    }

    public Boolean check(Class fragmentRef) {
        return fragment.equals(fragmentRef);
    }
}
