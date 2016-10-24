package com.genonbeta.CoolSocket.test.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import android.view.View.OnFocusChangeListener;
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
import com.genonbeta.CoolSocket.test.database.OldBadgeDatabase;
import com.genonbeta.CoolSocket.test.database.TemplateListDatabase;
import com.genonbeta.CoolSocket.test.dialog.JsonEditorDialog;
import com.genonbeta.CoolSocket.test.helper.MessageItem;
import com.genonbeta.CoolSocket.test.helper.RemoteServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class MessengerFragment extends Fragment
{
    public static final String TAG = "MessengerFragment";

    public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String ACTION_UPDATE = "com.genonbeta.CoolSocket.test.ACTION_UPDATE";
    public static final String EXTRA_MESSAGE = "extraMessage";
    public static final String EXTRA_PEER_ADDRESS = "extraPeerAddress";

    public static final int REQUEST_CHOOSE_PEER = 15;
    public static final int REQUEST_USE_TEMPLATE = 30;
    IntentFilter mSMSIntentFilter = new IntentFilter(ACTION_SMS_RECEIVED);
    private Cool mCool = new Cool();
    private MessageListAdapter mAdapter;
    private OldBadgeDatabase mBadgeDatabase;
    private TemplateListDatabase mTemplateDatabase;
    private Button mButton;
    private EditText mEditText;
    private EditText mEditTextPort;
    private EditText mEditTextServer;
    private ListView mListView;
    private MenuItem mJsonMenu;
    private View mSeperator;
    private SharedPreferences mPreferences;
    private JSONObject mPendingJson;
    private boolean mIsMultiscreen = false;
    private boolean mJsonEnabled = false;
    private ArrayList<MessageItem> mList = new ArrayList<MessageItem>();
    private MessageSenderHandler mSenderHandler = new MessageSenderHandler();
    private SMSReceiver mSMSReceiver = new SMSReceiver();

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle bundle)
    {
        super.onActivityCreated(bundle);

        if (getActivity() instanceof HomeActivity)
            mIsMultiscreen = ((HomeActivity) getActivity()).isMultiscreen();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
        View inflate = layoutInflater.inflate(R.layout.fragment_messenger, viewGroup, false);

        this.mCool.start();
        this.mEditText = (EditText) inflate.findViewById(R.id.mainEditText);
        this.mEditTextServer = (EditText) inflate.findViewById(R.id.mainServer);
        this.mEditTextPort = (EditText) inflate.findViewById(R.id.mainPort);
        this.mButton = (Button) inflate.findViewById(R.id.mainButton);
        this.mListView = (ListView) inflate.findViewById(R.id.mainListView);
        this.mSeperator = inflate.findViewById(R.id.seperator);
        this.mAdapter = new MessageListAdapter(getActivity(), this.mList);
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.mBadgeDatabase = new OldBadgeDatabase(getActivity());
        this.mTemplateDatabase = new TemplateListDatabase(getActivity());

        this.mEditText.setOnFocusChangeListener(new OnFocusChangeListener()
                                                {
                                                    @Override
                                                    public void onFocusChange(View view, boolean isFocused)
                                                    {
                                                        changeUtilities(isFocused);
                                                    }
                                                }
        );

        this.mButton.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                sendMessage();
                                            }
                                        }
        );

        this.mButton.setOnLongClickListener(new OnLongClickListener()
                                            {
                                                @Override
                                                public boolean onLongClick(View view)
                                                {
                                                    changeUtilities(false);
                                                    return true;
                                                }
                                            }
        );

        this.mListView.setOnItemClickListener(new OnItemClickListener()
                                              {
                                                  @Override
                                                  public void onItemClick(AdapterView<?> adapterView, View view, int i, long j)
                                                  {
                                                      setMessageBox(mList.get(i).message, true);
                                                  }
                                              }
        );

        this.mBadgeDatabase.getList(this.mList);
        this.mListView.setAdapter(this.mAdapter);
        this.setServerText(this.mPreferences.getString("lastServer", "0.0.0.0"));
        this.setMessageBox(this.mPreferences.getString("lastMessage", ""), false);
        this.setPortText(this.mPreferences.getInt("lastPort", 3000));
        this.setMode(this.mPreferences.getBoolean("lastSelectedMode", false));

        try
        {
            this.mPendingJson = new JSONObject(this.mPreferences.getString("lastJsonIndex", "{}"));
            ;
        } catch (JSONException e)
        {
            this.mPendingJson = new JSONObject();
        }

        Toast.makeText(getActivity(), "Server is ready", Toast.LENGTH_SHORT).show();
        this.changeUtilities(false);

        return inflate;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
    {
        super.onCreateOptionsMenu(menu, menuInflater);

        if (!this.mIsMultiscreen)
        {
            menu.add("Pair finder");
            menu.add("Template list");
        }

        this.mJsonMenu = menu.add(7, 7, 7, "0 JSON");
        menu.add(8, 8, 8, "Clear").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(9, 9, 9, "About");

        this.mJsonMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        updateJsonMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        if ("About".equals(menuItem.getTitle()))
        {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("This application is developed to test Genonbeta CoolSocket API");
            stringBuilder.append("\n\nCoolSocket API provides fast communication mechanism");
            stringBuilder.append("\n\nThis CoolSocket application is developed by love and with a couple of developer");

            Builder builder = new Builder(getActivity());

            builder.setTitle("About CoolSocket");
            builder.setMessage(stringBuilder);
            builder.setNegativeButton("Close", (OnClickListener) null);

            builder.setPositiveButton("Help", new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            StringBuilder stringBuilder = new StringBuilder();

                            stringBuilder = stringBuilder.append(" - Text boxes in CoolSocket - \nServer: CoolSocket server IP(v4)\nPort: CoolSocket server port\nMessage: text you send to server");
                            stringBuilder = stringBuilder.append("\n\n - Functions of the text box -");
                            stringBuilder = stringBuilder.append("\nTo change mode, write 'json' and click 'OK'. This toggles mode between JSON mode and Text mode");
                            stringBuilder = stringBuilder.append("\nTo do supported operations, write 'load' and click 'OK'. This will trigger the operations.");
                            stringBuilder = stringBuilder.append("\nJSON mode helps you to send JSON messages easily. ");
                            stringBuilder = stringBuilder.append("\nWhen you in JSON mode if text box is empty and you passed arguments before (you can see them by clicking JSON counter in action bar) then by pressing 'OK' button you can send JSON data to server. ");
                            stringBuilder = stringBuilder.append("\nTo put arguments enter a key then make a space and enter the value (like this 'key value'). ");
                            stringBuilder = stringBuilder.append("To make changes on arguments. You need to enter functions as key and enter the arguments key as a value. ");
                            stringBuilder = stringBuilder.append("\n':rm key' removes entered key");
                            stringBuilder = stringBuilder.append("\n':null key' passes entered key with empty value");
                            stringBuilder = stringBuilder.append("\n\n - Focusing on message box - \nWhen you focus on message box. Other boxes will disappear to make them appear perform long click on OK button");

                            Builder builder = new Builder(getActivity());

                            builder = builder.setTitle("Help");
                            builder = builder.setMessage(stringBuilder);
                            builder = builder.setNegativeButton("Close", (OnClickListener) null);

                            builder.show();
                        }
                    }
            );

            builder.show();
        }
        else if ("Pair finder".equals(menuItem.getTitle()))
        {
            startActivityForResult(new Intent(getActivity(), PairFinderActivity.class), REQUEST_CHOOSE_PEER);
        }
        else if ("Template list".equals(menuItem.getTitle()))
        {
            startActivityForResult(new Intent(getActivity(), TemplateListActivity.class), REQUEST_USE_TEMPLATE);
        }
        else if ("Clear".equals(menuItem.getTitle()))
        {
            this.mList.clear();
            this.mBadgeDatabase.clearList();
            this.mAdapter.notifyDataSetChanged();
        }
        else if (7 == menuItem.getItemId())
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
                    return (mJsonEnabled) ? "Text Mode" : "JSON Mode";
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

        edit.commit();
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
            if (resultCode == getActivity().RESULT_OK)
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
        this.mCool.stop();
    }

    public void addMessage(String client, String message, boolean isReceived, boolean isError)
    {
        if (message.length() >= 1)
        {
            MessageItem messageItem = new MessageItem();

            messageItem.message = message;
            messageItem.client = client;
            messageItem.isReceived = isReceived;
            messageItem.isError = isError;

            this.mList.add(messageItem);
            this.mBadgeDatabase.add(messageItem);
            this.mAdapter.notifyDataSetChanged();

            this.smoothScrollToEnd();
        }
    }

    public void addMessageUI(final String client, final String message, final boolean isReceived, final boolean isError)
    {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                addMessage(client, message, isReceived, isError);
                                            }
                                        }
            );
    }

    public void changeUtilities(boolean mode)
    {
        Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);

        if (mode)
        {
            this.mEditTextServer.setAnimation(fadeOut);
            this.mEditTextPort.setAnimation(fadeOut);

            this.mEditTextServer.setVisibility(View.GONE);
            this.mEditTextPort.setVisibility(View.GONE);
            this.mButton.setVisibility(View.VISIBLE);

            this.mButton.setAnimation(fadeIn);

            this.mEditText.setSingleLine(false);

            return;
        }

        this.mButton.setAnimation(fadeOut);

        this.mEditTextServer.setVisibility(View.VISIBLE);
        this.mEditTextPort.setVisibility(View.VISIBLE);
        this.mButton.setVisibility(View.GONE);

        this.mEditTextServer.setAnimation(fadeIn);
        this.mEditTextPort.setAnimation(fadeIn);

        this.mEditText.setSingleLine(true);
        this.mEditText.clearFocus();
    }

    public boolean jsonPusher(String text)
    {
        int indexOf = text.indexOf(" ");

        if (indexOf == 0 || indexOf + 1 == text.length() || indexOf == -1)
            return false;

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

            Toast.makeText(this.getActivity(), "holding " + this.mPendingJson.length() + ", key '" + key + "', value '" + value + "'", Toast.LENGTH_SHORT).show();

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
                if (this.mPendingJson.has("template_list"))
                {
                    JSONArray jsonArray = this.mPendingJson.getJSONArray("template_list");
                    TemplateListDatabase templateListDatabase = new TemplateListDatabase(getActivity());

                    int added = 0;

                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        if (templateListDatabase.add(jsonArray.getString(i)))
                            added++;
                    }

                    Toast.makeText(getActivity(), added + " template is added", Toast.LENGTH_SHORT).show();

                    this.mPendingJson = new JSONObject();

                    templateListDatabase.close();
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

            if (mTemplateDatabase.isShortcut(serverAddress))
            {
                serverAddress = mTemplateDatabase.getShortcut(serverAddress);
                Log.d(TAG, "This is a shortcut: " + serverAddress);
            }

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
                            addMessageUI(finalServer, "Message couldn't be delivered", false, true);
                        }
                    }
                }.start();
            }
            else
                Messenger.send(serverAddress, Integer.parseInt(this.mEditTextPort.getText().toString()), message, this.mSenderHandler);

            addMessage(serverAddress, message, false, false);

            return true;
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Send error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

                Toast.makeText(getActivity(), "JSON data is registered. Holding index " + this.mPendingJson.length(), Toast.LENGTH_SHORT).show();
                updateJsonMenu();
                return true;
            } catch (JSONException e)
            {
                Toast.makeText(getActivity(), "JSON cannot be parsed", Toast.LENGTH_SHORT).show();
            }
        }

        this.mEditText.getText().clear();
        this.mEditText.getText().append(str);

        return true;
    }

    public boolean setMode(boolean mode)
    {
        this.mJsonEnabled = mode;
        this.mEditText.setHint(this.mJsonEnabled ? "JSON" : "Text");

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
            this.mJsonMenu.setTitle(this.mPendingJson.length() + " JSON");
    }

    private class Cool extends CoolCommunication
    {
        public Cool()
        {
            super(3000);
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
        public void onResponseAvaiable(String response)
        {
            if (response != null && !response.equals(""))
                addMessageUI(mAddress.getHostName(), response, true, false);
        }

        @Override
        public void onError(Exception exception)
        {
            showToast("Someting went wrong while sending your message: (error) " + exception, Toast.LENGTH_SHORT);
            addMessageUI("Send failed", "@" + exception, false, true);
        }
    }
}
