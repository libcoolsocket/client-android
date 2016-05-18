package com.genonbeta.CoolSocket.test.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.helper.PairListHelper;
import com.genonbeta.CoolSocket.test.helper.PairListHelper.DeviceInfo;
import com.genonbeta.CoolSocket.test.helper.PairListHelper.ResultHandler;
import com.genonbeta.core.content.Intent;

public class PairListAdapter extends BaseAdapter
{
    private Context mContext;
    private ResultHandler mHandler;
    private ArrayMap<String, DeviceInfo> mIndex = new ArrayMap<String, DeviceInfo>();

    public PairListAdapter(Context context)
	{
        this.mHandler = new ResultHandler() {
            @Override
            public void onThreadsCompleted()
			{
                super.onThreadsCompleted();
                mContext.sendBroadcast(Intent.getNotifyIntent());
            }
        };
		
        this.mContext = context;
    }

    public void requestUpdate()
	{
        Toast.makeText(this.mContext, PairListHelper.update(this.mHandler) ? "Scan started" : "Still scanning. When it ends, list will be refreshed", 1).show();
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
        return getViewAt(LayoutInflater.from(this.mContext).inflate(R.layout.list_pair, viewGroup, false), i);
    }

    public View getViewAt(View view, int i)
	{
		String str = (String) getItem(i);
        DeviceInfo deviceInfo = this.mIndex.get(str);
		
        TextView textView = (TextView) view.findViewById(R.id.list_text2);
        ((TextView) view.findViewById(R.id.list_text)).setText(str);
		
		StringBuilder stringBuilder = new StringBuilder();
		
        if (deviceInfo.trebleShot || deviceInfo.coolSocket || deviceInfo.deviceController)
		{
            textView.setVisibility(0);
            if (deviceInfo.trebleShot)
                stringBuilder = stringBuilder.append("TShot ");
				
            if (deviceInfo.coolSocket)
                stringBuilder = stringBuilder.append("CSocket ");
				
            if (deviceInfo.deviceController)
                stringBuilder = stringBuilder.append("DContrllr ");
        }
		
        textView.setText(stringBuilder);
		
        return view;
    }
}
