package com.genonbeta.CoolSocket.test.fragment;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.genonbeta.CoolSocket.test.HomeActivity;
import com.genonbeta.CoolSocket.test.adapter.TemplateListAdapter;
import com.genonbeta.CoolSocket.test.dialog.EditTemplateDialog;
import com.genonbeta.CoolSocket.test.dialog.NewTemplateDialog;
import java.util.HashSet;
import android.widget.*;
import android.util.*;
import android.app.*;

public class TemplateListFragment extends ListFragment
{
    private TemplateListAdapter mAdapter;
    private ChoiceListener mChoiceListener = new ChoiceListener();
    private boolean mIsMultiscreen = false;
    private OnClickListener mPositive = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int i)
		{
			TemplateListFragment.this.mAdapter.update();
		}
	};

    private class ChoiceListener implements MultiChoiceModeListener
	{
        protected HashSet<String> mCheckedList = new HashSet<String>();
        protected MenuItem mEdit;

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
		{
            actionMode.setTitle("Edit templates");

            menu.add("Delete");
            this.mEdit = menu.add("Edit");

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu)
		{
            this.mCheckedList.clear();
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem)
		{
            if ("Delete".equals(menuItem.getTitle()))
			{
                for (String delete : this.mCheckedList)
				{
                    TemplateListFragment.this.mAdapter.getDatabase().delete(delete);
                }
            }
			else if ("Edit".equals(menuItem.getTitle()) || this.mCheckedList.size() != 1)
			{
                EditTemplateDialog editTemplateDialog = new EditTemplateDialog(TemplateListFragment.this.getActivity(), TemplateListFragment.this.mAdapter.getDatabase(), TemplateListFragment.this.mPositive, null, (String) this.mCheckedList.toArray()[0]);
                editTemplateDialog.show();
            }

            actionMode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode)
		{
            TemplateListFragment.this.mAdapter.update();
            this.mCheckedList.clear();
            this.mEdit = null;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean isSelected)
		{
            String str = (String) TemplateListFragment.this.mAdapter.getItem(position);

            if (isSelected)
                this.mCheckedList.add(str);
			else
                this.mCheckedList.remove(str);

            this.mEdit.setVisible(this.mCheckedList.size() == 1);
            actionMode.setSubtitle(TemplateListFragment.this.getListView().getCheckedItemCount() + " selected");
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle)
	{
        super.onActivityCreated(bundle);

        this.mAdapter = new TemplateListAdapter(getActivity());

        if (getActivity() instanceof HomeActivity)
            this.mIsMultiscreen = ((HomeActivity) getActivity()).isMultiscreen();

        setListAdapter(this.mAdapter);
        setHasOptionsMenu(true);
        setEmptyText("No template");
		
        getListView().setPadding(15, 0, 15, 0);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(this.mChoiceListener);
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);

		if (this.mIsMultiscreen)
		{
			getMessengerFragment().setMessageBox((String) this.mAdapter.getItem(position), true);
			return;
		}

		Intent intent = new Intent();
		intent.putExtra(MessengerFragment.EXTRA_MESSAGE, (String) this.mAdapter.getItem(position));

		if (getActivity().getParent() != null)
			getActivity().getParent().setResult(Activity.RESULT_OK, intent);
		else
			getActivity().setResult(Activity.RESULT_OK, intent);

		getActivity().finish();
	}

    @Override
    public void onResume()
	{
        super.onResume();
        this.mAdapter.update();
    }
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
	{
        super.onCreateOptionsMenu(menu, menuInflater);
        menu.add(0, 0, 0, "Add Template").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
	{
        if ("Add Template".equals(menuItem.getTitle()))
		{
            NewTemplateDialog newTemplateDialog = new NewTemplateDialog(getActivity(), TemplateListFragment.this.mAdapter.getDatabase(), TemplateListFragment.this.mPositive, (OnClickListener) null);
            newTemplateDialog.show();
            return true;
        }
		else if ("Remove".equals(menuItem.getTitle()))
		{
            return true;
        }
		
        return super.onOptionsItemSelected(menuItem);
    }

    private MessengerFragment getMessengerFragment()
	{
        return ((HomeActivity) getActivity()).getMessengerFragment();
    }
}
