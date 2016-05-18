package com.genonbeta.CoolSocket.test.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog.Builder;
import com.genonbeta.CoolSocket.test.adapter.JsonObjectAdapter;
import org.json.JSONObject;

public class JsonEditorDialog extends Builder
{
    private JsonObjectAdapter mAdapter;
    private Context mContext;
    private JSONObject mJson;
    
    public JsonEditorDialog(Context context, JSONObject jsonObject, final OnEditorClickListener removeListener, final OnEditorClickListener listItemSelected)
	{
        super(context);
		
        setTitle("JSON Index");
		
        this.mJson = jsonObject;
        this.mContext = context;
		
        if (jsonObject.length() < 1)
            setMessage("It's empty");
		else
		{
            this.mAdapter = new JsonObjectAdapter(context, jsonObject);
            setAdapter(this.mAdapter, new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						listItemSelected.onJsonClick(JsonEditorDialog.this, p1, p2);
					}
				}
			);
        }
		
        setNegativeButton("Close", (OnClickListener) null);
        setPositiveButton("Remove all", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					if (mAdapter != null)
					{
						for (String remove : mAdapter.getKeys())
						{
							mJson.remove(remove);
						}
					}
					
					if (removeListener != null)
						removeListener.onJsonClick(JsonEditorDialog.this, p1, p2);
				}
			}
		);
    }

    public JsonObjectAdapter getJsonAdapter()
	{
        return this.mAdapter;
    }
	
	public static interface OnEditorClickListener
	{
		public void onJsonClick(JsonEditorDialog editor, DialogInterface dialog, int position);
	}
}
