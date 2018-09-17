package com.genonbeta.CoolSocket.test.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.database.MainDatabase;
import com.genonbeta.CoolSocket.test.database.adapter.AbstractDatabaseAdapter;
import com.genonbeta.CoolSocket.test.helper.PeerListHelper;
import com.genonbeta.CoolSocket.test.helper.PeerListHelper.DeviceInfo;
import com.genonbeta.android.database.CursorItem;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.SQLiteDatabase;

public class PeerListAdapter extends AbstractDatabaseAdapter
{
	public static final String COLUMN_EXTRA_AVAILABLESERVICES = "availableServices";
	public static final String COLUMN_EXTRA_FAKE_ITEM = "fakeItem";

	public PeerListAdapter(Context context, SQLiteDatabase db)
	{
		super(context, db, new SQLQuery.Select(MainDatabase.TABLE_SERVERS));
	}

	public int getItemUniqueId(int position)
	{
		return ((CursorItem) getItem(position)).getInt(MainDatabase.COLUMN_SERVERS_ID);
	}

	public String getItemAddress(int position)
	{
		return ((CursorItem) getItem(position)).getString(MainDatabase.COLUMN_SERVERS_ADDRESS);
	}

	@Override
	public void notifyDataSetChanged()
	{
		ArrayMap<String, DeviceInfo> servers = new ArrayMap<>(PeerListHelper.getList());

		for (String deviceIp : servers.keySet())
		{
			CursorItem item = new CursorItem();
			DeviceInfo info = servers.get(deviceIp);

			if (info.deviceName != null)
				item.put(MainDatabase.COLUMN_SERVERS_TITLE, info.deviceName);

			item.put(MainDatabase.COLUMN_SERVERS_ID, 0);
			item.put(MainDatabase.COLUMN_SERVERS_ADDRESS, deviceIp);

			if (info.trebleShot || info.coolSocket || info.deviceController)
			{
				StringBuilder stringBuilder = new StringBuilder();

				if (info.trebleShot)
					stringBuilder.append("TShot");

				if (info.coolSocket)
					stringBuilder.append(" CSocket");

				if (info.deviceController)
					stringBuilder.append(" DContrllr");

				item.put(COLUMN_EXTRA_AVAILABLESERVICES, stringBuilder.toString());
			}

			item.put(COLUMN_EXTRA_FAKE_ITEM, true);

			getList().add(item);
		}

		// After changes made, notify
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup)
	{
		if (view == null)
			view = getLayoutInflater().inflate(R.layout.list_peer, viewGroup, false);

		CursorItem item = (CursorItem) getItem(position);

		TextView textView1 = (TextView) view.findViewById(R.id.list_text);
		TextView textView2 = (TextView) view.findViewById(R.id.list_text2);
		TextView textView3 = (TextView) view.findViewById(R.id.list_text3);

		textView1.setText(item.getString(MainDatabase.COLUMN_SERVERS_ADDRESS));

		if (item.exists(COLUMN_EXTRA_AVAILABLESERVICES))
			textView2.setText(item.getString(COLUMN_EXTRA_AVAILABLESERVICES));

		if (item.exists(MainDatabase.COLUMN_SERVERS_TITLE))
			textView3.setText(item.getString(MainDatabase.COLUMN_SERVERS_TITLE));

		textView2.setVisibility(item.exists(COLUMN_EXTRA_AVAILABLESERVICES) ? View.VISIBLE : View.GONE);
		textView3.setVisibility(item.exists(MainDatabase.COLUMN_SERVERS_TITLE) ? View.VISIBLE : View.GONE);

		return view;
	}
}