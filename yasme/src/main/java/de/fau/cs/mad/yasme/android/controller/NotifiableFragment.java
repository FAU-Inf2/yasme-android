package de.fau.cs.mad.yasme.android.controller;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 27.06.14.
 */
public interface NotifiableFragment<T> {

    void notifyFragment(T value);
}
