package net.yasme.android.controller;

import net.yasme.android.ui.ChatListFragment;

/**
 * Created by martin on 26.06.2014.
 */
public class test {
    protected void test() {
        ChatListFragment fragment1 = FragmentObserverList.getChatListInstance().getFragments().get(1);
        ChatListFragment fragment2 = FragmentObserver2.getInstance().getFragments(new ChatListFragment()).get(1);
        ChatListFragment fragment3 = (ChatListFragment)FragmentObserver3.getInstance().getFragments(ChatListFragment.class).get(1);
    }
}
