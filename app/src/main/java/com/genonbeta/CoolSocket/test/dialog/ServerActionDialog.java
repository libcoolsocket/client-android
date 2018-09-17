package com.genonbeta.CoolSocket.test.dialog;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.database.MainDatabase;
import com.genonbeta.android.database.CursorItem;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.SQLiteDatabase;

/**
 * Created by: veli
 * Date: 2/4/17 9:56 PM
 */

public class ServerActionDialog extends AlertDialog.Builder
{
	private AutoCompleteTextView mTitleText;
	private AutoCompleteTextView mAddressText;
	private int mEditedServerId = 0;

	public ServerActionDialog(Context context, final SQLiteDatabase db, final DialogInterface.OnClickListener onClickListener)
	{
		super(context);

		final View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_server_action, null);
		mTitleText = (AutoCompleteTextView) view.findViewById(R.id.layout_server_action_title_text);
		mAddressText = (AutoCompleteTextView) view.findViewById(R.id.layout_server_action_address_text);

		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				Editable titleText = mTitleText.getText();
				Editable addressText = mAddressText.getText();

				if (titleText.length() > 0 && addressText.length() > 0)
				{
					ContentValues values = new ContentValues();
					values.put(MainDatabase.COLUMN_SERVERS_ID, String.valueOf(System.currentTimeMillis() / 1000));
					values.put(MainDatabase.COLUMN_SERVERS_TITLE, titleText.toString());
					values.put(MainDatabase.COLUMN_SERVERS_ADDRESS, addressText.toString());

					if (mEditedServerId != 0)
						db.getWritableDatabase().update(MainDatabase.TABLE_SERVERS, values, MainDatabase.COLUMN_SERVERS_ID + "=?", new String[]{String.valueOf(mEditedServerId)});
					else if (db.getFirstFromTable(new SQLQuery.Select(MainDatabase.TABLE_SERVERS)
							.setWhere(MainDatabase.COLUMN_SERVERS_TITLE + "=?", titleText.toString())) == null)
						db.getWritableDatabase().insert(MainDatabase.TABLE_SERVERS, null, values);
					else
						Toast.makeText(getContext(), R.string.mesg_serverExistError, Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(getContext(), R.string.mesg_emptyFieldError, Toast.LENGTH_SHORT).show();

				onClickListener.onClick(dialogInterface, i);
			}
		};

		setTitle(R.string.butn_addServer);
		setNegativeButton(R.string.butn_cancel, null);
		setPositiveButton(R.string.butn_save, positiveListener);
		setView(view);
	}

	public ServerActionDialog(Context context, SQLiteDatabase db, DialogInterface.OnClickListener onClickListener, int editedServerId)
	{
		this(context, db, onClickListener);
		setTitle(R.string.butn_editServer);

		mEditedServerId = editedServerId;

		CursorItem editedServer = db.getFirstFromTable(new SQLQuery.Select(MainDatabase.TABLE_SERVERS)
				.setWhere(MainDatabase.COLUMN_SERVERS_ID + "=?", String.valueOf(editedServerId)));

		if (editedServer != null)
		{
			mTitleText.getText().append(editedServer.getString(MainDatabase.COLUMN_SERVERS_TITLE));
			mAddressText.getText().append(editedServer.getString(MainDatabase.COLUMN_SERVERS_ADDRESS));
		}
		else
			Toast.makeText(context, R.string.mesg_serverNotFound, Toast.LENGTH_SHORT).show();
	}
}
