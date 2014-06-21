package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.yasme.android.YasmeLogin;

import net.yasme.android.connection.DeviceTask;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;

/**
 * Created by cuong on 21/06/14.
 */
public class DeviceRegistrationTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    YasmeLogin activity;

    public DeviceRegistrationTask(Context context, YasmeLogin activity){
        this.context = context;
        this.activity = activity;

    }

    /*
    * @params params[0] is accessToken
    * */


    @Override
    protected Boolean doInBackground(String... params) {
        String accessToken = params[0];
        long deviceIdFromServer;

        //register device through REST-Call
        // create a new device to be registered

        // user which want to register the device
        User user = new User();

        // indicates if its a smartphone or a tablet
        String type = "smartphone";

        // the phone number
        String number = "00000000";

        Device deviceToBeRegistered = new Device(user,Device.Platform.ANDROID,type,number);

        // make the REST-Call
        try{
            deviceIdFromServer = DeviceTask.getInstance().registerDevice(deviceToBeRegistered,accessToken);
        }catch(RestServiceException e){
            return false;
        }

        return null;
    }
}
