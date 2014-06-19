package net.yasme.android;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.yasme.android.entities.Chat;

import java.util.ArrayList;

/**
 * Created by martin on 18.06.2014.
 */
public class ChatListAdapter extends ArrayAdapter<Chat> {

    Context context;
    int layoutResourceId;
    ArrayList<Chat> chats = null;

    public ChatListAdapter(Context context, int layoutResourceId, ArrayList<Chat> chats) {
        super(context, layoutResourceId, chats);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.chats = chats;
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

        Chat chat = chats.get(position);
        titleView.setText(chat.getName());
        subtitleView.setText(chat.getNumberOfParticipants() + " Teilnehmer");
        iconView.setImageResource(R.drawable.ic_action_cc_bcc);
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
}