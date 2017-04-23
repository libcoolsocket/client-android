package com.genonbeta.CoolSocket.test.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog.Builder;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.adapter.JsonObjectAdapter;

import org.json.JSONObject;

public class JsonEditorDialog extends Builder
{
	private JsonObjectAdapter mAdapter;

	public JsonEditorDialog(final Context context, final JSONObject jsonObject, final OnEditorClickListener removeListener, final OnEditorClickListener listItemSelected, final ThirdOption thirdOption)
	{
		super(context);

		setTitle(R.string.title_json_editor);

		if (jsonObject.length() < 1)
			setMessage(R.string.msg_json_editor_list_empty);
		else
		{
			mAdapter = new JsonObjectAdapter(context, jsonObject);
			setAdapter(mAdapter, new OnClickListener()
					{
						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							listItemSelected.onJsonClick(JsonEditorDialog.this, p1, p2);
						}
					}
			);

			setPositiveButton(R.string.json_editor_button_edit, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					new JsonEditorDialog(context, jsonObject, removeListener).show();
				}
			});
		}

		if (thirdOption != null)
			setNeutralButton(thirdOption.getButtonName(), thirdOption);

		setNegativeButton(R.string.close, null);
	}

	public JsonEditorDialog(final Context context, final JSONObject jsonObject, final OnEditorClickListener removeListener)
	{
		super(context);

		setTitle(R.string.title_json_editor_edit);

		mAdapter = new JsonObjectAdapter(context, jsonObject);

		setAdapter(mAdapter, new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						jsonObject.remove(((String) mAdapter.getItem(p2)));

						if (removeListener != null)
							removeListener.onJsonClick(JsonEditorDialog.this, p1, p2);
					}
				}
		);

		setPositiveButton(R.string.json_editor_button_remove_all, new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						for (String remove : mAdapter.getKeys())
							jsonObject.remove(remove);

						if (removeListener != null)
							removeListener.onJsonClick(JsonEditorDialog.this, p1, p2);
					}
				}
		);

		setNegativeButton(R.string.close, null);
	}

	public JsonObjectAdapter getJsonAdapter()
	{
		return mAdapter;
	}

	public static interface OnEditorClickListener
	{
		public void onJsonClick(JsonEditorDialog editor, DialogInterface dialog, int position);
	}

	public abstract static class ThirdOption implements OnClickListener
	{
		public abstract String getButtonName();
	}
}
