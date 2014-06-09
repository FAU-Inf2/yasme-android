package net.yasme.android;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class YasmeHome extends Activity {
	public final static String USER_NAME = "net.yasme.andriod.USER_NAME";
	public final static String USER_ID = "net.yasme.andriod.USER_ID";
	public final static String STORAGE_PREFS = "net.yasme.andriod.STORAGE_PREFS";

	String user_name;
	String user_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		SharedPreferences storage = getSharedPreferences(STORAGE_PREFS, 0);
		user_name = storage.getString(USER_NAME, "anonym");
		user_id = storage.getString(USER_ID, "0");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	public void showChat(View view) {
		// BZZZTT!!1!
		// findViewById(R.id.button1).performHapticFeedback(2);
		Intent intent = new Intent(this, YasmeChat.class);
		intent.putExtra(USER_NAME, user_name);
		intent.putExtra(USER_ID, user_id);
		startActivity(intent);
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
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
