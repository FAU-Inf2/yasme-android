package net.yasme.android.controller;

import net.yasme.android.ui.ChatListFragment;
import net.yasme.android.ui.ContactListItemFragment;

/**
 * Created by martin on 26.06.2014.
 */

public class FragmentObserverList {
    public static FragmentObserver1<ChatListFragment> chatListInstance;
    public static FragmentObserver1<ContactListItemFragment> contactListInstance;

    public static FragmentObserver1<ChatListFragment> getChatListInstance() {
        if (chatListInstance == null) {
            chatListInstance = new FragmentObserver1<ChatListFragment>();
        }
        return chatListInstance;
    }

    public static FragmentObserver1<ContactListItemFragment> getContactListInstance() {
        if (contactListInstance == null) {
            contactListInstance = new FragmentObserver1<ContactListItemFragment>();
        }
        return contactListInstance;
    }
}
