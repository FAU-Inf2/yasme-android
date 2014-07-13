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

        Log.d(this.getClass().getSimpleName(), "Received GCM message! 1/5");
        Log.d(this.getClass().getSimpleName(), "Received GCM message! 2/5");
        Log.d(this.getClass().getSimpleName(), "Received GCM message! 3/5");
        Log.d(this.getClass().getSimpleName(), "Received GCM message! 4/5");
        Log.d(this.getClass().getSimpleName(), "Received GCM message! 5/5");
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());

        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
