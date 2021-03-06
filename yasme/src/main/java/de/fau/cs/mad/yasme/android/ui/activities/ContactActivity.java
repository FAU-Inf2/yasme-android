package de.fau.cs.mad.yasme.android.ui.activities;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Locale;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetQRDataTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.entities.QRData;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.fragments.ContactListFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.OwnProfileFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.QRCodeFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.SearchContactFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.UserDetailsFragment;

/**
 * Created by Stefan Ettl <stefan.ettl@fau.de>
 */

public class ContactActivity extends AbstractYasmeActivity implements ActionBar.TabListener, ContactListFragment.OnFragmentInteractionListener, UserDetailsFragment.OnDetailsFragmentInteractionListener, SearchContactFragment.OnSearchFragmentInteractionListener, OwnProfileFragment.OnOwnProfileFragmentInteractionListener, QRCodeFragment.OnQRCodeFragmentInteractionListener {

    public static final String SEARCH_FOR_CONTACTS = "search_for_new_contacts";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.contact_title);

        //progress bar in actionbar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_contact);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, addIfNotExists a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        if (getIntent().hasExtra(SEARCH_FOR_CONTACTS)) {
            mViewPager.setCurrentItem(1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onFragmentInteraction(User user, View view) {
        this.displayDetailsFragment(user, false);
    }

    @Override
    public void onSearchFragmentInteraction(User user) {
        this.displayDetailsFragment(user,true);
    }

    @Override
    public void onQRCodeFragmentInteraction(User user) {
        this.displayDetailsFragment(user, true);
    }

    @Override
    public void onDetailsFragmentInteraction(User user, Integer buttonId) {

    }

    @Override
    public void onOwnProfileFragmentInteraction(String s) {
        Log.d(this.getClass().getSimpleName(),"-------------------- Within the Activity ---------------------");
    }

    private void displayDetailsFragment(User user, Boolean showAddContact){

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if(prev != null){
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        DialogFragment userDetailsFragment = UserDetailsFragment.newInstance(user, showAddContact);
        int style = userDetailsFragment.STYLE_NO_TITLE;
        //int theme = android.R.style.Theme_Holo;
        userDetailsFragment.setStyle(style, 0);
        userDetailsFragment.show(ft, "dialog");

    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position){
                case 0:
                    return new ContactListFragment();
                case 1:
                    return new SearchContactFragment();
                case 2:
                    return new OwnProfileFragment();
                case 3:
                    return new QRCodeFragment();
                default:
                    return new ContactListFragment();
            }
        }


        @Override
        public int getCount() {
            // Show 4 total pages.
            //if (BuildConfig.DEBUG) {
            //    return 4;
            //}
            //return 3;
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);

            }
            return null;
        }
    }

    // QR Code
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanningResult != null) {
                String scanContent = scanningResult.getContents();
                QRData qrData = new ObjectMapper().readValue(scanContent, QRData.class);
                Log.d(getClass().getSimpleName(), "DeviceId: " + qrData.getDeviceId());
                //SearchUserTask searchUserTask = new SearchUserTask(SearchUserTask.SearchBy.ID,String.valueOf(qrData.getUserId()), QRCodeFragment.class);
                GetQRDataTask getQRDataTask = new GetQRDataTask(qrData);
                toast(R.string.please_wait,Toast.LENGTH_LONG);
                getQRDataTask.execute();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            //Log.d(getClass().getSimpleName(), "Not valid");
            toast(R.string.qr_not_valid,Toast.LENGTH_LONG);
        }
    }
}
