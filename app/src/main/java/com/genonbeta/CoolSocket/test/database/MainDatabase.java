package com.genonbeta.CoolSocket.test.database;

import android.content.Context;

import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.SQLiteDatabase;

/**
 * Created by: veli
 * Date: 1/31/17 5:18 PM
 */

public class MainDatabase extends SQLiteDatabase
{
	protected static final String DATABASE_NAME = "MainDatabase";

	public static final String TABLE_MESSAGE = "messages";
	public static final String COLUMN_MESSAGE_CLIENT = "client";
	public static final String COLUMN_MESSAGE_ISRECEIVED = "isReceived";
	public static final String COLUMN_MESSAGE_ISERROR = "isError";
	public static final String COLUMN_MESSAGE_MESSAGE = "message";

	public static final String TABLE_TEMPLATE = "templates";
	public static final String COLUMN_TEMPLATE_MESSAGE = "message";

	public static final String TABLE_SERVERS = "servers";
	public static final String COLUMN_SERVERS_ID = "id";
	public static final String COLUMN_SERVERS_TITLE = "title";
	public static final String COLUMN_SERVERS_ADDRESS = "address";

	public MainDatabase(Context context)
	{
		super(context, DATABASE_NAME + ".db", null, 1);
	}

	@Override
	public void onCreate(android.database.sqlite.SQLiteDatabase sqLiteDatabase)
	{
		new SQLQuery.CreateTable(TABLE_MESSAGE)
				.addColumn(COLUMN_MESSAGE_CLIENT, SQLQuery.Type.TEXT.toString(), false)
				.addColumn(COLUMN_MESSAGE_MESSAGE, SQLQuery.Type.TEXT.toString(), false)
				.addColumn(COLUMN_MESSAGE_ISRECEIVED, SQLQuery.Type.INTEGER.toString(), false)
				.addColumn(COLUMN_MESSAGE_ISERROR, SQLQuery.Type.INTEGER.toString(), false)
				.exec(sqLiteDatabase);

		new SQLQuery.CreateTable(TABLE_TEMPLATE)
				.addColumn(COLUMN_TEMPLATE_MESSAGE, SQLQuery.Type.TEXT.toString(), false)
				.exec(sqLiteDatabase);

		new SQLQuery.CreateTable(TABLE_SERVERS)
				.addColumn(COLUMN_SERVERS_ID, SQLQuery.Type.INTEGER.toString(), false)
				.addColumn(COLUMN_SERVERS_TITLE, SQLQuery.Type.TEXT.toString(), false)
				.addColumn(COLUMN_SERVERS_ADDRESS, SQLQuery.Type.TEXT.toString(), false)
				.exec(sqLiteDatabase);
	}

	@Override
	public void onUpgrade(android.database.sqlite.SQLiteDatabase sqLiteDatabase, int i, int i1)
	{

	}
}