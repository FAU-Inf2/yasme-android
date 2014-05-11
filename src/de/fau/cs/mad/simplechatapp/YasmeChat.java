package de.fau.cs.mad.simplechatapp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

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
	private TextView chatView[];
	private String usr_name;
	private URL url;


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
		url = null;
		try {
			url = new URL(getResources().getString(R.string.server_url));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
    }
	
	@Override
    protected void onStop() {
        super.onStop();
    }

	private void initializeViews() {
		chatView = new TextView[7];
		message = (EditText) findViewById(R.id.text_message);
		status = (TextView) findViewById(R.id.text_status);
		chatView[0] = (TextView) findViewById(R.id.textView1);
		chatView[1] = (TextView) findViewById(R.id.textView2);
		chatView[2] = (TextView) findViewById(R.id.textView3);
		chatView[3] = (TextView) findViewById(R.id.textView4);
		chatView[4] = (TextView) findViewById(R.id.textView5);
		chatView[5] = (TextView) findViewById(R.id.textView6);
		chatView[6] = (TextView) findViewById(R.id.textView7);
		status.setText("Eingeloggt: " + usr_name);
	}
	
	
	public void send(View view) {
		
		String msg = message.getText().toString();
		if(!msg.isEmpty()) {
			for(int i = chatView.length-1; i > 0; i--) {
				chatView[i].setText(chatView[i-1].getText().toString());
			}
			chatView[0].setText(usr_name + ": " + msg);
						
			//sending to server
			//new SendMessageTask(url, usr_name).execute(msg);
			
			status.setText("Gesendet: " + msg);
			msg = null;
			message.setText("");
			
		} else {
			status.setText("Nichts eingegeben");
			return;
		}
		
	}
	
	
	public void update(View view) {
		ArrayList<String> messages= new ArrayList<String>();
		new GetMessageTask(url, messages).execute();
		
		if(messages.isEmpty()) {
			status.setText("Keine neuen Nachrichten");
			return;
		}
		
		Iterator<String> iterator = messages.iterator();
		int size = messages.size();
		if(size >= chatView.length) {
			for(int i = chatView.length-1; i >= 0; i--) {
				chatView[i].setText(iterator.next());
			}
		} else {
			for(int i = chatView.length-1; i >= size; i--) {
				chatView[i].setText(chatView[i-size].getText().toString());
			}
			for(int i = size-1; i >= 0; i--) {
				chatView[i].setText(iterator.next());
			}
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
