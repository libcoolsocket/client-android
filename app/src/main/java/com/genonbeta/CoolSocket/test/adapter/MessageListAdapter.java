package com.genonbeta.CoolSocket.test.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.helper.GAnimater;
import com.genonbeta.CoolSocket.test.helper.MessageItem;
import com.genonbeta.CoolSocket.test.helper.PairListHelper;

import java.util.ArrayList;

public class MessageListAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<MessageItem> mList;

    private int receivedColor = 0;
    private int sentColor = 0;
    private int errorColor = 0;

    public MessageListAdapter(Context context, ArrayList<MessageItem> arrayList)
    {
        this.mContext = context;
        this.mList = arrayList;

        this.receivedColor = ContextCompat.getColor(context, R.color.receivedMessage);
        this.sentColor = ContextCompat.getColor(context, R.color.sentMessage);
        this.errorColor = ContextCompat.getColor(context, R.color.errorMessage);
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
            view = LayoutInflater.from(this.mContext).inflate(R.layout.list_message, viewGroup, false);

        return getViewAt(view, i);
    }

    public View getViewAt(View view, int i)
    {
        TextView textView1 = (TextView) view.findViewById(R.id.list_text);
        TextView textView2 = (TextView) view.findViewById(R.id.list_text2);
        MessageItem messageItem = (MessageItem) getItem(i);

        String str = (messageItem.client == null || messageItem.client.equals("") || messageItem.client.equals("::1")) ? "localhost" : messageItem.client;

        if (PairListHelper.getList().containsKey(messageItem.client))
        {
            PairListHelper.DeviceInfo device = PairListHelper.getList().get(messageItem.client);

            if (device.deviceName != null)
                str += " @ " + device.deviceName;
        }

        textView1.setTextColor((messageItem.isError) ? this.errorColor : ((messageItem.isReceived) ? this.receivedColor : this.sentColor));

        textView1.setText(((messageItem.isReceived) ? "↓" : "↑") + " " + str);
        textView2.setText(messageItem.message);

        return view;
    }
}
