package net.yasme.android.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by florianwinklmeier on 19.06.14.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        Log.d(this.getClass().getSimpleName(),"[DEBUG] OnReceive");
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());

        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);

    }
}
