package de.fau.cs.mad.yasme.android.ui;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.entities.User;

/**
 * Created by robert on 07.09.14.
 */
public class UserAdapter extends ArrayAdapter<User> {
    private List<User> users;
    private final Context context;
    private SparseBooleanArray selectedContacts = new SparseBooleanArray();

    public SparseBooleanArray getSelectedContacts() {
        return selectedContacts;
    }

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

    public UserAdapter(Context context, int resource, List<User> users) {
        super(context, resource, users);
        this.users = users;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        User user = users.get(position);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        View rowView = inflater.inflate(R.layout.user_item, parent, false);

        ImageView profileImage = (ImageView) rowView.findViewById(R.id.user_picture);
        TextView initial = (TextView) rowView.findViewById(R.id.user_picture_text);
        TextView profileName = (TextView) rowView.findViewById(R.id.user_name);
        TextView profileId = (TextView) rowView.findViewById(R.id.user_id);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);

        profileImage.setBackgroundColor(CONTACT_DUMMY_COLORS_ARGB[(int) user.getId() % CONTACT_DUMMY_COLORS_ARGB.length]);
        initial.setText(user.getName().substring(0, 1).toUpperCase());
        profileName.setText(user.getName());
        profileId.setText("YD " + user.getId());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) selectedContacts.append(position, b);
                else selectedContacts.delete(position);
            }
        });

        rowView.requestFocus();
        return rowView;
    }
}
