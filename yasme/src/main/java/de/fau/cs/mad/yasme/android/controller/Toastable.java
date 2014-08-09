package de.fau.cs.mad.yasme.android.controller;

/**
 * Created by martin on 19.07.2014.
 */
public interface Toastable {
    public void toast(int id, int duration);

    public void toast(String text, int duration);

    public void toast(String text, int duration, int gravity);
}