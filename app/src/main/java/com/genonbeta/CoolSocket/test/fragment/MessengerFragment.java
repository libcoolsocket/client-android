package com.genonbeta.CoolSocket.test.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog.Builder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.genonbeta.CoolSocket.CoolCommunication;
import com.genonbeta.CoolSocket.CoolCommunication.Messenger;
import com.genonbeta.CoolSocket.CoolCommunication.Messenger.Process;
import com.genonbeta.CoolSocket.CoolCommunication.Messenger.ResponseHandler;
import com.genonbeta.CoolSocket.test.HomeActivity;
import com.genonbeta.CoolSocket.test.PairFinderActivity;
import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.TemplateListActivity;
import com.genonbeta.CoolSocket.test.adapter.MessageListAdapter;
import com.genonbeta.CoolSocket.test.database.MainDatabase;
import com.genonbeta.CoolSocket.test.dialog.JsonEditorDialog;
import com.genonbeta.CoolSocket.test.helper.RemoteServer;
import com.genonbeta.android.database.CursorItem;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.util.QueryLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class MessengerFragment extends Fragment
{
	public static final String TAG = "MessengerFragment";

	public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String EXTRA_MESSAGE = "extraMessage";
	public static final String EXTRA_PEER_ADDRESS = "extraPeerAddress";

	public static final int REQUEST_CHOOSE_PEER = 15;
	public static final int REQUEST_USE_TEMPLATE = 30;

	public static final int TASK_LOAD_LIST = 1;

	private IntentFilter mSMSIntentFilter = new IntentFilter(ACTION_SMS_RECEIVED);
	private Cool mCool = new Cool();
	private MessageListAdapter mAdapter;
	private MainDatabase mDatabase;
	private Button mButton;
	private EditText mEditText;
	private EditText mEditTextPort;
	private EditText mEditTextServer;
	private ListView mListView;
	private MenuItem mJsonMenu;
	private View mConnectionFormLayout;
	private SharedPreferences mPreferences;
	private JSONObject mPendingJson;
	private boolean mIsMultiScreen = false;
	private boolean mJsonEnabled = false;
	private MessageSenderHandler mSenderHandler = new MessageSenderHandler();
	private SMSReceiver mSMSReceiver = new SMSReceiver();
	private RemoteSynchronous mSynchronous = new RemoteSynchronous();

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setHasOptionsMenu(true);

		if (Thread.State.TERMINATED.equals(this.mSynchronous.getState()))
			this.mSynchronous = new RemoteSynchronous();

		if (!this.mSynchronous.isAlive())
			this.mSynchronous.start();
	}

	@Override
	public void onActivityCreated(Bundle bundle)
	{
		super.onActivityCreated(bundle);

		if (getActivity() instanceof HomeActivity)
			mIsMultiScreen = ((HomeActivity) getActivity()).isMultiscreen();
	}

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
	{
		View inflate = layoutInflater.inflate(R.layout.fragment_messenger, viewGroup, false);

		mCool.start();
		mEditText = (EditText) inflate.findViewById(R.id.fragment_messenger_message_text);
		mEditTextServer = (EditText) inflate.findViewById(R.id.fragment_messenger_server_text);
		mEditTextPort = (EditText) inflate.findViewById(R.id.fragment_messenger_port_text);
		mButton = (Button) inflate.findViewById(R.id.fragment_messenger_send_button);
		mListView = (ListView) inflate.findViewById(R.id.fragment_messenger_listview);
		mConnectionFormLayout = inflate.findViewById(R.id.fragment_messenger_form_connection);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mDatabase = new MainDatabase(getActivity());
		mAdapter = new MessageListAdapter(getActivity(), mDatabase);

		getLoaderManager().initLoader(TASK_LOAD_LIST, bundle, new QueryLoader.DefaultLoaderCallback(mAdapter));

		mButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (mEditText.getVisibility() == View.GONE)
					changeUtilities(true);
				else
					sendMessage();
			}
		});

		mButton.setOnLongClickListener(new OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View view)
			{
				changeUtilities(false);
				return true;
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long j)
			{
				setMessageBox(mAdapter.getList().get(i).getString(MainDatabase.COLUMN_MESSAGE_MESSAGE), true);
			}
		});

		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				ClipboardManager cMan = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				cMan.setPrimaryClip(ClipData.newPlainText("copiedText", mAdapter.getList().get(i).getString(MainDatabase.COLUMN_MESSAGE_MESSAGE)));

				Toast.makeText(getActivity(), R.string.msg_success_clipboard_copy, Toast.LENGTH_SHORT).show();

				return true;
			}
		});

		mDatabase.getTable(new SQLQuery.Select(MainDatabase.TABLE_MESSAGE));
		mListView.setAdapter(this.mAdapter);
		setServerText(this.mPreferences.getString("lastServer", "0.0.0.0"));
		setMessageBox(this.mPreferences.getString("lastMessage", ""), false);
		setPortText(this.mPreferences.getInt("lastPort", 3000));
		setMode(this.mPreferences.getBoolean("lastSelectedMode", false));

		try
		{
			mPendingJson = new JSONObject(this.mPreferences.getString("lastJsonIndex", "{}"));
		} catch (JSONException e)
		{
			mPendingJson = new JSONObject();
		}

		changeUtilities(false);

		return inflate;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
	{
		super.onCreateOptionsMenu(menu, menuInflater);

		menuInflater.inflate(R.menu.fragment_messenger, menu);

		if (this.mIsMultiScreen)
		{
			menu.findItem(R.id.menu_pair_finder).setVisible(false);
			menu.findItem(R.id.menu_template_list).setVisible(false);
		}

		mJsonMenu = menu.findItem(R.id.menu_json_editor);

		updateJsonMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		int id = menuItem.getItemId();

		if (id == R.id.menu_about)
		{
			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append(getString(R.string.msg_about_sentence_one));
			stringBuilder.append("\n\n");
			stringBuilder.append(getString(R.string.msg_about_sentence_two));
			stringBuilder.append("\n\n");
			stringBuilder.append(getString(R.string.msg_about_sentence_three));

			Builder builder = new Builder(getActivity());

			builder.setTitle(R.string.title_about);
			builder.setMessage(stringBuilder);
			builder.setNegativeButton(R.string.close, null);
			builder.show();
		}
		else if (id == R.id.menu_pair_finder)
		{
			startActivityForResult(new Intent(getActivity(), PairFinderActivity.class), REQUEST_CHOOSE_PEER);
		}
		else if (id == R.id.menu_template_list)
		{
			startActivityForResult(new Intent(getActivity(), TemplateListActivity.class), REQUEST_USE_TEMPLATE);
		}
		else if (id == R.id.menu_clear_list)
		{
			mDatabase.getWritableDatabase().delete(MainDatabase.TABLE_MESSAGE, null, null);
			getLoaderManager().restartLoader(TASK_LOAD_LIST, null, new QueryLoader.DefaultLoaderCallback(mAdapter));
		}
		else if (id == R.id.menu_json_editor)
		{
			JsonEditorDialog.OnEditorClickListener listItemSelected = new JsonEditorDialog.OnEditorClickListener()
			{
				@Override
				public void onJsonClick(JsonEditorDialog editor, DialogInterface dialog, int position)
				{
					setMessageBox(editor.getJsonAdapter().getKeys().get(position) + " " + editor.getJsonAdapter().getValues().get(position), false);
				}
			};

			JsonEditorDialog.OnEditorClickListener removeListener = new JsonEditorDialog.OnEditorClickListener()
			{
				@Override
				public void onJsonClick(JsonEditorDialog editor, DialogInterface dialog, int position)
				{
					updateJsonMenu();
				}
			};

			JsonEditorDialog.ThirdOption thirdOption = new JsonEditorDialog.ThirdOption()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					toggleMode();
				}

				@Override
				public String getButtonName()
				{
					return (mJsonEnabled) ? getString(R.string.title_mode_text) : getString(R.string.title_mode_json);
				}
			};

			JsonEditorDialog jsonEditorDialog = new JsonEditorDialog(this.getActivity(), this.mPendingJson, removeListener, listItemSelected, thirdOption);

			jsonEditorDialog.show();
		}

		return super.onOptionsItemSelected(menuItem);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		getActivity().unregisterReceiver(mSMSReceiver);

		Editor edit = this.mPreferences.edit();

		try
		{
			edit.putString("lastServer", this.mEditTextServer.getText().toString());
			edit.putString("lastMessage", this.mEditText.getText().toString());
			edit.putString("lastJsonIndex", this.mPendingJson.toString());
			edit.putBoolean("lastSelectedMode", this.mJsonEnabled);
			edit.putInt("lastPort", Integer.parseInt(this.mEditTextPort.getText().toString()));
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		edit.apply();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		getActivity().registerReceiver(mSMSReceiver, mSMSIntentFilter);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);

		if (intent != null)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				switch (requestCode)
				{
					case REQUEST_CHOOSE_PEER /*15*/:
						if (intent.hasExtra(EXTRA_PEER_ADDRESS))
							setServerText(intent.getStringExtra(EXTRA_PEER_ADDRESS));
						break;
					case REQUEST_USE_TEMPLATE /*30*/:
						if (intent.hasExtra(EXTRA_MESSAGE))
							setMessageBox(intent.getStringExtra(EXTRA_MESSAGE), true);
						break;
				}
			}
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		mCool.stop();
		mSynchronous.sendKillSignal();
	}

	public void addMessage(String client, String message, boolean isReceived, boolean isError)
	{
		if (message.length() >= 1)
		{
			ContentValues values = new ContentValues();

			values.put(MainDatabase.COLUMN_MESSAGE_CLIENT, client);
			values.put(MainDatabase.COLUMN_MESSAGE_MESSAGE, message);
			values.put(MainDatabase.COLUMN_MESSAGE_ISERROR, isError ? 1 : 0);
			values.put(MainDatabase.COLUMN_MESSAGE_ISRECEIVED, isReceived ? 1 : 0);

			mDatabase.getWritableDatabase().insert(MainDatabase.TABLE_MESSAGE, null, values);
		}
	}

	public void addMessageUI(final String client, final String message, final boolean isReceived, final boolean isError)
	{
		addMessage(client, message, isReceived, isError);

		if (isResumed())
			getActivity().runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					mAdapter.getList().add(new CursorItem()
							.put(MainDatabase.COLUMN_MESSAGE_CLIENT, client)
							.put(MainDatabase.COLUMN_MESSAGE_MESSAGE, message)
							.put(MainDatabase.COLUMN_MESSAGE_ISERROR, isError ? 1 : 0)
							.put(MainDatabase.COLUMN_MESSAGE_ISRECEIVED, isReceived ? 1 : 0));

					mAdapter.notifyDataSetChanged();
					smoothScrollToEnd();
				}
			});
	}

	public void changeUtilities(boolean mode)
	{
		Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
		Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);

		if (mode)
		{
			this.mConnectionFormLayout.setAnimation(fadeOut);
			this.mConnectionFormLayout.setVisibility(View.GONE);

			this.mEditText.setVisibility(View.VISIBLE);
			this.mEditText.setAnimation(fadeIn);

			this.mEditText.requestFocus();

			return;
		}

		this.mConnectionFormLayout.setAnimation(fadeIn);
		this.mConnectionFormLayout.setVisibility(View.VISIBLE);

		this.mEditText.setAnimation(fadeOut);
		this.mEditText.setVisibility(View.GONE);

		this.mEditTextServer.requestFocus();
	}

	public boolean jsonPusher(String text)
	{
		int indexOf = text.indexOf(" ");

		if (indexOf == 0 || indexOf + 1 == text.length() || indexOf == -1)
		{
			Toast.makeText(getActivity(), R.string.msg_json_apply_format, Toast.LENGTH_SHORT).show();
			return false;
		}

		String key = text.substring(0, indexOf);
		String value = text.substring(indexOf + 1, text.length());

		try
		{
			if (key.equals(":null"))
			{
				this.mPendingJson.put(value, "");
			}
			else if (key.equals(":rm") && this.mPendingJson.has(value))
			{
				this.mPendingJson.remove(value);
			}
			else
			{
				this.mPendingJson.put(key, value);
			}

			Toast.makeText(this.getActivity(), getString(R.string.msg_success_json_register_new, mPendingJson.length(), key, value), Toast.LENGTH_SHORT).show();

			return true;
		} catch (JSONException e)
		{
		}

		return false;
	}

	public boolean modChecker(Editable editable)
	{
		if (editable.toString().equals("json"))
		{
			toggleMode();
			editable.clear();

			return true;
		}
		else if (editable.toString().equals("load"))
		{
			try
			{
				if (mPendingJson.has("template_list"))
				{
					JSONArray jsonArray = mPendingJson.getJSONArray("template_list");
					SQLiteDatabase db = mDatabase.getWritableDatabase();

					int addCounter = 0;

					for (int i = 0; i < jsonArray.length(); i++)
					{
						String template = jsonArray.getString(i);

						if (mDatabase.getFirstFromTable(new SQLQuery.Select(MainDatabase.TABLE_TEMPLATE)
								.setWhere(MainDatabase.COLUMN_TEMPLATE_MESSAGE + "=?", template)) == null)
						{
							ContentValues values = new ContentValues();

							values.put(MainDatabase.COLUMN_TEMPLATE_MESSAGE, template);

							db.insert(MainDatabase.TABLE_TEMPLATE, null, values);
							addCounter++;
						}
					}

					Toast.makeText(getActivity(), getString(R.string.msg_success_template_load, addCounter), Toast.LENGTH_SHORT).show();

					if (mIsMultiScreen && getFragmentManager().findFragmentById(R.id.main_template_list_fragment) != null)
					{
						TemplateListFragment templateListFragment = (TemplateListFragment) getFragmentManager().findFragmentById(R.id.main_template_list_fragment);
						templateListFragment.refreshList();
					}

					mPendingJson = new JSONObject();

					updateJsonMenu();
					editable.clear();

					return true;
				}
			} catch (JSONException e)
			{
			}
		}

		return false;
	}

	public boolean toggleMode()
	{
		return setMode(!this.mJsonEnabled);
	}

	public boolean sendMessage()
	{
		Editable text = this.mEditText.getText();

		if (modChecker(text))
			return true;

		if (this.mJsonEnabled)
		{
			if (text.toString().length() > 0)
			{
				if (!jsonPusher(text.toString()))
				{
					return false;
				}
			}
			else if (this.mPendingJson.length() > 0)
			{
				if (!sendMessage(this.mPendingJson.toString()))
					return false;

				this.mPendingJson = new JSONObject();
			}

			updateJsonMenu();
			text.clear();
		}
		else if (text.toString().length() > 0)
		{
			if (!sendMessage(text.toString()))
				return false;

			text.clear();

			return true;
		}

		return false;
	}

	protected boolean sendMessage(final String message)
	{
		try
		{
			String serverAddress = this.mEditTextServer.getText().toString();

			String smsModePrefix = "sms:";
			String httpPrefix = "http://";
			String httpsPrefix = "https://";

			if (serverAddress.startsWith(smsModePrefix))
			{
				serverAddress = serverAddress.substring(smsModePrefix.length());
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(serverAddress, null, message, null, null);
			}
			else if (serverAddress.startsWith(httpPrefix) || serverAddress.startsWith(httpsPrefix))
			{
				final String finalServer = serverAddress;

				new Thread()
				{
					@Override
					public void run()
					{
						super.run();
						try
						{
							RemoteServer server = new RemoteServer(finalServer);
							addMessageUI(finalServer, server.connect("command", message), true, false);
						} catch (Exception e)
						{
							addMessageUI(finalServer, getString(R.string.error_send_message), false, true);
						}
					}
				}.start();
			}
			else
				Messenger.send(serverAddress, Integer.parseInt(this.mEditTextPort.getText().toString()), message, this.mSenderHandler);

			addMessageUI(serverAddress, message, false, false);

			return true;
		} catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getActivity(), getString(R.string.error_send_message_internal, e.getMessage()), Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public boolean setMessageBox(String str, boolean checkJson)
	{
		if (this.mJsonEnabled && checkJson)
		{
			try
			{
				this.mPendingJson = new JSONObject(str);

				Toast.makeText(getActivity(), getString(R.string.msg_success_json_register, mPendingJson.length()), Toast.LENGTH_SHORT).show();
				updateJsonMenu();
				return true;
			} catch (JSONException e)
			{
				Toast.makeText(getActivity(), R.string.error_json_parse, Toast.LENGTH_SHORT).show();
			}
		}

		this.mEditText.getText().clear();
		this.mEditText.getText().append(str);

		return true;
	}

	public boolean setMode(boolean mode)
	{
		this.mJsonEnabled = mode;
		this.mEditText.setHint(this.mJsonEnabled ? getString(R.string.info_mode_json) : getString(R.string.info_mode_text));

		return this.mJsonEnabled;
	}

	public void setServerText(String str)
	{
		this.mEditTextServer.getText().clear();
		this.mEditTextServer.getText().append(str);
	}

	public void setPortText(int i)
	{
		this.mEditTextPort.getText().clear();
		this.mEditTextPort.getText().append(String.valueOf(i));
	}

	public void showToast(final CharSequence charSequence, final int i)
	{
		if (getActivity() != null && !this.isDetached())
			getActivity().runOnUiThread(new Runnable()
										{
											@Override
											public void run()
											{
												Toast.makeText(getActivity(), charSequence, i).show();
											}
										}
			);
	}

	public void smoothScrollToEnd()
	{
		this.mListView.smoothScrollToPosition(this.mAdapter.getCount());
	}

	public void updateJsonMenu()
	{
		if (this.mJsonMenu != null)
			this.mJsonMenu.setTitle(getString(R.string.menu_title_json_editor, mPendingJson.length()));
	}

	private class Cool extends CoolCommunication
	{
		public Cool()
		{
			super(3000);
			setSocketTimeout(2000);
		}

		@Override
		protected void onMessage(Socket socket, String message, PrintWriter printWriter, String clientIp)
		{
			if (message.length() > 0)
				addMessageUI(clientIp, message, true, false);
		}

		@Override
		protected void onError(Exception exception)
		{
		}
	}

	private class SMSReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (ACTION_SMS_RECEIVED.equals(intent.getAction()))
			{
				Bundle bundle = intent.getExtras();

				if (bundle != null)
				{
					// get sms objects
					Object[] pdus = (Object[]) bundle.get("pdus");

					if (pdus.length == 0)
						return;

					// large message might be broken into many
					SmsMessage[] messages = new SmsMessage[pdus.length];

					StringBuilder sb = new StringBuilder();

					for (int i = 0; i < pdus.length; i++)
					{
						messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
						sb.append(messages[i].getMessageBody());
					}

					String sender = messages[0].getOriginatingAddress();
					String message = sb.toString();

					addMessageUI(sender, message, true, false);
				}
			}
		}
	}

	private class MessageSenderHandler extends ResponseHandler
	{
		private InetSocketAddress mAddress;

		@Override
		public void onConfigure(Process process)
		{
			super.onConfigure(process);
			this.mAddress = (InetSocketAddress) process.getSocketAddress();
		}

		@Override
		public void onResponseAvailable(String response)
		{
			if (response != null && !response.equals(""))
				addMessageUI(mAddress.getHostName(), response, true, false);
		}

		@Override
		public void onError(Exception exception)
		{
			showToast(getString(R.string.error_send_msg_connection, exception), Toast.LENGTH_SHORT);
			addMessageUI(getString(R.string.error_title_send_msg), "@" + exception, false, true);
		}
	}

	protected class RemoteSynchronous extends Thread
	{
		private boolean mKillSignal = false;

		public void sendKillSignal()
		{
			this.mKillSignal = true;
		}

		@Override
		public void run()
		{
			super.run();

			Log.d(this.getClass().getName(), "Started/" + this.getId());

			while (!this.mKillSignal)
			{
				try
				{
					sleep(2000);

					if (MessengerFragment.this.mEditText == null)
						continue;

					String serverAddress = MessengerFragment.this.mEditTextServer.getText().toString();

					if (serverAddress.startsWith("http://") || serverAddress.startsWith("https://"))
					{
						RemoteServer server = new RemoteServer(serverAddress);

						JSONObject resultIndex = new JSONObject(server.connect(null, null));

						if (resultIndex.length() > 0)
						{
							Iterator<String> keys = resultIndex.keys();

							while (keys.hasNext())
							{
								String key = keys.next();
								int sepPos = key.indexOf(":");

								try
								{
									addMessageUI((sepPos != -1) ? key.substring(++sepPos) : key, new JSONObject(resultIndex.getString(key)).toString(2), true, false);
								} catch (JSONException e)
								{
									addMessageUI(key, resultIndex.getString(key), true, false);
								}
							}
						}
					}
				} catch (Exception e)
				{
				}
			}

			Log.d(this.getClass().getName(), "Exiting/" + this.getId());
		}
	}
}
