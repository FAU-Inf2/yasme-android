package net.yasme.android.controller;

import android.app.Fragment;

/**
 * Created by andreas on 27.06.14.
 */
public class ExampleFragment extends Fragment implements NotifiableFragment<ExampleFragment.MyParameters> {

    @Override
    public void notifyFragment(MyParameters value) {
        System.out.println("I've been notified. Awesome.");
    }

    public static class MyParameters implements NotifyFragmentParameter {

        private int first;

        private double second;

        public MyParameters(int first, double second) {
            this.first = first;
            this.second = second;
        }
    }
}
