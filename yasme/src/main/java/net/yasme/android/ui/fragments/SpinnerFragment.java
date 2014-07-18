package net.yasme.android.ui.fragments;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import net.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by robert on 18.07.14.
 */
public class SpinnerFragment extends Fragment {
    AbstractYasmeActivity activity;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AbstractYasmeActivity) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        //progressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
        //showProgress(true);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress status.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            progressBar.setVisibility(View.VISIBLE);
            /*progressBar.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            progressBar.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });
            */
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //showProgress(false);
    }
}
