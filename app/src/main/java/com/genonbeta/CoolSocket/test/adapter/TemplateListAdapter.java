package com.genonbeta.CoolSocket.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.database.TemplateListDatabase;
import java.util.ArrayList;
import android.util.*;

public class TemplateListAdapter extends BaseAdapter
{
    private Context mContext;
    private TemplateListDatabase mDatabase;
    private ArrayList<String> mList = new ArrayList<String>();

    public TemplateListAdapter(Context context)
	{
        this.mContext = context;
        this.mDatabase = new TemplateListDatabase(this.mContext);
    }

    public void update()
	{
        this.mList.clear();
        this.mDatabase.getList(this.mList);
		
        notifyDataSetChanged();
    }

    public TemplateListDatabase getDatabase()
	{
        return this.mDatabase;
    }

    @Override
    public int getCount()
	{
        return this.mList.size();
    }

    @Override
    public Object getItem(int i)
	{
        return this.mList.get(i);
    }

    @Override
    public long getItemId(int i)
	{
        return (long) 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
	{
        return getViewAt(LayoutInflater.from(this.mContext).inflate(R.layout.list, viewGroup, false), i);
    }

    public View getViewAt(View view, int i)
	{
        ((TextView) view.findViewById(R.id.text)).setText((String) getItem(i));
        return view;
    }
}
