package net.yasme.android.controller;

import android.content.Context;

import net.yasme.android.storage.DatabaseManager;

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

    public void toast(String text, int duration) {
        for (Toastable toast : toastables) {
            toast.toast(text, duration);
        }
    }

    public void toast(String text, int duration, int gravity) {
        for (Toastable toast : toastables) {
            toast.toast(text, duration, gravity);
        }
    }
}


