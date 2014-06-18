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

/**
 * Created by martin on 18.06.2014.
 */
public class ChatListAdapter extends ArrayAdapter<Chat> {

    Context context;
    int layoutResourceId;
    Chat[] chats = null;

    public ChatListAdapter(Context context, int layoutResourceId, Chat[] chats) {
        super(context, layoutResourceId, chats);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.chats = chats;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        WeatherHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new WeatherHolder();
            //holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
           // holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);

            row.setTag(holder);
        }
        else
        {
            holder = (WeatherHolder)row.getTag();
        }

        //Weather weather = data[position];
        //holder.txtTitle.setText(weather.title);
        //holder.imgIcon.setImageResource(weather.icon);

        return row;
    }

    static class WeatherHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
    }
}