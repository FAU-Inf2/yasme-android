package de.fau.cs.mad.yasme.android.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.storage.DebugManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.activities.LoginActivity;


/**
 * Created by Florian Winklmeier <f.winklmeier@t-online.de> on 21.06.14.
 */

public class CloudMessaging {

    private static CloudMessaging instance;

    private Activity activity;
    private GoogleCloudMessaging gcm;
    private Context context;
    private String regid;


    public static CloudMessaging getInstance(Activity activity) {
        if (instance == null) {
            instance = new CloudMessaging(activity);
        }
        return instance;
    }

    private CloudMessaging(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.gcm = GoogleCloudMessaging.getInstance(activity);
    }

    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        AbstractYasmeActivity.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(AbstractYasmeActivity.TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public String getRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(AbstractYasmeActivity.PROPERTY_REG_ID, "");
        Log.i(AbstractYasmeActivity.TAG + "WOOOOOLOOOLOOOO", "this is my id: [" + AbstractYasmeActivity.PROPERTY_REG_ID + "]");
        if (registrationId.isEmpty()) {
            Log.i(AbstractYasmeActivity.TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(AbstractYasmeActivity.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(AbstractYasmeActivity.TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    public SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(LoginActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    public int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public String registerInBackground() {
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            regid = gcm.register(AbstractYasmeActivity.SENDER_ID);

            Log.d(this.getClass().getSimpleName(), "Device registered, registration ID=" + regid);

            // Persist the regID - no need to register again.
            storeRegistrationId(context, regid);
            return regid;
        } catch (IOException e) {
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(AbstractYasmeActivity.TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AbstractYasmeActivity.PROPERTY_REG_ID, regId);
        editor.putInt(AbstractYasmeActivity.PROPERTY_APP_VERSION, appVersion);
        editor.commit();

        // For Developer-Devices only
        if (DebugManager.INSTANCE.isDebugMode()) {
            Log.d(getClass().getSimpleName(), "Store gcmId to external storage");
            DebugManager.INSTANCE.storePushId(regId);
        }
    }
}
