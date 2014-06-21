package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.ui.LoginActivity;
import net.yasme.android.ui.AbstractYasmeActivity;

import net.yasme.android.connection.DeviceTask;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;

/**
 * Created by cuong on 21/06/14.
 */
public class DeviceRegistrationTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    LoginActivity activity;
    SharedPreferences prefs;
    long deviceId;

    public DeviceRegistrationTask(Context context,SharedPreferences prefs, LoginActivity activity){
        this.context = context;
        this.activity = activity;
        this.prefs = prefs;
    }

    /*
    * @params params[0] is accessToken
    * @params params[1] is userId
    * */


    @Override
    protected Boolean doInBackground(String... params) {
        String accessToken = params[0];
        long userId = Long.parseLong(params[1]);

        long deviceIdFromServer;

        //register device through REST-Call
        // create a new device to be registered

        // user which want to register the device
        User user = new User("user",userId);

        // indicates if its a smartphone or a tablet
        String type = "smartphone";

        // the phone number
        String number = "00000000";

        Device deviceToBeRegistered = new Device(user,Device.Platform.ANDROID,type,number);

        // make the REST-Call
        try{
            deviceIdFromServer = DeviceTask.getInstance().registerDevice(deviceToBeRegistered,accessToken);
            deviceId = deviceIdFromServer;
        }catch(RestServiceException e){
            // if error occurs return false
            Log.d(this.getClass().getSimpleName(),"[DEBUG] RestServiceException");
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        // after device registration
        if(success){
            Log.d(this.getClass().getSimpleName(),"[DEBUG] Device registered");
            // if device registration was a success
            // store to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(AbstractYasmeActivity.DEVICE_ID, deviceId);
            Log.d(this.getClass().getSimpleName(),"[DEBUG] Device stored to SharedPreferences");
            activity.onPostDeviceRegExecute(success,deviceId);
        }


    }


}
