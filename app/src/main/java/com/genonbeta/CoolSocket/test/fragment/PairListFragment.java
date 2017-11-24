package com.genonbeta.CoolSocket.test.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.genonbeta.CoolSocket.test.HomeActivity;
import com.genonbeta.CoolSocket.test.R;
import com.genonbeta.CoolSocket.test.adapter.PairListAdapter;
import com.genonbeta.CoolSocket.test.database.MainDatabase;
import com.genonbeta.CoolSocket.test.dialog.ServerActionDialog;
import com.genonbeta.CoolSocket.test.helper.PairListHelper;
import com.genonbeta.android.database.CursorItem;
import com.genonbeta.android.database.util.QueryLoader;

import java.net.InetAddress;
import java.util.HashSet;

public class PairListFragment extends ListFragment
{
	private static final int TASK_LOAD = 1;

	private boolean mIsMultiScreen = false;
	private PairListAdapter mAdapter;
	private MainDatabase mDatabase;
	private ChoiceListener mChoiceListener = new ChoiceListener();
	private DialogInterface.OnClickListener mOnClickListener = new DialogInterface.OnClickListener()
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

		mDatabase = new MainDatabase(getActivity());
		mAdapter = new PairListAdapter(getActivity(), mDatabase);

		if (getActivity() instanceof HomeActivity)
			this.mIsMultiScreen = ((HomeActivity) getActivity()).isMultiscreen();

		setListAdapter(mAdapter);
		setHasOptionsMenu(true);
		setEmptyText(getString(R.string.msg_pairlist_no_device));

		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(this.mChoiceListener);

		getLoaderManager().initLoader(TASK_LOAD, bundle, new QueryLoader.DefaultLoaderCallback(mAdapter));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);

		CursorItem cursorItem = ((CursorItem) mAdapter.getItem(position));
		boolean isServer = cursorItem.exists(PairListAdapter.COLUMN_EXTRA_FAKE_ITEM);

		String title = cursorItem.getString(isServer ? MainDatabase.COLUMN_SERVERS_ADDRESS : MainDatabase.COLUMN_SERVERS_TITLE);

		if (this.mIsMultiScreen)
		{
			getMessengerFragment().setServerText(title);
			return;
		}

		android.content.Intent intent = new android.content.Intent();
		intent.putExtra(MessengerFragment.EXTRA_PEER_ADDRESS, title);

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
		menuInflater.inflate(R.menu.fragment_pairlist, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		int id = menuItem.getItemId();

		if (id == R.id.menu_add_server)
		{
			new ServerActionDialog(getContext(), mDatabase, mOnClickListener).show();
		}
		else if (id == R.id.menu_scan_peers)
		{
			updateList();
			return true;
		}

		return super.onOptionsItemSelected(menuItem);
	}

	private MessengerFragment getMessengerFragment()
	{
		return ((HomeActivity) getActivity()).getMessengerFragment();
	}

	public void refreshList()
	{
		getLoaderManager().restartLoader(TASK_LOAD, null, new QueryLoader.DefaultLoaderCallback(mAdapter));
	}

	private void updateList()
	{
		Snackbar.make(getView(),
				PairListHelper.update(new PairListHelper.ResultHandler()
				{
					@Override
					public void onRunning(InetAddress address)
					{
						super.onRunning(address);
					}

					@Override
					public void onDeviceFound(InetAddress inetAddress)
					{
						super.onDeviceFound(inetAddress);
						refreshList();
					}
				}) ? R.string.msg_scan_start : R.string.msg_scan_running, Snackbar.LENGTH_SHORT).show();
	}

	private class ChoiceListener implements AbsListView.MultiChoiceModeListener
	{
		protected HashSet<Integer> mCheckedList = new HashSet<>();
		protected MenuItem mMenuItemEdit;

		@Override
		public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
		{
			actionMode.setTitle(R.string.edit_servers);

			new MenuInflater(getContext()).inflate(R.menu.actionmode_edit_server, menu);
			mMenuItemEdit = menu.findItem(R.id.menu_actionmode_server_edit);

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

			if (id == R.id.menu_actionmode_server_delete)
			{
				for (int uniqueId : this.mCheckedList)
					mDatabase.getWritableDatabase().delete(MainDatabase.TABLE_SERVERS,
							MainDatabase.COLUMN_SERVERS_ID + "=?", new String[]{String.valueOf(uniqueId)});
			}
			else if (id == R.id.menu_actionmode_server_edit)
			{
				int editId = (int) mCheckedList.toArray()[0];

				if (editId == 0)
				{
					Toast.makeText(getContext(), R.string.server_not_actual, Toast.LENGTH_SHORT).show();
					return false;
				}
				else
					new ServerActionDialog(getActivity(), mAdapter.getDatabase(), mOnClickListener, editId).show();
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
			int str = mAdapter.getItemUniqueId(position);

			if (str == 0)
			{
				if (isSelected)
				{
					getListView().setItemChecked(position, false);
					Toast.makeText(getContext(), R.string.server_not_actual, Toast.LENGTH_SHORT).show();
				}
			}
			else
			{
				if (isSelected)
					mCheckedList.add(str);
				else
					mCheckedList.remove(str);

				mMenuItemEdit.setVisible(mCheckedList.size() == 1);
				actionMode.setSubtitle(getString(R.string.msg_edit_template_selected_count, getListView().getCheckedItemCount()));
			}
		}
	}
}
