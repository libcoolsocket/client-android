package com.genonbeta.CoolSocket.test.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.database.TemplateListDatabase;

public class EditTemplateDialog extends Builder
{
    public OnClickListener mExtNegative;
    public OnClickListener mExtPositive;
    OnClickListener mPositive;
    private TemplateListDatabase mDatabase;
    private String mOriginal;
    private EditText mText;

    public EditTemplateDialog(Context context, TemplateListDatabase templateListDatabase, OnClickListener onClickListener, OnClickListener onClickListener2, String str)
    {
        super(context);

        this.mPositive = new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Editable text = mText.getText();

                if (!"".equals(text.toString()))
                    mDatabase.edit(mOriginal, text.toString());

                mExtPositive.onClick(dialogInterface, i);
            }
        };

        this.mDatabase = templateListDatabase;
        this.mExtPositive = onClickListener;
        this.mExtNegative = onClickListener2;
        this.mOriginal = str;

        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.layout_new_template, (ViewGroup) null);
        this.mText = (EditText) inflate.findViewById(R.id.layout_new_template_edit_text);
        this.mText.setText(str);

        setTitle("Edit template");
        setNegativeButton("Cancel", this.mExtNegative);
        setPositiveButton("Save", this.mPositive);
        setView(inflate);
    }
}
