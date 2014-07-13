package net.yasme.android.ui;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.entities.Message;
import net.yasme.android.storage.DatabaseManager;

import java.util.List;

/**
 * Created by robert on 12.07.14.
 */
public class ChatAdapter extends ArrayAdapter<Message> {
    private final Context context;
    private List<Message> messages;
    private long selfId;

    public ChatAdapter(Context context, int resource, long selfId, List<Message> messages) {
        super(context, resource, messages);
        this.selfId = selfId;
        this.context = context;
        this.messages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message msg = messages.get(position);
        View rowView;
        TextView textView;
        TextView dateView;
        ImageView imageView;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        rowView = inflater.inflate(R.layout.chat_item, parent, false);

        if (msg.getSender().getId() == selfId) {
            textView = (TextView) rowView.findViewById(R.id.chat_item_message_own);
            dateView = (TextView) rowView.findViewById(R.id.chat_item_date_own);
            imageView = (ImageView) rowView.findViewById(R.id.chat_item_picture_own);

            textView.setGravity(Gravity.RIGHT);
            dateView.setGravity(Gravity.RIGHT);
            textView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.chat_text_bg_self));
            textView.setTextColor(context.getResources().getColor(R.color.chat_text_color_self));
        } else {
            textView = (TextView) rowView.findViewById(R.id.chat_item_message_other);
            dateView = (TextView) rowView.findViewById(R.id.chat_item_date_other);
            imageView = (ImageView) rowView.findViewById(R.id.chat_item_picture_other);

            textView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.chat_text_bg_other));
            textView.setTextColor(context.getResources().getColor(R.color.chat_text_color_other));
        }

        String time = msg.getDateSent().toString();
        String name;
        try {
            name = DatabaseManager.INSTANCE.getUserDAO().get(msg.getSender().getId()).getName();
        } catch (NullPointerException e) {
            Log.d(this.getClass().getSimpleName(), "User nicht in DB gefunden");
            name = "anonym";
        }
        //Log.d(this.getClass().getSimpleName(), name + time);

        textView.setText(name + ": " + msg.getMessage());
        dateView.setText("Gesendet: " + time);
        imageView.setImageResource(R.drawable.chat_default_icon); //TODO

        rowView.requestFocus();
        return rowView;
    }
}
