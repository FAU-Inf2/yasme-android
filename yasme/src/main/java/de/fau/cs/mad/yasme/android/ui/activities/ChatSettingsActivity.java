package de.fau.cs.mad.yasme.android.ui.activities;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import de.fau.cs.mad.yasme.android.controller.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import java.util.Locale;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatSettingsAdd;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatSettingsInfo;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatSettingsRemove;

/**
 * Created by robert on 28.07.14.
 */
public class ChatSettingsActivity extends AbstractYasmeActivity {
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v13.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    public static final String CHAT_ID = "chatId";

    ChatSettingsActivity.SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    private long chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //progress bar in actionbar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        chatId = getIntent().getLongExtra(CHAT_ID, -1);
        if (chatId <= 0) {
            throw new IllegalArgumentException("chatId <= 0");
        }

        setContentView(R.layout.activity_contact);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        if(mViewPager == null) {
            Log.e(this.getClass().getSimpleName(), "ViewPager ist null");
        }
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

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // When the tab is selected, switch to the
                // corresponding page in the ViewPager.
                mViewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        // Add tabs, specifying the tab's text and TabListener
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(tabListener));
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(AbstractYasmeActivity.CHAT_ID, chatId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * A {@link android.support.v13.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.

            // It may be a better idea to pass the chat id and retrieve
            Bundle args = new Bundle();
            args.putLong(CHAT_ID, chatId);

            switch (position){
                case 0:
                    ChatSettingsInfo csi = new ChatSettingsInfo();
                    csi.setArguments(args);
                    return csi;
                case 1:
                    ChatSettingsAdd csa = new ChatSettingsAdd();
                    csa.setArguments(args);
                    return csa;
                case 2:
                    ChatSettingsRemove csr = new ChatSettingsRemove();
                    csr.setArguments(args);
                    return csr;
                default:
                    ChatSettingsInfo csid = new ChatSettingsInfo();
                    csid.setArguments(args);
                    return csid;
            }
        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            //return 3;
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.settings_title_tab1).toUpperCase(l);
                case 1:
                    return getString(R.string.settings_title_tab2).toUpperCase(l);
                case 2:
                    return getString(R.string.settings_title_tab3).toUpperCase(l);

            }
            return null;
        }
    }
}
