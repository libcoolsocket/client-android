package com.genonbeta.CoolSocket.test.database;

import android.content.Context;

import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.SQLType;
import com.genonbeta.android.database.SQLValues;
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
	public void onCreate(android.database.sqlite.SQLiteDatabase db)
	{
		SQLQuery.createTables(db, getDatabaseTables());
	}

	@Override
	public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int i, int i1)
	{

	}

	public static SQLValues getDatabaseTables()
	{
		SQLValues sqlValues = new SQLValues();

		sqlValues.defineTable(TABLE_MESSAGE)
				.define(new SQLValues.Column(COLUMN_MESSAGE_CLIENT, SQLType.TEXT, false))
				.define(new SQLValues.Column(COLUMN_MESSAGE_MESSAGE, SQLType.TEXT, false))
				.define(new SQLValues.Column(COLUMN_MESSAGE_ISRECEIVED, SQLType.INTEGER, false))
				.define(new SQLValues.Column(COLUMN_MESSAGE_ISERROR, SQLType.INTEGER, false));

		sqlValues.defineTable(TABLE_TEMPLATE)
				.define(new SQLValues.Column(COLUMN_TEMPLATE_MESSAGE, SQLType.TEXT, false));

		sqlValues.defineTable(TABLE_SERVERS)
				.define(new SQLValues.Column(COLUMN_SERVERS_ID, SQLType.INTEGER, false))
				.define(new SQLValues.Column(COLUMN_SERVERS_TITLE, SQLType.TEXT, false))
				.define(new SQLValues.Column(COLUMN_SERVERS_ADDRESS, SQLType.TEXT, false));

		return sqlValues;
	}
}