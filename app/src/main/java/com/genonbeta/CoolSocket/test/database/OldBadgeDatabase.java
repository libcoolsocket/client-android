package com.genonbeta.CoolSocket.test.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.genonbeta.CoolSocket.test.helper.MessageItem;

import java.util.ArrayList;

public class OldBadgeDatabase extends SQLiteOpenHelper
{
    public static final String COLUMN_CONNECTION_MESSAGE = "message";
    public static final String COLUMN_CONNECTION_PORT = "port";
    public static final String COLUMN_CONNECTION_SERVER = "server";
    public static final String COLUMN_LIST_CLIENT = "client";
    public static final String COLUMN_LIST_ISRECEIVED = "isReceived";
    public static final String COLUMN_LIST_ISERROR = "isError";
    public static final String COLUMN_LIST_MESSAGE = "message";
    public static final String DATABASE_NAME = "OldBadgeProvider";
    public static final String TABLE_CONNECTION = "Connection";
    public static final String TABLE_LIST = "List";
    private Context mContext;

    @Override
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2)
    {
    }

    public OldBadgeDatabase(Context context)
    {
        super(context, DATABASE_NAME, (CursorFactory) null, 1);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL("CREATE TABLE `" + TABLE_LIST + "` (`" + COLUMN_LIST_CLIENT + "` text NOT NULL, `" + COLUMN_LIST_MESSAGE + "` text NOT NULL, `" + COLUMN_LIST_ISRECEIVED + "` int NOT NULL, " + COLUMN_LIST_ISERROR + " int NOT NULL)");
    }

    public void add(String ip, String message, boolean isReceived, boolean isError)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_LIST_CLIENT, ip);
        contentValues.put(COLUMN_LIST_MESSAGE, message);
        contentValues.put(COLUMN_LIST_ISRECEIVED, isReceived ? 0 : 1);
        contentValues.put(COLUMN_LIST_ISERROR, isError ? 0 : 1);


        SQLiteDatabase writableDatabase = getWritableDatabase();

        writableDatabase.insert(TABLE_LIST, (String) null, contentValues);
        writableDatabase.close();
    }

    public void add(MessageItem messageItem)
    {
        add(messageItem.client, messageItem.message, messageItem.isReceived, messageItem.isError);
    }

    public void getList(ArrayList<MessageItem> arrayList)
    {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor query = readableDatabase.query(TABLE_LIST, new String[]{COLUMN_LIST_CLIENT, COLUMN_LIST_MESSAGE, COLUMN_LIST_ISRECEIVED, COLUMN_LIST_ISERROR}, (String) null, (String[]) null, (String) null, (String) null, (String) null);

        if (query.moveToFirst())
        {
            int columnIndex = query.getColumnIndex(COLUMN_LIST_MESSAGE);
            int columnIndex2 = query.getColumnIndex(COLUMN_LIST_CLIENT);
            int columnIndex3 = query.getColumnIndex(COLUMN_LIST_ISRECEIVED);
            int columnIndex4 = query.getColumnIndex(COLUMN_LIST_ISERROR);

            do
            {
                MessageItem messageItem = new MessageItem();

                messageItem.message = query.getString(columnIndex);
                messageItem.client = query.getString(columnIndex2);
                messageItem.isReceived = query.getInt(columnIndex3) == 0;
                messageItem.isError = query.getInt(columnIndex4) == 0;

                arrayList.add(messageItem);
            }
            while (query.moveToNext());
        }

        query.close();
        readableDatabase.close();
    }

    public void clearList()
    {
        SQLiteDatabase writableDatabase = getWritableDatabase();

        writableDatabase.delete(TABLE_LIST, (String) null, (String[]) null);
        writableDatabase.close();
    }
}
