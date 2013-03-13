package com.ian.sms.db;

import java.util.ArrayList;
import java.util.Random;

import com.ian.sms.mode.AutoReplyItem;
import com.ian.sms.mode.TimingSendMessageItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 数据库操作类
 * @author ives
 * @date 2013-3-4下午2:19:44
 * @version 1.0
 * @comment
 */
public class DatabaseHelper extends SQLiteOpenHelper{
	public static final String DB_NAME = "ian_sms";
	public static final String TABLE_NAME_TIMING = "timing";
	public static final String TABLE_NAME_REPLY = "reply";
	//默认数据库版本
	private static final int VERSION = 1;
	private static DatabaseHelper databaseHelper;//单例

	/**
	 * 获取数据库操作对象
	 * @date 2013-3-4下午1:42:25
	 * @comment 
	 * @param context
	 * @param name
	 * @return
	 */
	public static SQLiteDatabase getSQLiteDatabase(Context context){
		if(databaseHelper==null)databaseHelper = new DatabaseHelper(context, DB_NAME);
		return databaseHelper.getWritableDatabase();
	}
	/**
	 * 插入
	 * @date 2013-3-4下午2:01:18
	 * @comment 
	 * @param context
	 * @param values
	 * @param tableName
	 * @return
	 */
	public static long insert(Context context,String tableName,ContentValues values){
		return getSQLiteDatabase(context).insert(tableName, null, values);
	}
	/**
	 * 更新
	 * @date 2013-3-4下午2:01:23
	 * @comment 
	 * @param context
	 * @param tableName
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public static long update(Context context,String tableName,ContentValues values,String whereClause, String[] whereArgs){
		return getSQLiteDatabase(context).update(tableName, values, whereClause, whereArgs);
	}
	/**
	 * 删除记录
	 * @date 2013-3-4下午5:06:52
	 * @comment 
	 * @param context
	 * @param table
	 * @param where
	 * @return
	 */
	public static long delete(Context context,String table,String where){
		return getSQLiteDatabase(context).delete(table, where, null);
	}
	
	//在SQLiteOepnHelper的子类当中，必须有该构造函数
	public DatabaseHelper(Context context, String name, CursorFactory factory,int version) {
		//必须通过super调用父类当中的构造函数
		super(context, name, factory, version);
	}
	public DatabaseHelper(Context context,String name){
		this(context,name,VERSION);
	}
	public DatabaseHelper(Context context,String name,int version){
		this(context, name,null,version);
	}

