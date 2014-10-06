package de.fau.cs.mad.yasme.android.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.encryption.MessageEncryption;
import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 12.07.14.
 */
public class ChatAdapter extends ArrayAdapter<Message> {
    private final Context context;
    private List<Message> messages;

    /**
     * @see <a href="http://developer.android.com/design/style/color.html">Color palette used</a>
     * from K9 Mail
     */
    public final static int CONTACT_DUMMY_COLORS_ARGB[] = {
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

    public ChatAdapter(Context context, int resource, List<Message> messages) {
        super(context, resource, messages);
        this.context = context;
        this.messages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message msg = messages.get(position);
        Boolean isSelf;
        View rowView;
        ImageView imageView;
        TextView textView;
        TextView dateView;
        ImageView profileImageView = null;
        LinearLayout textViews;
        TextView initial = null;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        if (msg == null) {
            Log.e(this.getClass().getSimpleName(), "msg == null");
            rowView = inflater.inflate(R.layout.chat_item_other, parent, false);
            textView = (TextView) rowView.findViewById(R.id.chat_item_message);
            textView.setText(context.getResources().getString(R.string.message_not_correctly_delivered));
            return rowView;
        }
        if (msg.getSender() == null) {
            Log.e(this.getClass().getSimpleName(), "sender == null");
            rowView = inflater.inflate(R.layout.chat_item_other, parent, false);
            textView = (TextView) rowView.findViewById(R.id.chat_item_message);
            textView.setText(context.getResources().getString(R.string.message_not_correctly_delivered));
            return rowView;
        }
        User user;
        try {
            user = DatabaseManager.INSTANCE.getUserDAO().get(msg.getSender().getId());
            //NPE occured - maybe DatabaseManager is not initialized yet
        } catch (NullPointerException npe) {
            Log.e(this.getClass().getSimpleName(), "NullPointerException occured");
            user = msg.getSender();
        }
        if (user == null) {
            Log.w(this.getClass().getSimpleName(), "User nicht in DB gefunden");
            user = msg.getSender();
        }
        String name = msg.getSender().getName();

        isSelf = user.getId() == DatabaseManager.INSTANCE.getUserId();
        if (isSelf) {
            rowView = inflater.inflate(R.layout.chat_item_own, parent, false);
        } else {
            rowView = inflater.inflate(R.layout.chat_item_other, parent, false);
            profileImageView = (ImageView) rowView.findViewById(R.id.chat_item_picture);
            initial = (TextView) rowView.findViewById(R.id.chat_item_picture_text);
        }
        textView = (TextView) rowView.findViewById(R.id.chat_item_message);
        dateView = (TextView) rowView.findViewById(R.id.chat_item_date);
        imageView = (ImageView) rowView.findViewById(R.id.image_content);
        textViews = (LinearLayout) rowView.findViewById(R.id.chat_item_text);

        if (msg.getMimeType() == null || msg.getMimeType()
                .compareTo(context.getResources().getString(R.string.mime_text)) == 0) {
            String text;
            if (msg.getErrorId() != MessageEncryption.ErrorType.OK) {
                switch (msg.getErrorId()) {
                    case MessageEncryption.ErrorType.DECRYPTION_FAILED:
                        text = context.getResources().getString(R.string.decryption_failed) + msg.getMessage();
                        break;
                    case MessageEncryption.ErrorType.AUTHENTICATION_FAILED:
                        text = context.getResources().getString(R.string.authentication_failed) + msg.getMessage();
                        break;
                    default:
                        text = context.getResources().getString(R.string.unknown_message_error) + msg.getMessage();
                        break;
                }
            } else {
                text = msg.getMessage();
            }
            textView.setText(text);
        } else if (msg.getMimeType().compareTo(context.getResources().getString(R.string.mime_image)) == 0) {
            byte[] bytes = msg.getMessage().getBytes();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap == null) {
                //error decoding bitmap
                bitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_delete);
                //textView.setVisibility(View.GONE);
                //imageView.setVisibility(View.VISIBLE);
                textView.setBackground(new BitmapDrawable(context.getResources(), bitmap));
                //imageView.setImageBitmap(bitmap);

                //textView.setText("Error");
            } else {
                //textView.setVisibility(View.GONE);
                //imageView.setVisibility(View.VISIBLE);
                //imageView.setImageBitmap(bitmap);
                textView.setBackground(new BitmapDrawable(context.getResources(), bitmap));
            }
        }

        String time = getDateOfMessage(msg);
        if (isSelf) {
            dateView.setText(time);
        } else {
            dateView.setText(name + ", " + time);
        }

        if (profileImageView != null && initial != null && !isSelf) {
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                // load picture from local storage
                initial.setVisibility(View.GONE);
                profileImageView.setBackgroundColor(Color.TRANSPARENT);
                profileImageView.setImageBitmap(PictureManager.INSTANCE.getPicture(user, 40, 40));
            } else {
                // no local picture found. Set default pic
                profileImageView.setBackgroundColor(CONTACT_DUMMY_COLORS_ARGB
                        [(int) user.getId() % CONTACT_DUMMY_COLORS_ARGB.length]);
                initial.setText(user.getName().substring(0, 1).toUpperCase());
            }
            LayerDrawable d = (LayerDrawable) textViews.getBackground();
            GradientDrawable bgShape = (GradientDrawable) d.findDrawableByLayerId(R.id.chat_item_line);
            bgShape.setColor(CONTACT_DUMMY_COLORS_ARGB
                    [(int) user.getId() % CONTACT_DUMMY_COLORS_ARGB.length]);
        }

        rowView.requestFocus();
        return rowView;
    }

    private String getDateOfMessage(Message message) {
        String returnDate;

        Date dateSent = message.getDateSent();
        Date currentDate = new Date(System.currentTimeMillis());

        Calendar sent = new GregorianCalendar();
        sent.setTimeInMillis(dateSent.getTime());

        Calendar current = new GregorianCalendar();
        current.setTimeInMillis(currentDate.getTime());

        boolean today = dateEquals(sent, current);

        if (today) {
            returnDate = formatTime(dateSent) + " Uhr";
        } else {
            returnDate = formatDate(sent, current, dateSent) + " Uhr";
        }
        return returnDate;
    }

    public String formatTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        return formatter.format(date);
    }

    public String formatDate(Calendar calendar, Calendar current, Date date) {
        Calendar tmp = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        tmp.add(Calendar.DATE, 1);
        if (dateEquals(tmp, current)) {
            return context.getResources().getString(R.string.yesterday) + ", " + formatTime(date);
        }
        tmp.add(Calendar.DATE, 6);
        if (tmp.compareTo(current) > 0) {
            int dayNumber = calendar.get(Calendar.DAY_OF_WEEK) % 7;

            String day = context.getResources().getStringArray(R.array.daysofweek)[dayNumber];
            return day + ", " + formatTime(date);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        return formatter.format(date);
    }


    public boolean dateEquals(Calendar first, Calendar second) {
        return (first.get(Calendar.YEAR) == second.get(Calendar.YEAR)) && (first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR));
    }
}
