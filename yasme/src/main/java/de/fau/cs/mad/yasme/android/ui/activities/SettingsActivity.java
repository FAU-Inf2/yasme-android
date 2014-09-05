package de.fau.cs.mad.yasme.android.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.fragments.SettingsFragment;

/**
 * Created by robert on 01.09.14.
 */
public class SettingsActivity extends AbstractYasmeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_with_single_fragment);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.singleFragmentContainer, new SettingsFragment(),
                            "settingsFragment").commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }
}
