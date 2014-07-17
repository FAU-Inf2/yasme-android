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
import net.yasme.android.entities.Message;
import net.yasme.android.storage.DatabaseManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

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
        Boolean self;
        Message msg = messages.get(position);
        View rowView;
        TextView textView;
        TextView dateView;
        ImageView imageView;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        //rowView = inflater.inflate(R.layout.chat_item, parent, false);
        //rowView = inflater.inflate(R.layout.chat_item_own, parent, false);

        if(msg == null) {
            Log.e(this.getClass().getSimpleName(), "msg == null");
        }
        if(msg.getSender() == null) {
            Log.e(this.getClass().getSimpleName(), "sender == null");
        }

        self = msg.getSender().getId() == selfId;
        if (self) {
            rowView = inflater.inflate(R.layout.chat_item_own, parent, false);
            //textView = (TextView) rowView.findViewById(R.id.chat_item_message_own);
            //dateView = (TextView) rowView.findViewById(R.id.chat_item_date_own);
            //imageView = (ImageView) rowView.findViewById(R.id.chat_item_picture_own);
            //textView.setGravity(Gravity.RIGHT);
            //dateView.setGravity(Gravity.RIGHT);
            //textView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.chat_text_bg_self));
            //textView.setTextColor(context.getResources().getColor(R.color.chat_text_color_self));
        } else {
            rowView = inflater.inflate(R.layout.chat_item_other, parent, false);
            //textView = (TextView) rowView.findViewById(R.id.chat_item_message_own);
            //dateView = (TextView) rowView.findViewById(R.id.chat_item_date_own);
            //imageView = (ImageView) rowView.findViewById(R.id.chat_item_picture_own);

            //textView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.chat_text_bg_other));
            //textView.setTextColor(context.getResources().getColor(R.color.chat_text_color_other));
        }

        textView = (TextView) rowView.findViewById(R.id.chat_item_message);
        dateView = (TextView) rowView.findViewById(R.id.chat_item_date);
        imageView = (ImageView) rowView.findViewById(R.id.chat_item_picture);

        String time = getDateOfMessage(msg);
        String name;
        try {
            name = DatabaseManager.INSTANCE.getUserDAO().get(msg.getSender().getId()).getName();
        } catch (NullPointerException e) {
            Log.d(this.getClass().getSimpleName(), "User nicht in DB gefunden");
            name = "anonym";
        }
        //Log.d(this.getClass().getSimpleName(), name + time);

        if (self) {
            textView.setText( msg.getMessage());
        } else {
            textView.setText(name + ": " + msg.getMessage());
        }

        dateView.setText("Gesendet: " + time);
        Log.d(this.getClass().getSimpleName(), name + ": " + msg.getMessage());
        imageView.setImageResource(R.drawable.chat_default_icon); //TODO

        //ViewGroup.LayoutParams params = rowView.getLayoutParams();
        //params.height = textView.getLayoutParams().height;
        //rowView.setLayoutParams(params);

        rowView.requestFocus();
        return rowView;
    }

    private String getDateOfMessage(Message message) {
        String returnDate = "";



        Date dateSent = message.getDateSent();
        Date currentDate =  new Date(System.currentTimeMillis());

        Calendar sent = new GregorianCalendar();
        sent.setTimeInMillis(dateSent.getTime());

        Calendar current = new GregorianCalendar();
        current.setTimeInMillis(currentDate.getTime());

        boolean today = dateEquals(sent,current);

        if(today) {
            returnDate = formatTime(dateSent) + " Uhr";
        } else {
            returnDate = formatDate(sent, current, dateSent) + " Uhr";
        }
        return returnDate;
    }

    public String formatTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String formattedDate = formatter.format(date);
        return formattedDate;
    }

    public String formatDate(Calendar calendar, Calendar current, Date date) {
        Calendar tmp = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        tmp.add(Calendar.DATE,1);
        if (dateEquals(tmp,current)) {
            return context.getResources().getString(R.string.yesterday) + ", " + formatTime(date);
        }
        tmp.add(Calendar.DATE,6);
        if (tmp.compareTo(current) > 0) {
            int dayNumber = calendar.get(Calendar.DAY_OF_WEEK);
            String day;
            switch(dayNumber) {
                case 1:
                    day = context.getResources().getString(R.string.sunday);
                    break;
                case 2:
                    day = context.getResources().getString(R.string.monday);
                    break;
                case 3:
                    day = context.getResources().getString(R.string.tuesday);
                    break;
                case 4:
                    day = context.getResources().getString(R.string.wednesday);
                    break;
                case 5:
                    day = context.getResources().getString(R.string.thursday);
                    break;
                case 6:
                    day = context.getResources().getString(R.string.friday);
                    break;
                case 7:
                    day = context.getResources().getString(R.string.saturday);
                    break;
                default:
                    day = "";
                    break;
            }
            return day + ", " + formatTime(date);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String formattedDate = formatter.format(date);
        return formattedDate;
    }


    public boolean dateEquals(Calendar first, Calendar second) {
        return (first.get(Calendar.YEAR) == second.get(Calendar.YEAR)) && (first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR));
    }
}
