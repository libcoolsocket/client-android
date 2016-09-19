package com.genonbeta.CoolSocket.test.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class TemplateListDatabase extends SQLiteOpenHelper
{
    public static final String COLUMN_LIST_MESSAGE = "message";
    public static final String DATABASE_NAME = "TemplateList";
    public static final String TABLE_LIST = "List";
    private Context mContext;

    @Override
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2)
    {
    }

    public TemplateListDatabase(Context context)
    {
        super(context, DATABASE_NAME, (CursorFactory) null, 1);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL("CREATE TABLE `" + TABLE_LIST + "` (`" + COLUMN_LIST_MESSAGE + "` text NOT NULL)");
    }

    public boolean add(String str)
    {
        if (isExist(str))
            return false;

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_LIST_MESSAGE, str);

        SQLiteDatabase writableDatabase = getWritableDatabase();

        writableDatabase.insert(TABLE_LIST, (String) null, contentValues);
        writableDatabase.close();

        return true;
    }

    public void getList(ArrayList<String> arrayList)
    {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor query = readableDatabase.query(TABLE_LIST, new String[]{COLUMN_LIST_MESSAGE}, (String) null, (String[]) null, (String) null, (String) null, (String) null);

        if (query.moveToFirst())
        {
            int columnIndex = query.getColumnIndex(COLUMN_LIST_MESSAGE);

            do
            {
                arrayList.add(query.getString(columnIndex));
            }
            while (query.moveToNext());
        }

        query.close();
        readableDatabase.close();
    }

    public void delete(String str)
    {
        SQLiteDatabase writableDatabase = getWritableDatabase();

        writableDatabase.delete(TABLE_LIST, COLUMN_LIST_MESSAGE + " LIKE ?", new String[]{str});
        writableDatabase.close();
    }

    public void edit(String str, String str2)
    {
        SQLiteDatabase writableDatabase = getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_LIST_MESSAGE, str2);

        writableDatabase.update(TABLE_LIST, contentValues, COLUMN_LIST_MESSAGE + " LIKE ?", new String[]{str});
        writableDatabase.close();
    }

    public boolean isExist(String str)
    {
        SQLiteDatabase writableDatabase = getWritableDatabase();

        Cursor query = writableDatabase.query(TABLE_LIST, new String[]{COLUMN_LIST_MESSAGE}, COLUMN_LIST_MESSAGE + " LIKE ?", new String[]{str}, (String) null, (String) null, (String) null);
        boolean moveToFirst = query.moveToFirst();

        query.close();
        writableDatabase.close();

        return moveToFirst;
    }
}
