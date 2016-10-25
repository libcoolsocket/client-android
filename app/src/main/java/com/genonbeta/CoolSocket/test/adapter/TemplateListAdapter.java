package com.genonbeta.CoolSocket.test.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.database.TemplateListDatabase;
import com.genonbeta.CoolSocket.test.helper.TemplateItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TemplateListAdapter extends BaseAdapter
{
    private Context mContext;
    private TemplateListDatabase mDatabase;
    private ArrayList<TemplateItem> mList = new ArrayList<>();

    public TemplateListAdapter(Context context)
    {
        this.mContext = context;
        this.mDatabase = new TemplateListDatabase(this.mContext);
    }

    public void update()
    {
        ArrayList<String> list = new ArrayList<String>();

        this.mList.clear();
        this.mDatabase.getList(list);

        for (String template : list)
        {
            TemplateItem item = new TemplateItem();

            item.template = template;

            try
            {
                JSONObject object = new JSONObject(template);

                item.isJson = true;
                item.template = object.toString(1);
                item.template = item.template.substring(2, item.template.length() - 2);
            } catch (JSONException e)
            {
                e.printStackTrace();
            }

            item.isShortcut = !item.isJson && TemplateListDatabase.isShortcut(template);


            item.templateOriginal = template;

            this.mList.add(item);
        }

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
        if (view == null)
            view = LayoutInflater.from(this.mContext).inflate(R.layout.list, viewGroup, false);

        return getViewAt(view, i);
    }

    public View getViewAt(View view, int i)
    {
        TemplateItem item = (TemplateItem) getItem(i);
        TextView text = (TextView) view.findViewById(R.id.text);

        if (item.isJson)
            text.setTextColor(Color.GREEN);
        else if (item.isShortcut)
        {
            text.setMaxLines(1);
            text.setTextColor(Color.CYAN);
            text.setEllipsize(TextUtils.TruncateAt.END);
        }

        text.setText(item.template);

        return view;
    }
}
