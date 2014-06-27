package net.yasme.android.controller;

/**
 * Created by andreas on 27.06.14.
 */
public interface NotifiableFragment<T extends NotifyFragmentParameter> {

    void notifyFragment(T value);
}
