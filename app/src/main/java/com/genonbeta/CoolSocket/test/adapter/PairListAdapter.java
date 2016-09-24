package com.genonbeta.CoolSocket.test.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.helper.GAnimater;
import com.genonbeta.CoolSocket.test.helper.PairListHelper;
import com.genonbeta.CoolSocket.test.helper.PairListHelper.DeviceInfo;
import com.genonbeta.CoolSocket.test.helper.PairListHelper.ResultHandler;
import com.genonbeta.core.content.Intent;

import java.net.InetAddress;

public class PairListAdapter extends BaseAdapter
{
    private Context mContext;
    private ResultHandler mHandler = new ResultHandler()
    {
        @Override
        public void onDeviceFound(InetAddress inetAddress)
        {
            super.onDeviceFound(inetAddress);
            mContext.sendBroadcast(Intent.getNotifyIntent());
        }
    };
    private ArrayMap<String, DeviceInfo> mIndex = new ArrayMap<>();

    public PairListAdapter(Context context)
    {
        this.mContext = context;
    }

    public void requestUpdate()
    {
        Toast.makeText(this.mContext, PairListHelper.update(this.mHandler) ? "Scan started" : "Still scanning. When it ends, list will be refreshed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyDataSetChanged()
    {
        this.mIndex.clear();

        for (String str : PairListHelper.getList().keySet())
        {
            mIndex.put(str, PairListHelper.getList().get(str));
        }

        super.notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return this.mIndex.size();
    }

    @Override
    public Object getItem(int i)
    {
        return this.mIndex.keyAt(i);
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
            view = LayoutInflater.from(this.mContext).inflate(R.layout.list_pair, viewGroup, false);

        return getViewAt(view, i);
    }

    public View getViewAt(View view, int i)
    {
        String str = (String) getItem(i);
        DeviceInfo deviceInfo = this.mIndex.get(str);


        ((TextView) view.findViewById(R.id.list_text)).setText(str);
        TextView textView2 = (TextView) view.findViewById(R.id.list_text2);
        TextView textView3 = (TextView) view.findViewById(R.id.list_text3);

        StringBuilder stringBuilder = new StringBuilder();

        if (!str.equals(textView2.getText().toString()))
        {
            AnimationSet set = GAnimater.getAnimation(GAnimater.APPEAR);
            view.setAnimation(set);
        }

        if (deviceInfo.trebleShot || deviceInfo.coolSocket || deviceInfo.deviceController)
        {
            textView2.setVisibility(View.VISIBLE);

            if (deviceInfo.trebleShot)
                stringBuilder = stringBuilder.append("TShot ");

            if (deviceInfo.coolSocket)
                stringBuilder = stringBuilder.append("CSocket ");

            if (deviceInfo.deviceController)
                stringBuilder = stringBuilder.append("DContrllr ");
        }

        textView2.setText(stringBuilder);
        textView3.setVisibility((deviceInfo.deviceName != null) ? View.VISIBLE : View.GONE);
        textView3.setText(deviceInfo.deviceName);

        return view;
    }
}