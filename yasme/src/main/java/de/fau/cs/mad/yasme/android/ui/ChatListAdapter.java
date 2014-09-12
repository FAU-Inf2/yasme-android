package de.fau.cs.mad.yasme.android.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetProfilePictureTask;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 18.06.2014.
 */
public class ChatListAdapter extends ArrayAdapter<Chat> {

    private final static int CHATPARTNER_VISIBLE_CNT = 10;
    Context context;
    int layoutResourceId;
    List<Chat> chats;

    public ChatListAdapter(Context context, int layoutResourceId, List<Chat> chats) {
        super(context, layoutResourceId, chats);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.chats = chats;
    }

    @Override
    public void notifyDataSetChanged() {
        //Log.d(getClass().getSimpleName(), "Notify");
        Collections.sort(chats, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                //Log.d(getClass().getSimpleName(), "Compare");
                Chat c1 = (Chat) o1;
                Chat c2 = (Chat) o2;
                if (c1.getLastMessage() == null && c2.getLastMessage() == null) {
                    return 0;
                }
                if (c1.getLastMessage() == null) {
                    return 1;
                }
                if (c2.getLastMessage() == null) {
                    return -1;
                }
                if (c1.getLastMessage().getId() < c2.getLastMessage().getId()) {
                    return 1;
                }
                return -1;
            }
        });
        super.notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ChatListViewHolder holder;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        if (convertView == null) {
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ChatListViewHolder();
            holder.titleView = (TextView) row.findViewById(R.id.chatlist_item_title);
            holder.subtitleView = (TextView) row.findViewById(R.id.chatlist_item_subtitle);
            holder.lastMessageView = (TextView) row.findViewById(R.id.chatlist_item_last_message);
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
        if (!lastMessage.getMessage().isEmpty()) {
            holder.lastMessageView.setText(lastMessage.getSender().getName()
                    + ": " + lastMessage.getMessage());
            holder.lastMessageView.setVisibility(View.VISIBLE);
        }

        holder.chatpartnerList.removeAllViews(); // TODO: also recycle these
        ArrayList<User> users = chat.getParticipants();
        for (int i = 0; i < users.size() && i < CHATPARTNER_VISIBLE_CNT; i++) {
            if (DatabaseManager.INSTANCE.getUserId() == users.get(i).getId()) {
                continue;
            }
            View chatpartner = inflater.inflate(R.layout.chatpartner_item, null);
            ImageView img = (ImageView) chatpartner.findViewById(R.id.chatpartner_picture);
            TextView text = (TextView) chatpartner.findViewById(R.id.chatpartner_picture_text);

            User user = DatabaseManager.INSTANCE.getUserDAO().get(users.get(i).getId());
            if (user == null) {
                user = users.get(i);
            }
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                // load picture from local storage
                text.setVisibility(View.GONE);
                img.setBackgroundColor(Color.TRANSPARENT);
                img.setImageBitmap(PictureManager.INSTANCE.getPicture(user, 50, 50));
            } else {
                // no local picture found. load picture from server and set default pic
                new GetProfilePictureTask(this.getClass()).execute(user.getId());
                img.setImageResource(R.drawable.chatlist_default_icon);
                img.setBackgroundColor(ChatAdapter.CONTACT_DUMMY_COLORS_ARGB
                        [(int) user.getId() % ChatAdapter.CONTACT_DUMMY_COLORS_ARGB.length]);
                text.setText(user.getName().substring(0, 1).toUpperCase());
            }
            holder.chatpartnerList.addView(chatpartner);
        }
        if (users.size() > CHATPARTNER_VISIBLE_CNT) {
            holder.moreUsers.setText("and " + (users.size() - CHATPARTNER_VISIBLE_CNT) + " more...");
            holder.moreUsers.setVisibility(View.VISIBLE);
        } else {
            holder.moreUsers.setVisibility(View.GONE);
        }

        return row;
    }

    static class ChatListViewHolder {
        TextView titleView, subtitleView, lastMessageView, moreUsers;
        LinearLayout chatpartnerList;
    }
}

