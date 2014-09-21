package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.connection.DeviceTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.Device;
import de.fau.cs.mad.yasme.android.entities.QRData;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatListFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.QRCodeFragment;

//import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by Tim on 03.09.14.
 */

public class GetQRDataTask extends AsyncTask<Object, Void, Device> {

    private QRData qrData = null;

    public GetQRDataTask(QRData qrData) {
        this.qrData = qrData;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        SpinnerObservable.getInstance().registerBackgroundTask(this);
    }

    @Override
    protected Device doInBackground(Object... params) {

        try {
            DeviceTask deviceTask = DeviceTask.getInstance();
            Device device = deviceTask.getDevice(qrData.getDeviceId());
            return device;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(final Device device) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (device == null) {
            Log.e(getClass().getSimpleName(),"Device not found");

        } else {
            Log.d(getClass().getSimpleName(),"Device found for User:" + device.getUser().getName());
        }
        qrData.setServerDevice(device);
        ObservableRegistry.getObservable(QRCodeFragment.class).notifyFragments(qrData);
    }
}
