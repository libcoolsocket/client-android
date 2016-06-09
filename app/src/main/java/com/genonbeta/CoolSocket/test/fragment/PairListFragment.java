package com.genonbeta.CoolSocket.test.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.genonbeta.CoolSocket.test.HomeActivity;
import com.genonbeta.CoolSocket.test.adapter.PairListAdapter;
import com.genonbeta.CoolSocket.test.helper.PairListHelper;
import com.genonbeta.core.content.Intent;

public class PairListFragment extends ListFragment
{
	private PairListAdapter mAdapter;
	private boolean mIsMultiscreen = false;
	private IntentFilter mFilter = new IntentFilter();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, android.content.Intent intent)
		{
			Toast.makeText(PairListFragment.this.getActivity(), "List has been refreshed", Toast.LENGTH_SHORT).show();
			PairListFragment.this.mAdapter.notifyDataSetChanged();
		}
	};

    @Override
    public void onActivityCreated(Bundle bundle)
	{
        super.onActivityCreated(bundle);

        this.mAdapter = new PairListAdapter(getActivity());
        this.mFilter.addAction(Intent.ACTION_NOTIFY_CHANGES);

        if (getActivity() instanceof HomeActivity)
            this.mIsMultiscreen = ((HomeActivity) getActivity()).isMultiscreen();

        this.setListAdapter(this.mAdapter);
        this.setHasOptionsMenu(true);
		this.setEmptyText("No pair was found");
		
        this.getListView().setPadding(15, 0, 15, 0);

        if (PairListHelper.getScanner().isScannerAvaiable())
            this.mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume()
	{
        super.onResume();
        getActivity().registerReceiver(this.mReceiver, this.mFilter);
    }

    @Override
    public void onPause()
	{
        super.onPause();
        getActivity().unregisterReceiver(this.mReceiver);
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);

		if (this.mIsMultiscreen)
		{
            getMessengerFragment().setServerText((String) this.mAdapter.getItem(position));
            return;
        }

        android.content.Intent intent = new android.content.Intent();
        intent.putExtra(MessengerFragment.EXTRA_PEER_ADDRESS, (String) this.mAdapter.getItem(position));

        if (getActivity().getParent() != null)
            getActivity().getParent().setResult(getActivity().RESULT_OK, intent);
		else
            getActivity().setResult(getActivity().RESULT_OK, intent);

        getActivity().finish();
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
	{
        super.onCreateOptionsMenu(menu, menuInflater);
        menu.add(1, 1, 1, "Scan for Peer").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
	{
        if ("Scan for Peer".equals(menuItem.getTitle()))
		{
			this.mAdapter.requestUpdate();
			return true;
		}
		
        return super.onOptionsItemSelected(menuItem);
    }

    private MessengerFragment getMessengerFragment()
	{
        return ((HomeActivity) getActivity()).getMessengerFragment();
    }
}