	//该函数是在第一次创建数据库的时候执行,实际上是在第一次得到SQLiteDatabse对象的时候，才会调用这个方法
	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建timing表
		db.execSQL("create table timing(id INTEGER PRIMARY KEY,body TEXT,date INTEGER,address TEXT,repeat INTEGER,status INTEGER,nextdate INTEGER,weekdaysign INTEGER)");
		db.execSQL(AutoReplyItem.createTableStr);
	}
	//update回调函数
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		System.out.println("update a Database");
	}
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
	/**
	 * 取得指定条件下的所有auto reply消息
	 * @date 2013-3-5下午10:37:05
	 * @comment 
	 * @param context
	 * @param where
	 * @param orderBy
	 * @return
	 */
	public static ArrayList<AutoReplyItem> getAllReplyMessage(Context context,String where,String orderBy){
		ArrayList<AutoReplyItem> result = new ArrayList<AutoReplyItem>();
		SQLiteDatabase db = getSQLiteDatabase(context);
		Cursor cursor = db.query(TABLE_NAME_REPLY, AutoReplyItem.ALLCLUMN, where, null, null, null, orderBy);
		while(cursor.moveToNext()){
			AutoReplyItem item = new AutoReplyItem();
			int id = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.ID));
			String body = cursor.getString(cursor.getColumnIndex(AutoReplyItem.BODY));
			int isAcceptSMS = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.ISACCEPTSMS));
			int isAllContacts = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.ISALLCONTACTS));
			int isCustomTime = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.ISCUSTOMTIME));
			int isHangUpCall = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.ISHANGUPCALL));
			int isIncludeStranger = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.ISINCLUDESTRANGER));
			int isNoAnswerCall = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.ISNOANSWERCALL));
			int isOnlyReply = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.ISONLYREPLY));
			int isStart = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.ISSTART));
			int day_startTime = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.DAY_STARTTIME));
			int day_stopTime = cursor.getInt(cursor.getColumnIndex(AutoReplyItem.DAY_STOPTIME));
			String contacts = cursor.getString(cursor.getColumnIndex(AutoReplyItem.CONTACTS));
			
			item.setId(id);
			item.setBody(body);
			item.setContacts(contacts);
			item.setAcceptSMS(isAcceptSMS==1);
			item.setAllContacts(isAllContacts==1);
			item.setCustomTime(isCustomTime==1);
			item.setHangUpCall(isHangUpCall==1);
			item.setIncludeStranger(isIncludeStranger==1);
			item.setNoAnswerCall(isNoAnswerCall==1);
			item.setOnlyReply(isOnlyReply==1);
			item.setStart(isStart==1);
			item.setDay_startTime(day_startTime);
			item.setDay_stopTime(day_stopTime);
			
			result.add(item);
		}
		cursor.close();
		return result;
	}
	/**
	 * 取得指定条件下的所有定时消息
	 * @date 2013-3-4下午3:38:57
	 * @comment 
	 * @param context
	 * @param where
	 * @param orderBy
	 * @return
	 */
	public static ArrayList<TimingSendMessageItem> getAllTimingMessage(Context context,String where,String orderBy){
		ArrayList<TimingSendMessageItem> result = new ArrayList<TimingSendMessageItem>();
		SQLiteDatabase db = getSQLiteDatabase(context);
		Cursor cursor = db.query(TABLE_NAME_TIMING, TimingSendMessageItem.ALLCLUMN, where, null, null, null, orderBy);
		while(cursor.moveToNext()){
			int id = cursor.getInt(cursor.getColumnIndex(TimingSendMessageItem.TIMING_ID));
			String body = cursor.getString(cursor.getColumnIndex(TimingSendMessageItem.TIMING_BODY));
			String address = cursor.getString(cursor.getColumnIndex(TimingSendMessageItem.TIMING_ADDRESS));
			String date = cursor.getString(cursor.getColumnIndex(TimingSendMessageItem.TIMING_DATE));
			int repeat = cursor.getInt(cursor.getColumnIndex(TimingSendMessageItem.TIMING_REPEAT));
			int status = cursor.getInt(cursor.getColumnIndex(TimingSendMessageItem.TIMING_STATUS));
			String nextdate = cursor.getString(cursor.getColumnIndex(TimingSendMessageItem.TIMING_NEXTDATE));
			int weekDaySign = cursor.getInt(cursor.getColumnIndex(TimingSendMessageItem.TIMING_WEEKDAYSIGN));
			
			TimingSendMessageItem item = new TimingSendMessageItem();
			item.setId(id);
			item.setBody(body);
			item.setAddress(address);
			item.setDate(date);
			item.setRepeat(repeat);
			item.setStatus(status);
			item.setNextDate(nextdate);
			item.setWeekDaySign(weekDaySign);
			result.add(item);
		}
		cursor.close();
		return result;
	}
	
	/**
	 * 加入测试数据
	 * @date 2013-3-4下午3:27:52
	 * @comment 
	 * @param context
	 */
	public static void addtempdata(Context context){
		for (int i = 0; i < 10; i++) {
			ContentValues values = new ContentValues();
			values.put(TimingSendMessageItem.TIMING_DATE, String.valueOf(System.currentTimeMillis()+10000*i));
			values.put(TimingSendMessageItem.TIMING_BODY, "第"+i+"条消息");
			values.put(TimingSendMessageItem.TIMING_ADDRESS, "1001022,1008611");
			values.put(TimingSendMessageItem.TIMING_REPEAT, TimingSendMessageItem.TIMING_REPEAT_ONE);
			values.put(TimingSendMessageItem.TIMING_STATUS, new Random().nextInt(3));
			values.put(TimingSendMessageItem.TIMING_NEXTDATE, String.valueOf(System.currentTimeMillis()+10000*i));
			values.put(TimingSendMessageItem.TIMING_WEEKDAYSIGN, new Random().nextInt(127));
			insert(context,TABLE_NAME_TIMING, values );
		}
	}

}
