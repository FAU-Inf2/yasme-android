package net.yasme.android.controller;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.storage.DatabaseConstants;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by martin on 18.07.2014.
 */
public class Toaster {
    private static Toaster instance;
    private Context context;
    private Set<Toastable> toastables = new HashSet<>();

    private Toaster() {
        context = DatabaseManager.INSTANCE.getContext();
    }

    public static Toaster getInstance() {
        if (instance == null) {
            instance = new Toaster();
        }
        return instance;
    }

    public void register(Toastable toast) {
        toastables.add(toast);
    }

    public void remove(Toastable toast) {
        toastables.remove(toast);
    }

    public void toast(int id, int duration) {
        for (Toastable toast : toastables) {
            toast.toast(id,duration);
        }
    }
}


