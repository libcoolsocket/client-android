package com.genonbeta.CoolSocket.test.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.database.MainDatabase;
import com.genonbeta.android.database.CursorItem;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.SQLiteDatabase;
import com.genonbeta.android.database.adapter.AbstractDatabaseAdapter;

public class TemplateListAdapter extends AbstractDatabaseAdapter
{
	public TemplateListAdapter(Context context, SQLiteDatabase db)
	{
		super(context, db, new SQLQuery.Select(MainDatabase.TABLE_TEMPLATE));
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		// Creating new instance otherwise in can't revert changes
		if (view == null)
			view = getLayoutInflater().inflate(R.layout.list, viewGroup, false);

		CursorItem item = (CursorItem) getItem(i);
		TextView text = (TextView) view.findViewById(R.id.text);

		text.setText(item.getString(MainDatabase.COLUMN_TEMPLATE_MESSAGE));

		return view;
	}

}
