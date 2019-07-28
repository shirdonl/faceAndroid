package com.wis.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wis.bean.Person;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ybbz on 16/8/28.
 */
public class DBManager {

    private DBHelper helper;
    private SQLiteDatabase db;

    private final static String DBMANAGER = "DBManager";

    public DBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add persons
     *
     * @param persons
     */
    public void addPersons(List<Person> persons) {
        db.beginTransaction();  //开始事务
        try {
            for (Person person : persons) {
                db.execSQL("INSERT INTO person VALUES(null, ?, ?, ?)", new Object[]{person.name, person.image, person.feature});
            }
            Log.i(DBMANAGER, "批量添加多条记录");
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    /**
     * add person
     *
     * @param person
     */
    public void addPerson(Person person) {
        db.execSQL("INSERT INTO person VALUES(null, ?, ?, ?)", new Object[]{person.name, person.image, person.feature});
        Log.i(DBMANAGER, "添加一条记录");
    }

    /**
     * update person's name
     *
     * @param person
     */
    public void updateName(Person person) {
        ContentValues cv = new ContentValues();
        cv.put("name", person.name);
        db.update("person", cv, "_id=?", new String[]{String.valueOf(person._id)});
    }

    /**
     * delete person
     *
     * @param person
     */
    public void deletePerson(Person person) {
        db.delete("person", "_id=?", new String[]{String.valueOf(person._id)});
        //db.execSQL("DELETE FROM person where _id=" + person._id);
        //db.delete("person", "_id" + "=" +person._id, null);
    }

    /**
     * query all persons, return list
     *
     * @return List<Person>
     */
    public List<Person> query() {
        ArrayList<Person> persons = new ArrayList<Person>();
        Cursor c = queryTheCursor();
        while (c.moveToNext()) {
            Person person = new Person();
            person._id = c.getInt(c.getColumnIndex("_id"));
            person.name = c.getString(c.getColumnIndex("name"));
            person.image = c.getBlob(c.getColumnIndex("image"));
            person.feature = c.getString(c.getColumnIndex("feature"));
            persons.add(person);
        }
        c.close();
        return persons;
    }

    /**
     * query all persons, return cursor
     *
     * @return Cursor
     */
    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM person", null);
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }

}
