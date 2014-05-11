package de.fau.cs.mad.simplechatapp;

import java.net.MalformedURLException;
import java.net.URL;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class YasmeChat extends Activity {
		
	private EditText message;
	private TextView status;
	private TextView textView1;
	private TextView textView2;
	private TextView textView3;
	private TextView textView4;
	private TextView textView5;
	private TextView textView6;
	private TextView textView7;
	private String usr_name;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
				.add(R.id.container, new PlaceholderFragment()).commit();
		}
		Intent intent = getIntent();
		usr_name = intent.getStringExtra(YasmeHome.USER_NAME);
		
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        initializeViews();
    }
	
	@Override
    protected void onStop() {
        super.onStop();
    }

	private void initializeViews() {
		message = (EditText) findViewById(R.id.text_message);
		status = (TextView) findViewById(R.id.text_status);
		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
		textView6 = (TextView) findViewById(R.id.textView6);
		textView7 = (TextView) findViewById(R.id.textView7);
		status.setText("Eingeloggt: " + usr_name);
	}
	
	public void send(View view) {
		
		
		String msg = message.getText().toString();
		if(!msg.isEmpty()) {
			
			textView7.setText(textView6.getText().toString());
			textView6.setText(textView5.getText().toString());
			textView5.setText(textView4.getText().toString());
			textView4.setText(textView3.getText().toString());
			textView3.setText(textView2.getText().toString());
			textView2.setText(textView1.getText().toString());
			textView1.setText(usr_name + ": " + msg);
			
			//sending to server
			URL url = null;
			try {
				url = new URL(getResources().getString(R.string.server_url));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
			//new SendMessageTask(url, usr_name).execute(msg);
			
			status.setText("Gesendet: " + msg);
			msg = null;
			message.setText("");
			
		} else {
			status.setText("Nichts eingegeben");
			return;
		}
		
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
