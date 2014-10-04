package de.fau.cs.mad.yasme.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by robert on 03.10.14.
 */
public class EditTextWithX {
    private EditText et;
    private Drawable x;

    public EditTextWithX(Context context) {
        et = new EditText(context);
        x = context.getResources().getDrawable(android.R.drawable.presence_offline);
        x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
        et.setCompoundDrawables(null, null, et.getText().toString().equals("") ? null : x, null);
        et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (et.getCompoundDrawables()[2] == null) {
                    return false;
                }
                if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                if (motionEvent.getX() > et.getWidth() - et.getPaddingRight() - x.getIntrinsicWidth()) {
                    et.setText("");
                    et.setCompoundDrawables(null, null, null, null);
                }
                return false;
            }
        });
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                et.setCompoundDrawables(null, null, et.getText().toString().equals("") ? null : x, null);
            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
        });
    }

    public EditText getEditText() {
        return et;
    }
}
