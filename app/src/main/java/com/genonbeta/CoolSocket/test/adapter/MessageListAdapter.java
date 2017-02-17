package com.genonbeta.CoolSocket.test.adapter;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.database.MainDatabase;
import com.genonbeta.android.database.CursorItem;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.SQLiteDatabase;
import com.genonbeta.android.database.adapter.AbstractDatabaseAdapter;

public class MessageListAdapter extends AbstractDatabaseAdapter
{
	private int receivedColor = 0;
	private int sentColor = 0;
	private int errorColor = 0;

	public MessageListAdapter(Context context, SQLiteDatabase db)
	{
		super(context, db, new SQLQuery.Select(MainDatabase.TABLE_MESSAGE));

		this.receivedColor = ContextCompat.getColor(context, R.color.receivedMessage);
		this.sentColor = ContextCompat.getColor(context, R.color.sentMessage);
		this.errorColor = ContextCompat.getColor(context, R.color.errorMessage);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		if (view == null)
			view = getLayoutInflater().inflate(R.layout.list_message, viewGroup, false);

		TextView textView1 = (TextView) view.findViewById(R.id.list_text);
		TextView textView2 = (TextView) view.findViewById(R.id.list_text2);
		CursorItem item = (CursorItem) getItem(i);

		String client = item.getString(MainDatabase.COLUMN_MESSAGE_CLIENT);
		String message = item.getString(MainDatabase.COLUMN_MESSAGE_MESSAGE);
		boolean isReceived = item.getInt(MainDatabase.COLUMN_MESSAGE_ISRECEIVED) == 1;
		boolean isError = item.getInt(MainDatabase.COLUMN_MESSAGE_ISERROR) == 1;

		String str = (client == null || client.equals("") || client.equals("::1")) ? "localhost" : client;

		textView1.setTextColor((isError) ? this.errorColor : ((isReceived) ? this.receivedColor : this.sentColor));

		textView1.setText(str);
		textView2.setText(message);

		textView1.setGravity(isReceived ? Gravity.START : Gravity.END);
		textView2.setGravity(isReceived ? Gravity.START : Gravity.END);

		return view;
	}
}
