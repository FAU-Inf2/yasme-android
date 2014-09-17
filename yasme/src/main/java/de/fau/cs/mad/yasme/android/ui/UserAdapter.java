package de.fau.cs.mad.yasme.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;

/**
 * Created by robert on 07.09.14.
 */
public class UserAdapter extends ArrayAdapter<User> {
    private List<User> users;
    private final Context context;
    private SparseBooleanArray selectedContacts = new SparseBooleanArray();
    private int layout = R.layout.user_item;

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
        this.layout = resource;
    }

    @Override
    public void notifyDataSetChanged() {
        //Log.d(getClass().getSimpleName(), "Notify");
        Collections.sort(users, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                //Log.d(getClass().getSimpleName(), "Compare");
                User u1 = (User) o1;
                User u2 = (User) o2;
                if (u1.getName() == null && u2.getName() == null) {
                    return 0;
                }
                if (u1.getName() == null) {
                    return -1;
                }
                if (u2.getName() == null) {
                    return 1;
                }
                return u1.getName().toLowerCase().compareTo(u2.getName().toLowerCase());
            }
        });
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        User user = DatabaseManager.INSTANCE.getUserDAO().get(users.get(position).getId());
        if (user == null) {
            Log.w(this.getClass().getSimpleName(), "User nicht in DB gefunden");
            user = users.get(position);
        }

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        View rowView = inflater.inflate(layout, parent, false);

        ImageView profileImage = (ImageView) rowView.findViewById(R.id.user_picture);
        TextView initial = (TextView) rowView.findViewById(R.id.user_picture_text);
        TextView profileName = (TextView) rowView.findViewById(R.id.user_name);
        TextView profileId = (TextView) rowView.findViewById(R.id.user_id);
        CheckBox checkBox;
        if (layout == R.layout.user_item_checkbox) {
            checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
        } else {
            checkBox = null;
        }

        boolean isSelf = (user.getId() == DatabaseManager.INSTANCE.getUserId());
        if (isSelf) {
            SharedPreferences storage = context
                    .getSharedPreferences(AbstractYasmeActivity.STORAGE_PREFS, Context.MODE_PRIVATE);
            user.setProfilePicture(storage.getString(AbstractYasmeActivity.PROFILE_PICTURE, null));
        }

        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            // load picture from local storage
            initial.setVisibility(View.GONE);
            profileImage.setBackgroundColor(Color.TRANSPARENT);
            profileImage.setImageBitmap(PictureManager.INSTANCE.getPicture(user, 50, 50));
        } else {
            // no local picture found. Set default pic
            profileImage.setBackgroundColor(CONTACT_DUMMY_COLORS_ARGB
                    [(int) user.getId() % CONTACT_DUMMY_COLORS_ARGB.length]);
            initial.setText(user.getName().substring(0, 1).toUpperCase());
        }

        profileName.setText(user.getName());
        profileId.setText("YD " + user.getId());
        if (checkBox != null) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    selectedContacts.append(position, b);
                }
            });
        }

        rowView.requestFocus();
        return rowView;
    }
}
