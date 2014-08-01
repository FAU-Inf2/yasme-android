package net.yasme.android.ui;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

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

    /**
     *  @see <a href="http://developer.android.com/design/style/color.html">Color palette used</a>
     *  from K9 Mail
     */
    private final static int CONTACT_DUMMY_COLORS_ARGB[] = {
	    0xff33B5E5,
	    0xffAA66CC,
	    0xff99CC00,
	    0xffFFBB33,
	    0xffFF4444,
	    0xff0099CC,
	    0xff9933CC,
	    0xff669900,
	    0xffFF8800,
	    0xffCC0000
    };

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
        LinearLayout textViews;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        if(msg == null) {
            Log.e(this.getClass().getSimpleName(), "msg == null");
        }
        if(msg.getSender() == null) {
            Log.e(this.getClass().getSimpleName(), "sender == null");
        }

        self = msg.getSender().getId() == selfId;
        if (self) {
            rowView = inflater.inflate(R.layout.chat_item_own, parent, false);
        } else {
            rowView = inflater.inflate(R.layout.chat_item_other, parent, false);
        }
        textView = (TextView) rowView.findViewById(R.id.chat_item_message);
        dateView = (TextView) rowView.findViewById(R.id.chat_item_date);
        imageView = (ImageView) rowView.findViewById(R.id.chat_item_picture);
        textViews = (LinearLayout) rowView.findViewById(R.id.chat_item_text);

        String time = getDateOfMessage(msg);
        String name;
        try {
            name = DatabaseManager.INSTANCE.getUserDAO().get(msg.getSender().getId()).getName();
        } catch (NullPointerException e) {
            Log.d(this.getClass().getSimpleName(), "User nicht in DB gefunden");
            name = "anonym";
        }

        String text;
        if (msg.getErrorId() != 0) {
            text = context.getResources().getString(msg.getErrorId()) + msg.getMessage();
        } else {
            text = msg.getMessage();
        }

        textView.setText(text);
        if (self) {
            //textView.setText(text);
            dateView.setText(time);
        } else {
            //textView.setText(/*name + ": " + */text);
            dateView.setText(name + ", " + time);

            // This is a test with speech bubbles
            //textViews.setBackgroundResource(R.drawable.bubble);
        }

        //dateView.setText(time);
        Log.d(this.getClass().getSimpleName(), name + ": " + msg.getMessage());
	if (imageView != null && !self) {
		imageView.setImageResource(R.drawable.chat_default_icon); //TODO
		imageView.setBackgroundColor(CONTACT_DUMMY_COLORS_ARGB[(int)msg.getSender().getId() % CONTACT_DUMMY_COLORS_ARGB.length]);
		TextView initial = (TextView) rowView.findViewById(R.id.chat_item_picture_text);
		initial.setText(name.substring(0,1).toUpperCase());
		LayerDrawable d = (LayerDrawable) textViews.getBackground();	
		GradientDrawable bgShape = (GradientDrawable) d.findDrawableByLayerId (R.id.chat_item_line);
		bgShape.setColor(CONTACT_DUMMY_COLORS_ARGB[(int)msg.getSender().getId() % CONTACT_DUMMY_COLORS_ARGB.length]);
	}

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
            int dayNumber = calendar.get(Calendar.DAY_OF_WEEK) % 7;

            String day = context.getResources().getStringArray(R.array.daysofweek)[dayNumber];
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
