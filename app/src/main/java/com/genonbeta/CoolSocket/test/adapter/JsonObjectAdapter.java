package com.genonbeta.CoolSocket.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.genonbeta.CoolSocket.test.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class JsonObjectAdapter extends BaseAdapter
{
	private Context mContext;
	private JSONObject mObject;
	private ArrayList<String> mKeys;
	private ArrayList<String> mValues;

	public JsonObjectAdapter(Context context, JSONObject jsonObject)
	{
		mKeys = new ArrayList<String>();
		mValues = new ArrayList<String>();
		mObject = jsonObject;
		mContext = context;

		if (jsonObject.length() > 0)
		{
			Iterator keys = jsonObject.keys();

			do
			{
				try
				{
					String str = (String) keys.next();

					mKeys.add(str);
					mValues.add(mObject.getString(str));
				} catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
			while (keys.hasNext());
		}
	}

	public ArrayList<String> getKeys()
	{
		return mKeys;
	}

	public ArrayList<String> getValues()
	{
		return mValues;
	}

	@Override
	public int getCount()
	{
		return mObject.length();
	}

	@Override
	public Object getItem(int i)
	{
		return mKeys.get(i);
	}

	@Override
	public long getItemId(int i)
	{
		return (long) 0;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		if (view == null)
			view = LayoutInflater.from(this.mContext).inflate(R.layout.list_json, viewGroup, false);

		TextView textView = (TextView) view.findViewById(R.id.list_text);
		TextView textView2 = (TextView) view.findViewById(R.id.list_text2);

		textView.setText(mKeys.get(i));
		textView2.setText(mValues.get(i));

		return view;
	}
}
