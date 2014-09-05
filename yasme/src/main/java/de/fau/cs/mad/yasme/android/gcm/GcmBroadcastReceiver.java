package de.fau.cs.mad.yasme.android.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import de.fau.cs.mad.yasme.android.controller.Log;

/**
 * Created by Florian Winklmeier <f.winklmeier@t-online.de> on 19.06.14.
 * Modified by Tim Nisslbeck on 03.08.14
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        Log.d(this.getClass().getSimpleName(), "Received GCM message!");
        ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());

        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
