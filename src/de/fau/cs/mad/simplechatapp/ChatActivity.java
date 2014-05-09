package de.fau.cs.mad.simplechatapp;

import de.fau.cs.mad.simplechatapp.BoundService.LocalBinder;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ChatActivity extends Activity {
	
	BoundService mService;
	boolean mBound = false;
	EditText message;
	//TextView status = (TextView) findViewById(R.id.text_status);
	TextView status;
	//TextView textView1 = (TextView) findViewById(R.id.textView1);
	TextView textView1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
				.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		EditText message = (EditText) findViewById(R.id.text_message);
		
		TextView textView1 = (TextView) findViewById(R.id.textView1);
		TextView status = (TextView) findViewById(R.id.text_status);
		//status.setText("start");
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent service = new Intent(this, BoundService.class);
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }
	
	@Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

		
	
	public void send(View view) {
		

		String msg = message.getText().toString();
		
		textView1.setText(msg);
		status.setText("gesendet" + msg);
		
		if(mBound) {
			textView1.setText(msg);
			status.setText("gesendet" + msg);
		} else {
			
		}
		
		//TODO: Senden zum Server
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			// We've bound to BoundService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
			mBound = false;
		}
	 };

	 
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_chat, container,
					false);
			return rootView;
		}
	}

}
