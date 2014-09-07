package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import de.fau.cs.mad.yasme.android.controller.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetContactsTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.CreateChatTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ChatActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ContactActivity;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 22.06.14.
 */
public class InviteToChatFragment extends Fragment implements View.OnClickListener, NotifiableFragment<InviteToChatFragment.InviteToChatParam> {

	protected List<User> users;
	protected ListView chatPartners;
	protected Button startChat;
	protected ArrayAdapter<String> adapter;
	protected TextView emptyContactsNotice;

	public InviteToChatFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Register at observer
		Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
		FragmentObservable<InviteToChatFragment, InviteToChatParam> obs =
			ObservableRegistry.getObservable(this.getClass());
		Log.d(this.getClass().getSimpleName(), "... successful");
		obs.register(this);

		//progress bar on
		//getActivity().setProgressBarIndeterminateVisibility(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		// Make sure that this fragment is registered before invoking the GetContactsTask
		FragmentObservable<InviteToChatFragment, InviteToChatParam> obs = 
			ObservableRegistry.getObservable(this.getClass());
		obs.register(this);

		new GetContactsTask(this.getClass()).execute();
		View rootView = inflater.inflate(R.layout.fragment_invite_to_chat, container, false);
		return rootView;
	}

	protected void findViewsById() {
		AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
		if (null == startChat) {
			startChat = (Button) activity.findViewById(R.id.inviteToChat_startChat);
		}

		if (null == emptyContactsNotice) {
			emptyContactsNotice = (TextView) activity.findViewById(R.id.empty_contacts_notice);
			emptyContactsNotice.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(getActivity(), ContactActivity.class);
					// Message is immaterial
					intent.putExtra(ContactActivity.SEARCH_FOR_CONTACTS, "let me search for contacts");
					startActivity(intent);
				}
			});
		}

		if (null == chatPartners) {
			chatPartners = (ListView) activity.findViewById(R.id.inviteToChat_usersList);
			// Only show the notice when the list view is empty
			chatPartners.setEmptyView(emptyContactsNotice);
		}
	}


	/**
	 * Will be called by the GetAllUsersTask after the list of users has been retrieved
	 *
	 * @param allUsers list
	 */
	public void updateChatPartnersList(List<User> allUsers) {
		if (null == chatPartners || null == startChat) {
			findViewsById();
		}

		AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
		User self = activity.getSelfUser();

		// Exclude self
		User myself = null;
		List<String> userNames = new ArrayList<String>();
		for (int cur = 0; cur < allUsers.size(); cur++) {
			User user = allUsers.get(cur);

			// Skip self
			if (user.getId() == self.getId()) {
				myself = user;
				continue;
			}

			userNames.add("[" + user.getId() + "] " + user.getName());
		}

		String[] userNamesArr = userNames.toArray(new String[userNames.size()]);
		if (myself != null) {
			allUsers.remove(myself);
		}
		this.users = allUsers;  // without myself

		adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_multiple_choice, userNamesArr);
		chatPartners.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		chatPartners.setAdapter(adapter);

		startChat.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
		SparseBooleanArray checked = chatPartners.getCheckedItemPositions();
		Set<User> selectedUsers = new HashSet<>();
		ArrayList<String> selectedUserNames = new ArrayList<>();

		if (checked.size() == 0) {
			Toast.makeText(activity, getString(R.string.toast_no_selection), Toast.LENGTH_LONG).show();
			return;
		}

		for (int i = 0; i < checked.size(); i++) {
			// Item position in adapter
			if (checked.valueAt(i)) {
				int position = checked.keyAt(i);
				selectedUsers.add(users.get(position));
				selectedUserNames.add(users.get(position).getName());
			}
		}
		new CreateChatTask(activity.getSelfUser(), selectedUsers, this.getClass()).execute();
	}


	public void startChat(long chatId) {
		AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
		Intent intent = new Intent(activity, ChatActivity.class);
		intent.putExtra(activity.USER_MAIL, activity.getUserMail());
		intent.putExtra(activity.USER_ID, activity.getUserId());
		intent.putExtra(activity.CHAT_ID, chatId);
		intent.putExtra(activity.USER_NAME, activity.getSelfUser().getName());
		startActivity(intent);
		activity.finish();
	}

	@Override
	public void notifyFragment(InviteToChatParam inviteToChatParam) {
		Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
		getActivity().setProgressBarIndeterminateVisibility(false); //progress bar off

		if (inviteToChatParam instanceof ChatRegisteredParam) {
			ChatRegisteredParam param = (ChatRegisteredParam) inviteToChatParam;
			startChat(param.getChatId());
		} else if (inviteToChatParam instanceof AllUsersFetchedParam) {
			AllUsersFetchedParam contactsFetchedParam = (AllUsersFetchedParam) inviteToChatParam;
			updateChatPartnersList(contactsFetchedParam.getAllUsers());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		//Register at observer
		FragmentObservable<InviteToChatFragment, InviteToChatParam> obs =
			ObservableRegistry.getObservable(this.getClass());
		obs.register(this);
		findViewsById();
	}

	@Override
	public void onStop() {
		super.onStop();
		//De-Register at observer
		FragmentObservable<InviteToChatFragment, InviteToChatParam> obs =
			ObservableRegistry.getObservable(this.getClass());
		obs.remove(this);
	}



	public static abstract class InviteToChatParam {
		protected Boolean success;

		public Boolean getSuccess() {
			return success;
		}
	}

	public static class ChatRegisteredParam extends InviteToChatParam {
		private Long chatId;

		public ChatRegisteredParam(Boolean success, Long chatId) {
			this.success = success;
			this.chatId = chatId;
		}

		public Long getChatId() {
			return this.chatId;
		}
	}

	public static class AllUsersFetchedParam extends InviteToChatParam {
		private List<User> allUsers;

		public AllUsersFetchedParam(Boolean success, List<User> allUsers) {
			this.success = success;
			this.allUsers = allUsers;
		}

		public List<User> getAllUsers() {
			return allUsers;
		}
	}
}
