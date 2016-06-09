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
        this.mKeys = new ArrayList<String>();
        this.mValues = new ArrayList<String>();
        this.mObject = jsonObject;
        this.mContext = context;
		
        if (jsonObject.length() > 0)
		{
            Iterator keys = jsonObject.keys();
			
            do {
                try
				{
                    String str = (String) keys.next();
					
                    this.mKeys.add(str);
                    this.mValues.add(this.mObject.getString(str));
                }
				catch (JSONException e)
				{
                    e.printStackTrace();
                }
            } while (keys.hasNext());
        }
    }

    public ArrayList<String> getKeys()
	{
        return this.mKeys;
    }

    public ArrayList<String> getValues()
	{
        return this.mValues;
    }

    @Override
    public int getCount()
	{
        return this.mObject.length();
    }

    @Override
    public Object getItem(int i)
	{
        return null;
    }

    @Override
    public long getItemId(int i)
	{
        return (long) 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
	{
        View inflate = LayoutInflater.from(this.mContext).inflate(R.layout.list_json, viewGroup, false);
		
        TextView textView = (TextView) inflate.findViewById(R.id.list_text);
		TextView textView2 = (TextView) inflate.findViewById(R.id.list_text2);
		
        textView.setText(this.mKeys.get(i));
        textView2.setText(this.mValues.get(i));
		
        return inflate;
    }
}
