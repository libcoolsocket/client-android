package com.genonbeta.CoolSocket.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.helper.MessageItem;
import java.util.ArrayList;

public class MessageListAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<MessageItem> mList;

    public MessageListAdapter(Context context, ArrayList<MessageItem> arrayList)
	{
        this.mContext = context;
        this.mList = arrayList;
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
        return getViewAt(LayoutInflater.from(this.mContext).inflate(R.layout.list_message, viewGroup, false), i);
    }

    public View getViewAt(View view, int i)
	{
        TextView textView = (TextView) view.findViewById(R.id.list_text);
        TextView textView2 = (TextView) view.findViewById(R.id.list_text2);
        MessageItem messageItem = (MessageItem) getItem(i);
		
        if (!messageItem.isReceived)
            view.setActivated(true);
		
        String str = (messageItem.client == null || messageItem.client.equals("") || messageItem.client.equals("::1")) ? "localhost" : messageItem.client;
		
        textView.setText(str + "> ");
        textView2.setText(messageItem.message);
		
        return view;
    }
}
