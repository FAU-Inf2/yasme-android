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
import android.widget.LinearLayout;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

import net.yasme.android.R;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by martin on 18.06.2014.
 */
public class ChatListAdapter extends ArrayAdapter<Chat> {

    Context context;
    int layoutResourceId;
    List<Chat> chats = null;

    private final static int CHATPARTNER_VISIBLE_CNT = 10;

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
                //return -1*(c1.getMessages().get(c1.getMessages().size()).getDateSent().compareTo(c2.getMessages().get(c2.getMessages().size()).getDateSent()));
                return -1*(c1.getLastModified().compareTo(c2.getLastModified()));
            }
        });
        super.notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
	ChatListViewHolder holder;
	LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	if (convertView == null) {
		row = inflater.inflate(layoutResourceId, parent, false);
		holder = new ChatListViewHolder();
		holder.titleView = (TextView)row.findViewById(R.id.chatlist_item_title);
		holder.subtitleView = (TextView)row.findViewById(R.id.chatlist_item_subtitle);
		holder.lastMessageView = (TextView)row.findViewById(R.id.chatlist_item_last_message);
		holder.chatpartnerList = (LinearLayout) row.findViewById(R.id.chatpartner);  
		holder.moreUsers = (TextView) row.findViewById(R.id.chatlist_more_users);

		row.setTag(holder);
	} else {
		holder = (ChatListViewHolder) convertView.getTag();
	}
        holder.lastMessageView.setVisibility(View.GONE);

        Chat chat = chats.get(position);

        holder.titleView.setText(chat.getName());
        holder.subtitleView.setText(chat.getStatus());

        Message lastMessage = DatabaseManager.INSTANCE.getMessageDAO().
                getNewestMessageOfChat(chat.getId());
        if(!lastMessage.getMessage().isEmpty()) {
            holder.lastMessageView.setText(lastMessage.getSender().getName()
                    + ": " + lastMessage.getMessage());
            holder.lastMessageView.setVisibility(View.VISIBLE);
        }

	holder.chatpartnerList.removeAllViews(); // TODO: also recycle these
	ArrayList<User> users = chat.getParticipants();
	for (int i = 0; i < users.size() && i < CHATPARTNER_VISIBLE_CNT; i++) {  
		// TODO: skip self
		View chatpartner = inflater.inflate(R.layout.chatpartner_item, null);  
		ImageView img = (ImageView) chatpartner.findViewById(R.id.chatpartner_picture);
		img.setImageResource(R.drawable.chatlist_default_icon);	
		img.setBackgroundColor(ChatAdapter.CONTACT_DUMMY_COLORS_ARGB[(int)users.get(i).getId() % ChatAdapter.CONTACT_DUMMY_COLORS_ARGB.length]);
		TextView text = (TextView) chatpartner.findViewById(R.id.chatpartner_picture_text);  
		text.setText(users.get(i).getName().substring(0,1));
		holder.chatpartnerList.addView(chatpartner);  
	} 
	if (users.size() > CHATPARTNER_VISIBLE_CNT) {
		holder.moreUsers.setText("and " + (users.size() - CHATPARTNER_VISIBLE_CNT) + " more...");
	} else {
		holder.moreUsers.setVisibility(View.GONE);
	}

        return row;
    }

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

    static class ChatListViewHolder {
	    TextView titleView, subtitleView, lastMessageView, moreUsers;
	    LinearLayout chatpartnerList;
    }
}

