package com.genonbeta.CoolSocket.test.database.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.genonbeta.android.database.CursorItem;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by: veli
 * Date: 2/3/17 11:25 AM
 */

abstract public class AbstractDatabaseAdapter extends BaseAdapter
{
	private Context mContext;
	private LayoutInflater mInflater;
	private SQLQuery.Select mQuery;
	private SQLiteDatabase mDatabase;
	private ArrayList<CursorItem> mList = new ArrayList<>();

	public AbstractDatabaseAdapter(Context context, SQLiteDatabase db, SQLQuery.Select selectQuery)
	{
		mContext = context;
		mDatabase = db;
		mQuery = selectQuery;
		mInflater = LayoutInflater.from(context);
	}

	public ArrayList<CursorItem> onLoad()
	{
		return mDatabase.getTable(mQuery);
	}

	public void onUpdate(ArrayList<CursorItem> itemList)
	{
		mList.clear();
		mList.addAll(itemList);
	}

	public Context getContext()
	{
		return mContext;
	}

	@Override
	public int getCount()
	{
		return getList().size();
	}

	public SQLiteDatabase getDatabase()
	{
		return mDatabase;
	}

	@Override
	public Object getItem(int i)
	{
		return getList().get(i);
	}

	@Override
	public long getItemId(int i)
	{
		return 0;
	}

	public LayoutInflater getLayoutInflater()
	{
		return mInflater;
	}

	public ArrayList<CursorItem> getList()
	{
		return mList;
	}

	public SQLQuery.Select getQuery()
	{
		return mQuery;
	}
}