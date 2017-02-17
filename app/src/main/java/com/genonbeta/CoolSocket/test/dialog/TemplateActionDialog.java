package com.genonbeta.CoolSocket.test.dialog;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.database.MainDatabase;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.SQLiteDatabase;

/**
 * Created by: veli
 * Date: 2/4/17 9:38 PM
 */

public class TemplateActionDialog extends AlertDialog.Builder
{
	private EditText mEditText;
	private String mEditedTemplate;

	public TemplateActionDialog(Context context, final SQLiteDatabase db, final DialogInterface.OnClickListener onClickListener)
	{
		super(context);

		final View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_template_action, null);
		mEditText = (EditText) view.findViewById(R.id.layout_template_action_edit_text);

		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				Editable text = mEditText.getText();

				if (text.length() > 0)
				{
					if (db.getFirstFromTable(new SQLQuery.Select(MainDatabase.TABLE_TEMPLATE)
							.setWhere(MainDatabase.COLUMN_TEMPLATE_MESSAGE + "=?", text.toString())) == null)
					{
						ContentValues values = new ContentValues();
						values.put(MainDatabase.COLUMN_TEMPLATE_MESSAGE, text.toString());

						if (mEditedTemplate == null)
							db.getWritableDatabase().insert(MainDatabase.TABLE_TEMPLATE, null, values);
						else
							db.getWritableDatabase().update(MainDatabase.TABLE_TEMPLATE, values, MainDatabase.COLUMN_TEMPLATE_MESSAGE + "=?", new String[]{mEditedTemplate});
					}
					else
						Toast.makeText(getContext(), R.string.error_template_exist, Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(getContext(), R.string.error_template_empty, Toast.LENGTH_SHORT).show();

				onClickListener.onClick(dialogInterface, i);
			}
		};

		setTitle(R.string.add_template);
		setNegativeButton(R.string.cancel, null);
		setPositiveButton(R.string.save, positiveListener);
		setView(view);
	}

	public TemplateActionDialog(Context context, SQLiteDatabase db, DialogInterface.OnClickListener onClickListener, String editedTemplate)
	{
		this(context, db, onClickListener);
		setTitle(R.string.edit_template);

		mEditedTemplate = editedTemplate;
		mEditText.getText().append(editedTemplate);
	}
}
