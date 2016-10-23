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
import android.widget.Toast;

import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.database.TemplateListDatabase;

public class NewTemplateDialog extends Builder
{
    public OnClickListener mExtNegative;
    public OnClickListener mExtPositive;
    OnClickListener mPositive;
    private TemplateListDatabase mDatabase;
    private EditText mText;

    public NewTemplateDialog(Context context, TemplateListDatabase templateListDatabase, OnClickListener onClickListener, OnClickListener onClickListener2)
    {
        super(context);


        this.mPositive = new OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Editable text = NewTemplateDialog.this.mText.getText();

                if (!"".equals(text.toString()))
                {
                    Toast.makeText(NewTemplateDialog.this.getContext(), NewTemplateDialog.this.mDatabase.add(text.toString()) ? "Added to list" : "Not added to list. Possibly it already exists", 0).show();
                }

                NewTemplateDialog.this.mExtPositive.onClick(dialogInterface, i);
            }
        };

        this.mDatabase = templateListDatabase;
        this.mExtPositive = onClickListener;
        this.mExtNegative = onClickListener2;

        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.layout_new_template, (ViewGroup) null);
        this.mText = (EditText) inflate.findViewById(R.id.layout_new_template_edit_text);

        setTitle("Add new template");
        setNegativeButton("Cancel", this.mExtNegative);
        setPositiveButton("Add", this.mPositive);
        setView(inflate);
    }
}
