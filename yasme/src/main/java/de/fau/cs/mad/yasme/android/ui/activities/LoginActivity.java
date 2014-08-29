package de.fau.cs.mad.yasme.android.ui.activities;

import de.fau.cs.mad.yasme.android.controller.Log;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.fragments.LoginFragment;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de>
 */

public class LoginActivity extends AbstractYasmeActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //progress bar in actionbar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_with_single_fragment);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.singleFragmentContainer, new LoginFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }
}
