package com.genonbeta.CoolSocket.test.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.genonbeta.CoolSocket.test.HomeActivity;
import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.adapter.TemplateListAdapter;
import com.genonbeta.CoolSocket.test.database.MainDatabase;
import com.genonbeta.CoolSocket.test.dialog.TemplateActionDialog;
import com.genonbeta.android.database.util.QueryLoader;

import java.util.HashSet;

public class TemplateListFragment extends ListFragment
{
	private static final int TASK_LOAD = 1;

	private boolean mIsMultiScreen = false;
	private TemplateListAdapter mAdapter;
	private MainDatabase mDatabase;
	private ChoiceListener mChoiceListener = new ChoiceListener();
	private DialogInterface.OnClickListener mPositive = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialogInterface, int i)
		{
			refreshList();
		}
	};

	@Override
	public void onActivityCreated(Bundle bundle)
	{
		super.onActivityCreated(bundle);

		mDatabase = new MainDatabase(getContext());
		mAdapter = new TemplateListAdapter(getActivity(), mDatabase);

		if (getActivity() instanceof HomeActivity)
			this.mIsMultiScreen = ((HomeActivity) getActivity()).isMultiscreen();

		setListAdapter(this.mAdapter);
		setHasOptionsMenu(true);
		setEmptyText(getString(R.string.msg_templatelist_no_template));

		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(this.mChoiceListener);

		getLoaderManager().initLoader(TASK_LOAD, bundle, new QueryLoader.DefaultLoaderCallback(mAdapter));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);

		if (this.mIsMultiScreen)
		{
			getMessengerFragment().setMessageBox(getTemplate(position), true);
			return;
		}

		Intent intent = new Intent();
		intent.putExtra(MessengerFragment.EXTRA_MESSAGE, getTemplate(position));

		if (getActivity().getParent() != null)
			getActivity().getParent().setResult(Activity.RESULT_OK, intent);
		else
			getActivity().setResult(Activity.RESULT_OK, intent);

		getActivity().finish();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
	{
		super.onCreateOptionsMenu(menu, menuInflater);
		menuInflater.inflate(R.menu.fragment_templatelist, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		int id = menuItem.getItemId();

		if (id == R.id.menu_template_add)
		{
			new TemplateActionDialog(getActivity(), mAdapter.getDatabase(), mPositive).show();
			return true;
		}

		return super.onOptionsItemSelected(menuItem);
	}

	private String getTemplate(int position)
	{
		return mAdapter.getList().get(position).getString(MainDatabase.COLUMN_TEMPLATE_MESSAGE);
	}

	private MessengerFragment getMessengerFragment()
	{
		return ((HomeActivity) getActivity()).getMessengerFragment();
	}

	public void refreshList()
	{
		getLoaderManager().restartLoader(TASK_LOAD, null, new QueryLoader.DefaultLoaderCallback(mAdapter));
	}

	private class ChoiceListener implements AbsListView.MultiChoiceModeListener
	{
		protected HashSet<String> mCheckedList = new HashSet<>();
		protected MenuItem mMenuItemEdit;

		@Override
		public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
		{
			actionMode.setTitle(R.string.edit_templates);

			new MenuInflater(getContext()).inflate(R.menu.actionmode_edit_templates, menu);
			mMenuItemEdit = menu.findItem(R.id.menu_actionmode_template_edit);

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode actionMode, Menu menu)
		{
			mCheckedList.clear();
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem)
		{
			int id = menuItem.getItemId();

			if (id == R.id.menu_actionmode_template_delete)
			{
				for (String template : this.mCheckedList)
				{
					mDatabase.getWritableDatabase().delete(MainDatabase.TABLE_TEMPLATE,
							MainDatabase.COLUMN_TEMPLATE_MESSAGE + "=?", new String[] {template});
				}
			} else if (id == R.id.menu_actionmode_template_edit)
			{
				new TemplateActionDialog(getActivity(), mAdapter.getDatabase(), mPositive, (String) mCheckedList.toArray()[0]).show();
			}

			actionMode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode actionMode)
		{
			refreshList();

			mCheckedList.clear();
			mMenuItemEdit = null;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean isSelected)
		{
			String str = getTemplate(position);

			if (isSelected)
				mCheckedList.add(str);
			else
				mCheckedList.remove(str);

			mMenuItemEdit.setVisible(mCheckedList.size() == 1);
			actionMode.setSubtitle(getString(R.string.msg_edit_template_selected_count, getListView().getCheckedItemCount()));
		}
	}
}
