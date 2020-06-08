package com.smarthome.demo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.smarthome.demo.model.UserModel;

public class SqliteDao extends SQLiteOpenHelper {

    public SqliteDao(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table user(phone nvarchar(11) primary key,password nvarchar(20) not null)";
        db.execSQL(sql);
        //初始化
        String sql2 = "insert into user (phone,password) values ('16073001111','1234')";
        db.execSQL(sql2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //查询登录
    public boolean show(UserModel user) {
        boolean k = false;
        //得到数据库对象

        SQLiteDatabase sqldb = getReadableDatabase();
        //创建游标
        Cursor mCursor = sqldb.query("user", new String[]{"phone", "password"}, "phone=?", new String[]{user.getPhone()}, null, null,
                null);
        //游标置顶
        if (mCursor.moveToPosition(0) == true) {
            mCursor.moveToFirst();
            //遍历
            do {
                String password = mCursor.getString(mCursor.getColumnIndex("password"));
                if (user.getPassword().equals(password)) {
                    k = true;
                    break;
                }
            } while (mCursor.moveToNext());
        }
        sqldb.close();
        return k;
    }

    //注册
    public boolean insert(UserModel user) {
        boolean k = false;
        //读写模式
        SQLiteDatabase sqldb = getWritableDatabase();
        //ContentValues存简单数据类型
        ContentValues values = new ContentValues();
        values.put("phone", user.getPhone());
        values.put("password", user.getPassword());
        //执行插入操作
        long s = sqldb.insert("user", null, values);
        if (s > 0)
            k = true;
        sqldb.close();
        return k;
    }

    //修改密码
    public boolean modify(UserModel user) {
        boolean k = false;
        SQLiteDatabase sqldb = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("password", user.getPassword());

        int i = sqldb.update("user", values, "phone=?", new String[]{user.getPhone()});
        if (i > 0) k = true;
        sqldb.close();
        return k;
    }
}
