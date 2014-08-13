package de.fau.cs.mad.yasme.android.controller;

import android.os.AsyncTask;
import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by martin on 26.06.2014.
 */
public class SpinnerObservable {
    private Set<AsyncTask> backgroundTasks;
    private Set<AbstractYasmeActivity> activities;
    private boolean spinning = false;

    private static SpinnerObservable instance;

    public static SpinnerObservable getInstance() {
        if (instance == null) {
            instance = new SpinnerObservable();
        }
        return instance;
    }

    private SpinnerObservable() {
        backgroundTasks = new HashSet<>();
        activities = new HashSet<>();
    }

    public void registerActivity(AbstractYasmeActivity activity) {
        Log.d(getClass().getSimpleName(), "Register activity");
        activities.add(activity);
        if (spinning) {
            Log.d(getClass().getSimpleName(), "Start spinning");
            activity.startSpinning();
        } else {
            Log.d(getClass().getSimpleName(), "No spinning");
        }
    }

    public void removeActivity(AbstractYasmeActivity activity) {
        Log.d(getClass().getSimpleName(), "Remove activity");
        activities.remove(activity);
    }

    public void registerBackgroundTask(AsyncTask backgroundTask) {
        Log.d(getClass().getSimpleName(), "Register backgroundTask");
        backgroundTasks.add(backgroundTask);
        spinning = true;
        startSpinning();
    }

    public void removeBackgroundTask(AsyncTask backgroundTask) {
        Log.d(getClass().getSimpleName(), "Remove backgroundTask");
        backgroundTasks.remove(backgroundTask);
        if (backgroundTasks.size() == 0) {
            spinning = false;
            stopSpinning();
        }
    }

    public void startSpinning() {
        Log.d(getClass().getSimpleName(), "Start spinning");
        for (AbstractYasmeActivity activity : activities) {
            activity.startSpinning();
        }
    }

    public void stopSpinning() {
        Log.d(getClass().getSimpleName(), "Stop spinning");
        for (AbstractYasmeActivity activity : activities) {
            activity.stopSpinning();
        }
    }
}
