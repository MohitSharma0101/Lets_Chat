package com.mohitsharma.letschat.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "LetsChat.db";
    public static final String TABLE_NAME = "ProfilePhoto";
    public static final String _ID = "id";
    public static final String DESCRIPTION = "description";
    public static final String Date = "date";

    SQLiteDatabase database=this.getWritableDatabase();



    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create table " + TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT, description TEXT, date TEXT)" );

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public long insertData( String description ,String date){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put(DESCRIPTION, description);
        contentValues.put(Date, date);

        long id = db.insert(TABLE_NAME,null,contentValues);
        return id;

    }

    public Cursor getAllData(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cur = db.rawQuery("select * from " + TABLE_NAME , null);
        return  cur;
    }

    public int deleteData(String ID){
        SQLiteDatabase db=this.getWritableDatabase();

        return db.delete(TABLE_NAME, "ID = ?" ,new String[] {ID}) ;
    }
    public void deleteAll(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);

    }

    public void updateData(String ID, String description , String date){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(_ID, ID);
        cv.put(DESCRIPTION,description);
        cv.put(Date,date);
        db.update(TABLE_NAME,cv,"ID = ?" , new String[] {ID});
    }



    public Cursor getData(long rowId) throws SQLException {
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor mCursor =
                db.rawQuery("select * from NOTEBOOK WHERE id =" + rowId + ";", null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;


    }
}
