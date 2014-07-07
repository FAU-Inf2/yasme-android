package net.yasme.android.asyncTasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObserverRegistry;
import net.yasme.android.ui.LoginActivity;
import net.yasme.android.ui.AbstractYasmeActivity;

import net.yasme.android.connection.DeviceTask;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.LoginFragment;

/**
 * Created by cuong on 21/06/14.
 */
public class DeviceRegistrationTask extends AsyncTask<String, Void, Boolean> {
    SharedPreferences prefs;
    long deviceId;

    public DeviceRegistrationTask(SharedPreferences prefs){
        this.prefs = prefs;
    }

    /*
    * @params params[0] is accessToken
    * @params params[1] is userId
    * @param params[2] is product
    * @param params[3] is regId
    * */


    @Override
    protected Boolean doInBackground(String... params) {
        String accessToken = params[0];
        long userId = Long.parseLong(params[1]);

        // the product : e.g Google Nexus
        String product = params[2];

        // regId from google for push

        String regId = params[3];

        long deviceIdFromServer;

        //register device through REST-Call
        // create a new device to be registered

        // user which want to register the device
        // ignore the name user, the server will set the right values according to the userId
        User user = new User("user",userId);

        // indicates if its a smartphone or a tablet
        String type = "smartphone";

        // TODO the phone number
        String number = "00000000";

        Log.d(this.getClass().getSimpleName(),"[DEBUG] product name: " + product);

        Device deviceToBeRegistered = new Device(user,Device.Platform.ANDROID,type,number,product,regId);

        // make the REST-Call
        try{
            deviceIdFromServer = DeviceTask.getInstance().registerDevice(deviceToBeRegistered);
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
            Log.d(this.getClass().getSimpleName(),"[DEBUG] Device registered at yasme server");
            // if device registration was a success
            // store to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(AbstractYasmeActivity.DEVICE_ID, deviceId);
            editor.commit();
            Log.d(this.getClass().getSimpleName(),"[DEBUG] Device stored to SharedPreferences");
            //TODO: Observer, LoginFragment
            //activity.onPostYasmeDeviceRegExecute(success,deviceId);
            //ObserverRegistry.getRegistry(ObserverRegistry.Observers.LOGINFRAGMENT).notifyFragments(new LoginFragment.DeviceRegistrationParam(success, deviceId));

        }

    }


}
