package net.yasme.android.ui;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.storage.DatabaseManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by martin on 18.06.2014.
 */
public class ChatListAdapter extends ArrayAdapter<Chat> {

    Context context;
    int layoutResourceId;
    List<Chat> chats = null;

    public ChatListAdapter(Context context, int layoutResourceId, List<Chat> chats) {
        super(context, layoutResourceId, chats);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.chats = chats;
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(chats, new Comparator<Object>() {
            @Override
            public int compare(Object o, Object o2) {
                Chat c1 = (Chat) o;
                Chat c2 = (Chat) o2;
                return -1*(c1.getLastModified().compareTo(c2.getLastModified()));
            }
        });
        super.notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        //ChatHolder holder = null;
        /*
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ChatHolder();
            holder.iconView = (ImageView)row.findViewById(R.id.chatlist_item_icon);
            holder.titleView = (TextView)row.findViewById(R.id.chatlist_item_title);
            holder.subtitleView = (TextView)row.findViewById(R.id.chatlist_item_subtitle);

            row.setTag(holder);
        }
        else
        {
            holder = (ChatHolder)row.getTag();
        }

        Chat chat = chats.get(position);
        holder.titleView.setText(chat.getName());
        holder.subtitleView.setText(chat.getNumberOfParticipants() + " Teilnehmer");
        holder.iconView.setImageResource(R.drawable.ic_action_cc_bcc);
        */

        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        ImageView iconView = (ImageView)row.findViewById(R.id.chatlist_item_icon);
        TextView titleView = (TextView)row.findViewById(R.id.chatlist_item_title);
        TextView subtitleView = (TextView)row.findViewById(R.id.chatlist_item_subtitle);
        TextView lastMessageView = (TextView)row.findViewById(R.id.chatlist_item_last_message);
        lastMessageView.setVisibility(View.GONE);

        Chat chat = chats.get(position);

        titleView.setText(chat.getName());
        subtitleView.setText(chat.getStatus());
        iconView.setImageResource(R.drawable.chat_default_icon);

        Message lastMessage = DatabaseManager.INSTANCE.getMessageDAO().
                getNewestMessageOfChat(chat.getId());
        if(!lastMessage.getMessage().isEmpty()) {
            lastMessageView.setText(lastMessage.getSender().getName()
                    + ": " + lastMessage.getMessage());
            lastMessageView.setVisibility(View.VISIBLE);
        }

        row.setTag(chat.getId());

        return row;
    }
    /*
    static class ChatHolder
    {
        ImageView iconView;
        TextView titleView;
        TextView subtitleView;
    }
    */

    public void updateChats(List<Chat> updatedChats) {
        // This:
        // chats = updatedChats;
        // does not work. No update at runtime!
        chats.clear();
        for (int i=0; i < updatedChats.size(); i++) {
            chats.add(updatedChats.get(i));
        }
        Log.d(this.getClass().getSimpleName(), "Chats updated: " + this.chats.size());
    }
}