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
        Message msg = messages.get(position);
        View rowView;
        TextView textView;
        TextView dateView;
        ImageView imageView;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        rowView = inflater.inflate(R.layout.chat_item, parent, false);

        if(msg == null) {
            Log.e(this.getClass().getSimpleName(), "msg == null");
        }
        if(msg.getSender() == null) {
            Log.e(this.getClass().getSimpleName(), "sender == null");
        }

        if (msg.getSender().getId() == selfId) {
            textView = (TextView) rowView.findViewById(R.id.chat_item_message_own);
            dateView = (TextView) rowView.findViewById(R.id.chat_item_date_own);
            imageView = (ImageView) rowView.findViewById(R.id.chat_item_picture_own);
            textView.setGravity(Gravity.RIGHT);
            dateView.setGravity(Gravity.RIGHT);
            //textView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.chat_text_bg_self));
            //textView.setTextColor(context.getResources().getColor(R.color.chat_text_color_self));
        } else {
            textView = (TextView) rowView.findViewById(R.id.chat_item_message_other);
            dateView = (TextView) rowView.findViewById(R.id.chat_item_date_other);
            imageView = (ImageView) rowView.findViewById(R.id.chat_item_picture_other);

            //textView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.chat_text_bg_other));
            //textView.setTextColor(context.getResources().getColor(R.color.chat_text_color_other));
        }

        String time = getDateOfMessage(msg);
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

    private String getDateOfMessage(Message message) {
        String returnDate = "";

        Date dateSent = message.getDateSent();
        Date currentDate =  new Date(System.currentTimeMillis());

        Calendar sent = new GregorianCalendar();
        sent.setTimeInMillis(dateSent.getTime());

        Calendar current = new GregorianCalendar();
        current.setTimeInMillis(currentDate.getTime());

        boolean today = (sent.get(Calendar.YEAR) == current.get(Calendar.YEAR))
                &&(sent.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR));

        if(today) {
            returnDate = formatDateToday(dateSent);
        } else {
            returnDate = formatDate(dateSent);;
        }
        return returnDate;
    }

    public String formatDateToday(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String formattedDate = formatter.format(date);
        return formattedDate + " Uhr";
    }

    public String formatDate(Date date) {
        Calendar calendarDate = new GregorianCalendar();
        calendarDate.setTimeInMillis(date.getTime());


        int dayNumber = calendarDate.get(Calendar.DAY_OF_WEEK);
        String day;
        switch(dayNumber) {
            case 1:
                day = "Montag";
                break;
            case 2:
                day = "Dienstag";
                break;
            case 3:
                day = "Mittwoch";
                break;
            case 4:
                day = "Donnerstag";
                break;
            case 5:
                day = "Freitag";
                break;
            case 6:
                day = "Samstag";
                break;
            case 7:
                day = "Sonntag";
                break;
            default:
                day = "";
                break;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String formattedDate = formatter.format(date);
        if(day.isEmpty()) {
            return formattedDate + " Uhr";
        }
        day = day + ", ";
        return day + formattedDate + " Uhr";
    }
}
