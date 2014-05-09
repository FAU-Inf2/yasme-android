package de.fau.cs.mad.simplechatapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BoundService extends Service{

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

	
	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        BoundService getService() {
            // Return this instance of BoundService so clients can call public methods
            return BoundService.this;
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Allows the system to shut down the service
		return START_NOT_STICKY;
    }

	
	/**
	 * TODO: implement public methods the client can call
	 * - returns the current Service instance, which has public 
	 * 		methods the client can call
	 * - or, returns an instance of another class hosted by the 
	 * 		service with public methods the client can call
	 */

}
