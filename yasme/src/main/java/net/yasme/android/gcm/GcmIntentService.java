package net.yasme.android.gcm;


import android.app.IntentService;
import android.content.Intent;

/**
 * Created by florianwinklmeier on 19.06.14.
 */
public class GcmIntentService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    public GcmIntentService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {

    }
}
