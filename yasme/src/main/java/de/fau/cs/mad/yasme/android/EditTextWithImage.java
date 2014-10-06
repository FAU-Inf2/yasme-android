package de.fau.cs.mad.yasme.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by robert on 03.10.14.
 */
public class EditTextWithImage {
    //private View view;
    private EditText et;
    private ImageView iv;
    private Drawable button;
    private Bitmap bitmap;

    public EditTextWithImage(final Context context) {
        //view = new View(context);
        et = new EditText(context);
        iv = new ImageView(context);
        iv.setVisibility(View.GONE);
        ArrayList<View> list = new ArrayList<>();
        list.add(et);
        list.add(iv);
        //view.addTouchables(list);
        button = context.getResources().getDrawable(android.R.drawable.ic_menu_camera);
        button.setBounds(0, 0, button.getIntrinsicWidth(), button.getIntrinsicHeight());
        et.setCompoundDrawables(null, null, button, null);

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                et.setCompoundDrawables(null, null, button, null);
            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
        });
    }

    //public View getView() {
    //return view;
    //}

    public int getIntrinsicWidth() {
        return button.getIntrinsicWidth();
    }

    public EditText getEditText() {
        return et;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
