package de.fau.cs.mad.yasme.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Created by robert on 03.10.14.
 */
public class EditTextWithImage {
    private EditText et;
    private ImageView iv;
    private Drawable button;

    public EditTextWithImage(final Context context) {
        et = new EditText(context);
        iv = new ImageView(context);
        iv.setVisibility(View.GONE);
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

    public ImageView getImageView() {
        return iv;
    }

    public int getIntrinsicWidth() {
        return button.getIntrinsicWidth();
    }

    public EditText getEditText() {
        return et;
    }
}
