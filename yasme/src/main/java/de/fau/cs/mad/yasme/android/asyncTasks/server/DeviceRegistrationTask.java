package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.app.Activity;
import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.connection.DeviceTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.encryption.KeyEncryption;
import de.fau.cs.mad.yasme.android.entities.OwnDevice;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.gcm.CloudMessaging;
import de.fau.cs.mad.yasme.android.storage.DebugManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.fragments.LoginFragment;

/**
 * Created by Cuong Bui <cuong.bui@fau.de> on 21/06/14.
 */
public class DeviceRegistrationTask extends AsyncTask<String, Void, Boolean> {

    private long deviceId;
    private Activity activity;
    private String regId;
    private Class classToNotify;

    public DeviceRegistrationTask(Activity activity, Class classToNotify) {
        this.activity = activity;
        this.classToNotify = classToNotify;
    }

    /**
     * @params params[0] is userId
     * @params params[1] is product
     */
    @Override
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        long userId = Long.parseLong(params[0]);

        // the product : e.g Google Nexus
        String product = params[1];

        // Register for Google Cloud Messaging at Google Server
        if (!registerGCM()) {
            return false;
        }

        // Register at YASME server
        return registerDeviceAtYASME(userId, product, regId);
    }


    private boolean registerGCM() {
        CloudMessaging cloudMessaging = CloudMessaging.getInstance(this.activity);

        if (cloudMessaging.checkPlayServices()) {
            String regid = cloudMessaging.getRegistrationId();
            Log.d(this.getClass().getSimpleName(), "Google reg id is empty? " + regid.isEmpty());
            if (regid.isEmpty()) {
                regId = cloudMessaging.registerInBackground();
                if (null == regId || regId.isEmpty()) {
                    Log.e(this.getClass().getSimpleName(), "reg id for GCM is empty");
                    return false;
                }
            }
        } else {
            Log.i(AbstractYasmeActivity.TAG, "No valid Google Play Services APK found.");
        }

        return true;
    }


    private boolean registerDeviceAtYASME(long userId, String product, String regId) {
        long deviceIdFromServer;

        //register device through REST-Call
        // create a new device to be registered

        // user which want to register the device
        // ignore the name user, the server will set the right values according to the userId
        User user = new User("user", userId);

        // indicates if its a smartphone or a tablet
        // currently unused
        String type = "device";

        // phone number, currently unused
        String number = null;

        KeyEncryption rsa = new KeyEncryption();
        //generate private and public Key
        rsa.generateRSAKeys();
        String pubKeyInBase64 = rsa.getGeneratedPubKeyInBase64();

        OwnDevice deviceToBeRegistered = new OwnDevice(user, OwnDevice.Platform.ANDROID, pubKeyInBase64, type, number, product, regId);

        // make the REST-Call
        try {
            deviceIdFromServer = DeviceTask.getInstance().registerDevice(deviceToBeRegistered);
            deviceId = deviceIdFromServer;
            //save private and public Key to storage
            rsa.saveRSAKeys(deviceId);
        } catch (RestServiceException e) {
            // if error occurs return false
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }

        Log.d(this.getClass().getSimpleName(), "Device registered at yasme server");

        // For Developer-Devices only
        if (DebugManager.INSTANCE.isDebugMode()) {
            Log.d(getClass().getSimpleName(), "Store keys to external storage");
            DebugManager.INSTANCE.storeDeviceId(deviceId);
        }

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        // after device registration
        ObservableRegistry.getObservable(classToNotify)
                .notifyFragments(new LoginFragment.DeviceRegistrationParam(success));
    }
}
