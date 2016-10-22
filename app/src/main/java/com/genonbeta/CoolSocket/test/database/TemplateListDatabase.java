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
    public static final String PREFIX_SHORTCUT = "@";

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
        if (!isAddable(str))
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

        ArrayList<String> lateList = new ArrayList<>();

        if (query.moveToFirst())
        {
            int columnIndex = query.getColumnIndex(COLUMN_LIST_MESSAGE);

            do
            {
                String currentString = query.getString(columnIndex);

                if (isShortcut(currentString))
                    lateList.add(currentString);
                else
                    arrayList.add(currentString);
            }
            while (query.moveToNext());
        }

        arrayList.addAll(lateList);
        lateList.clear();

        query.close();
        readableDatabase.close();
    }

    public void delete(String str)
    {
        SQLiteDatabase writableDatabase = getWritableDatabase();

        writableDatabase.delete(TABLE_LIST, COLUMN_LIST_MESSAGE + " LIKE ?", new String[]{str});
        writableDatabase.close();
    }

    public void edit(String str, String changeAs)
    {
        SQLiteDatabase writableDatabase = getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_LIST_MESSAGE, changeAs);

        writableDatabase.update(TABLE_LIST, contentValues, COLUMN_LIST_MESSAGE + " LIKE ?", new String[]{str});
        writableDatabase.close();
    }

    public boolean isAddable(String str)
    {
        if (isExist(str))
            return false;

        if (isShortcut(str))
        {
            try
            {
                if (getShortcut(str) != null)
                    return false;
            } catch (Exception e)
            {
                return false;
            }
        }

        return true;
    }

    public boolean isExist(String str)
    {
        SQLiteDatabase readableDatabase = getReadableDatabase();

        Cursor query = readableDatabase.query(TABLE_LIST, new String[]{COLUMN_LIST_MESSAGE}, COLUMN_LIST_MESSAGE + " LIKE ?", new String[]{str}, (String) null, (String) null, (String) null);
        boolean moveToFirst = query.moveToFirst();

        query.close();
        readableDatabase.close();

        return moveToFirst;
    }

    public boolean isShortcut(String text)
    {
        return text.startsWith(PREFIX_SHORTCUT);
    }

    public String getShortcut(String text) throws Exception
    {
        String outString = null;
        String addition = null;

        int seperatorPoint = text.indexOf(":");

        if (seperatorPoint != -1)
        {
            addition = text.substring(seperatorPoint + 1, text.length());
            text = text.substring(0, seperatorPoint);
        }

        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor query = readableDatabase.query(TABLE_LIST, new String[]{COLUMN_LIST_MESSAGE}, COLUMN_LIST_MESSAGE + " LIKE ?", new String[]{text + ":%"}, (String) null, (String) null, (String) null);

        if (query.moveToFirst())
        {
            int columnIndex = query.getColumnIndex(COLUMN_LIST_MESSAGE);
            outString = query.getString(columnIndex).substring((PREFIX_SHORTCUT + text + ":").length() - 1);
            outString += addition;
        }
        else
            throw new Exception("Shortcut for " + text + " is not defined in templates");

        query.close();
        readableDatabase.close();

        return outString;
    }
}
