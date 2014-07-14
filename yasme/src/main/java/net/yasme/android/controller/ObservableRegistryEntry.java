package net.yasme.android.controller;

/**
 * Created by martin on 27.06.14.
 */
public class ObservableRegistryEntry<P> {

    private FragmentObservable<?, ?> obs;
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