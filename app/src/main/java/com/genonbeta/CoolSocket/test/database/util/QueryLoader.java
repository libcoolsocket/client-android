package com.genonbeta.CoolSocket.test.database.util;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import com.genonbeta.CoolSocket.test.database.adapter.AbstractDatabaseAdapter;
import com.genonbeta.android.database.CursorItem;

import java.util.ArrayList;

/**
 * Created by: veli
 * Date: 2/3/17 4:27 PM
 */

public class QueryLoader extends AsyncTaskLoader<ArrayList<CursorItem>>
{
	private AbstractDatabaseAdapter mAdapter;
	private DefaultLoaderCallback mCallback;

	public QueryLoader(AbstractDatabaseAdapter adapter, DefaultLoaderCallback callback)
	{
		super(adapter.getContext());
		mAdapter = adapter;
		mCallback = callback;
	}

	@Override
	protected void onStartLoading()
	{
		super.onStartLoading();
		forceLoad();
	}

	@Override
	public ArrayList<CursorItem> loadInBackground()
	{
		return mCallback.passLoading() ? null : mAdapter.onLoad();
	}

	public static class DefaultLoaderCallback implements LoaderManager.LoaderCallbacks<ArrayList<CursorItem>>
	{
		private AbstractDatabaseAdapter mAdapter;
		private QueryLoader mLoader;
		private OnCreateQueryLoaderListener mListener;
		private boolean mPassLoading = false;

		public DefaultLoaderCallback(AbstractDatabaseAdapter adapter)
		{
			mAdapter = adapter;
			mLoader = new QueryLoader(mAdapter, this);
		}

		public DefaultLoaderCallback(AbstractDatabaseAdapter adapter, OnCreateQueryLoaderListener listener)
		{
			this(adapter);
			mListener = listener;
		}

		@Override
		public Loader<ArrayList<CursorItem>> onCreateLoader(int id, Bundle args)
		{
			if (mListener != null)
				mListener.onCreateQueryLoader(mLoader);

			return mLoader;
		}

		@Override
		public void onLoadFinished(Loader<ArrayList<CursorItem>> loader, ArrayList<CursorItem> data)
		{
			if (!passLoading())
				mAdapter.onUpdate(data);

			mAdapter.notifyDataSetChanged();
		}

		@Override
		public void onLoaderReset(Loader<ArrayList<CursorItem>> loader)
		{
		}

		public boolean passLoading()
		{
			return mPassLoading;
		}

		public DefaultLoaderCallback passLoading(boolean passLoading)
		{
			mPassLoading = passLoading;

			return this;
		}
	}

	public static interface OnCreateQueryLoaderListener
	{
		public void onCreateQueryLoader(QueryLoader loader);
	}
}