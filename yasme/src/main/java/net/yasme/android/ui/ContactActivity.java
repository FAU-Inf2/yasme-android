package net.yasme.android.ui;

import java.util.Locale;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.CreateSingleChatTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;

public class ContactActivity extends AbstractYasmeActivity implements ActionBar.TabListener, ContactListItemFragment.OnFragmentInteractionListener, UserDetailsFragment.OnDetailsFragmentInteractionListener, SearchContactFragment.OnSearchFragmentInteractionListener, OwnProfileFragment.OnOwnProfileFragmentInteractionListener {

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



        // For each of the sections in the app, add a tab to the action bar.
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
        int id = item.getItemId();
        if (id == R.id.action_search_contact) {
            Intent intent = new Intent(this, InviteToChatActivity.class);
            startActivity(intent);
            return true;
        }
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
    public void onDetailsFragmentInteraction(User user, Integer buttonId) {

        DatabaseManager db = DatabaseManager.getInstance();

        switch (buttonId){
            case R.id.contact_detail_newchat:
                System.out.println("------------------- Create New Chat ---------------------------");
                CreateSingleChatTask chatTask = new CreateSingleChatTask(this,selfUser,user);
                chatTask.execute(String.valueOf(this.getUserId()),this.getAccessToken());
                break;
            case R.id.contact_detail_addcontact:
                user.addToContacts();
                db.createUserIfNotExists(user);
                System.out.println("------------------- Contact Added ---------------------------");
                break;
            case R.id.mail_image_button:
                this.sendMail(user.getEmail());
                break;
            case R.id.number_image_button:
                break;
        }

    }

    @Override
    public void onOwnProfileFragmentInteraction(String s) {
        System.out.println("-------------------- In der Activity ---------------------");
    }

    private void callContact(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+number));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
    }

    private void sendMail(String email){

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
        i.putExtra(Intent.EXTRA_TEXT   , "Message powered by YASME");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
           ex.printStackTrace();
        }

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
        int theme = android.R.style.Theme_Holo;
        userDetailsFragment.setStyle(style,0);
        userDetailsFragment.show(ft,"dialog");

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
            Bundle b = new Bundle();
            b.putString("accessToken", accessToken);
            b.putLong("userId", selfUser.getId());

            switch (position){
                case 0:
                    ContactListItemFragment clif = new ContactListItemFragment();
                    clif.setArguments(b);
                    return clif;
                case 1:
                    SearchContactFragment scf = new SearchContactFragment();
                    scf.setArguments(b);
                    return scf;
                case 2:
                    OwnProfileFragment opf = new OwnProfileFragment();
                    opf.setArguments(b);
                    return opf;
                default:
                    ContactListItemFragment cliff = new ContactListItemFragment();
                    cliff.setArguments(b);
                    return cliff;
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
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

            }
            return null;
        }
    }

    public void startChat(long chatId) {
        //Log.d(this.getClass().getSimpleName(), "[DEBUG] Start chat: " + chatId);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(this.USER_MAIL, this.getUserMail());
        intent.putExtra(this.USER_ID, this.getUserId());
        intent.putExtra(this.CHAT_ID, chatId);
        intent.putExtra(this.USER_NAME, this.getSelfUser().getName());
        startActivity(intent);
    }
}
