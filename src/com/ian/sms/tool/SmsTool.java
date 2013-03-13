package com.ian.sms.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.ian.sms.mode.ContactsItem;
import com.ian.sms.mode.MessageItem;
import com.ian.sms.mode.SMS;
import com.ian.sms.mode.SmsGroupInfo;
import com.ian.sms.service.SMSListenerService;
import com.ian.sms.view.TalkActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SmsTool {
	///data/data/com.android.providers.telephony/databases/mmssms.db
	public static final Uri SMS_URI = Uri.parse("content://sms/");
	
	/**
	 * 群发消息
	 * @date 2013-3-7下午4:43:52
	 * @comment 
	 * @param context
	 * @param phones
	 * @param body
	 */
	public static void sendGroupMessage(Context context,Set<String> phones, String body) {
		long threadId = Threads.getOrCreateThreadId(context, phones);
		SmsManager msg = SmsManager.getDefault();
		// 将数据插入数据库
		ContentValues cv = new ContentValues();
		for (String pno : phones) {
			
				//构造监听发送是否成功的PendingIntent
		        Intent sendIntent = new Intent(SMSListenerService.SMSSendCheckBoradcastReceiver.SMS_SEND_RECEIVED_ACTION);
		        //让intent携带此信心的重要内容，方便发送监听器操作
		        sendIntent.putExtra("body", body);
		        sendIntent.putExtra("address", pno);
		        sendIntent.putExtra(Globals.SMS_SEND_TYPE_TAG, Globals.SMS_SEND_TYPE_GROUP);//说明为群发
		        PendingIntent psendIntent = PendingIntent.getBroadcast(context, 0, sendIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		        
		        //构造监听对方接收是否成功的PendingIntent
		        Intent resultIntent = new Intent(SMSListenerService.SMSSendCheckBoradcastReceiver.SMS_SEND_RESULT_RECEIVED_ACTION);
		        //让intent携带此信息的重要内容，方便发送监听器操作
		        resultIntent.putExtra("body", body);
		        resultIntent.putExtra("address", pno);
		        PendingIntent presultIntent = PendingIntent.getBroadcast(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				
				msg.sendTextMessage(pno, null, body, psendIntent, presultIntent);
				cv.put("thread_id", threadId);
				cv.put("date", System.currentTimeMillis());
				cv.put("body", body);
				cv.put("read", 0);
				cv.put("type", 2);//1为收 2为发  
				cv.put("address", pno);
				context.getContentResolver().insert(SMS_URI, cv);	
		}
	}
	/**
	 * 单个消息发送
	 * @date Feb 27, 20132:17:49 PM
	 * @comment 
	 * @param phone
	 * @param message
	 * @param context
	 */
	public static void sendMessage(Context context,String phone, String body,String threadID,int viewIndex) {
		SmsManager msg = SmsManager.getDefault();
		//构造监听发送是否成功的PendingIntent
        Intent sendIntent = new Intent(SMSListenerService.SMSSendCheckBoradcastReceiver.SMS_SEND_RECEIVED_ACTION);
        //让intent携带此信心的重要内容，方便发送监听器操作
        sendIntent.putExtra("body", body);
        sendIntent.putExtra("threadid", threadID);
        sendIntent.putExtra("viewIndex", viewIndex);
        sendIntent.putExtra(Globals.SMS_SEND_TYPE_TAG, Globals.SMS_SEND_TYPE_SINGLE);//说明为单个发送
        PendingIntent psendIntent = PendingIntent.getBroadcast(context, 0, sendIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        //构造监听对方接收是否成功的PendingIntent
        Intent resultIntent = new Intent(SMSListenerService.SMSSendCheckBoradcastReceiver.SMS_SEND_RESULT_RECEIVED_ACTION);
        //让intent携带此信息的重要内容，方便发送监听器操作
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("threadid", threadID);
        resultIntent.putExtra("viewIndex", viewIndex);
        PendingIntent presultIntent = PendingIntent.getBroadcast(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		// 将数据插入数据库
		ContentValues cv = new ContentValues();
		msg.sendTextMessage(phone, null, body, psendIntent, presultIntent);
		cv.put("thread_id", threadID);
		cv.put("date", System.currentTimeMillis());
		cv.put("body", body);
		cv.put("read", 0);
		cv.put("type", 2);//1为收 2为发  
		cv.put("address", phone);
		context.getContentResolver().insert(SMS_URI, cv);
	}
	/**
	 * 发送定时消息
	 * @date 2013-3-5下午4:37:27
	 * @comment 
	 * @param context
	 * @param phone
	 * @param message
	 */
	public static void sendTimingMessage(Context context,Set<String>phones, String body,int id) {
		long threadId = Threads.getOrCreateThreadId(context, phones);
		SmsManager msg = SmsManager.getDefault();
		// 将数据插入数据库
		ContentValues cv = new ContentValues();
		for (String pno : phones) {
			//构造监听发送是否成功的PendingIntent
	        Intent sendIntent = new Intent(SMSListenerService.SMSSendCheckBoradcastReceiver.SMS_SEND_RECEIVED_ACTION);
	        //让intent携带此信心的重要内容，方便发送监听器操作
	        sendIntent.putExtra("body", body);
	        sendIntent.putExtra("address", pno);
	        sendIntent.putExtra("id", id);
	        sendIntent.putExtra(Globals.SMS_SEND_TYPE_TAG, Globals.SMS_SEND_TYPE_TIMING);//说明为群发
	        PendingIntent psendIntent = PendingIntent.getBroadcast(context, 0, sendIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	        
	        //构造监听对方接收是否成功的PendingIntent
	        Intent resultIntent = new Intent(SMSListenerService.SMSSendCheckBoradcastReceiver.SMS_SEND_RESULT_RECEIVED_ACTION);
	        //让intent携带此信息的重要内容，方便发送监听器操作
	        resultIntent.putExtra("body", body);
	        resultIntent.putExtra("address", pno);
	        sendIntent.putExtra("id", id);
	        PendingIntent presultIntent = PendingIntent.getBroadcast(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			msg.sendTextMessage(pno, null, body, psendIntent, presultIntent);
			cv.put("thread_id", threadId);
			cv.put("date", System.currentTimeMillis());
			cv.put("body", body);
			cv.put("read", 0);
			cv.put("type", 2);//1为收 2为发  
			cv.put("address", pno);
			context.getContentResolver().insert(SMS_URI, cv);
		}
	}
	/**
	 * 单个消息发送
	 * @date Feb 27, 20132:17:49 PM
	 * @comment 
	 * @param phone
	 * @param message
	 * @param context
	 */
	public static void sendMessage_AutoReply(String phone, String body, Context context) {
		long threadID = Threads.getOrCreateThreadId(context, phone);
		SmsManager msg = SmsManager.getDefault();
		//构造监听发送是否成功的PendingIntent
        Intent sendIntent = new Intent(SMSListenerService.SMSSendCheckBoradcastReceiver.SMS_SEND_RECEIVED_ACTION);
        //让intent携带此信心的重要内容，方便发送监听器操作
        sendIntent.putExtra("body", body);
        sendIntent.putExtra("threadid", threadID);
        sendIntent.putExtra(Globals.SMS_SEND_TYPE_TAG, Globals.SMS_SEND_TYPE_AUTOREPLY);//说明为自动回复
        PendingIntent psendIntent = PendingIntent.getBroadcast(context, 0, sendIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        //构造监听对方接收是否成功的PendingIntent
        Intent resultIntent = new Intent(SMSListenerService.SMSSendCheckBoradcastReceiver.SMS_SEND_RESULT_RECEIVED_ACTION);
        //让intent携带此信息的重要内容，方便发送监听器操作
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("threadid", threadID);
        PendingIntent presultIntent = PendingIntent.getBroadcast(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		// 将数据插入数据库
		ContentValues cv = new ContentValues();
		msg.sendTextMessage(phone, null, body, psendIntent, presultIntent);
		cv.put("thread_id", threadID);
		cv.put("date", System.currentTimeMillis());
		cv.put("body", body);
		cv.put("read", 0);
		cv.put("type", 2);//1为收 2为发  
		cv.put("address", phone);
		context.getContentResolver().insert(SMS_URI, cv);		
	}
	/**
	 * 查询数据库中消息的最大id
	 * @date 2013-3-2下午10:11:46
	 * @comment 
	 * @param context
	 * @return
	 */
	public static int select_sms_MAXID(Context context){
		 Uri uri = Uri.parse("content://sms");
		 Cursor cursor= context.getContentResolver().query(uri,  new String[]{SMS._ID}, null, null, " _id desc limit 0,1");
		 int id = 0;
		 while (cursor.moveToNext()){
				id = cursor.getInt(0);
		 }
		 cursor.close();
		 return id;
	}
	/**
	 * 
	 * @date 2013-3-8上午12:44:35
	 * @comment 
	 * @param context
	 * @param id
	 * @param values
	 * @return
	 */
	public static void insert_sms_item(Context context,ContentValues values){
		 context.getContentResolver().insert(SMS.CONTENT_URI, values);
		//return context.getContentResolver().update(SMS.CONTENT_URI, values, " _id = '"+id+"'", null);
	}
	/**
	 * 更改短信内容
	 * @date 2013-3-2下午11:57:04
	 * @comment 
	 * @param context
	 * @param id
	 * @param values
	 * @return
	 */
	public static int update_sms_item(Context context,int id,ContentValues values){
		return context.getContentResolver().update(SMS.CONTENT_URI, values, " _id = '"+id+"'", null);
	}
	/**
	 * 更新一个对话的所有消息
	 * @date 2013-3-2下午11:57:14
	 * @comment 
	 * @param context
	 * @param address
	 * @param values
	 * @return
	 */
	public static int update_sys_group(Context context,String threadid,ContentValues values ){
		return context.getContentResolver().update(SMS.CONTENT_URI, values, SMS.THREAD_ID+"= '"+threadid +"' and read=0", null);
	}
	/**
	 * 删除单个消息
	 * @date 2013-3-2下午11:56:33
	 * @comment 
	 * @param context
	 * @param smsID
	 * @return
	 */
	public static int delete_sms_item(Context context,int smsID){
		return context.getContentResolver().delete(SMS.CONTENT_URI, " _id = '"+smsID+"'", null);
			
	}
	/**
	 * 删除一个组的所有消息
	 * @date 2013-3-2下午11:56:13
	 * @comment 
	 * @param context
	 * @param thread_id
	 * @return
	 */
	public static int delete_sys_group(Context context,String thread_id){
		 Uri uri = Uri.parse("content://sms");
		return context.getContentResolver().delete(uri, " thread_id = "+thread_id, null);
	}
	
	
	public static final Uri MMSSMS_FULL_CONVERSATION_URI = Uri.parse("content://mms-sms/conversations");
	public static final Uri CONVERSATION_URI = MMSSMS_FULL_CONVERSATION_URI.buildUpon().appendQueryParameter("simple", "true").build();
	/**
	 * 得到分组的短信 里面应该有 联系人名称，短信对话的数量 最近一条短信的内容，最好还能有相关时间 最新一条短信是否已经阅读
	 * 
	 * @return
	 */
	public static  List<SmsGroupInfo> getThreadSmsData(Context context) {
		Cursor smsThreadCursor = context.getContentResolver().query(
				MMSSMS_FULL_CONVERSATION_URI,
				new String[] { "body", "type", "date", "read", "thread_id","address"}, null, null, "date desc");
		// 存入所有会话
		LinkedHashMap<String, SmsGroupInfo> smsDataMap = new LinkedHashMap<String, SmsGroupInfo>();
		//子线程数量
		int threadCount = smsThreadCursor.getCount();
		// 遍历所有短信
		for (int i = 0; i < threadCount; i++) {
			// 获取短信
			SmsGroupInfo nowSmsInfo = new SmsGroupInfo();
			// 向下移动一条记录
			smsThreadCursor.moveToNext();
			nowSmsInfo.smsBody = smsThreadCursor.getString(smsThreadCursor.getColumnIndex("body")); // 內容
			nowSmsInfo.address = smsThreadCursor.getString(smsThreadCursor.getColumnIndex("address")); //号码
			nowSmsInfo.type = smsThreadCursor.getString(smsThreadCursor.getColumnIndex("type")); //类型
			nowSmsInfo.threadId = smsThreadCursor.getString(smsThreadCursor.getColumnIndex("thread_id")); //线程号
			//getSmsDataListByThreadId(context, nowSmsInfo.smsThreadId);
			//如果联系人中存在此人，就设置此人name
//testColNameAndDatas(smsThreadCursor);
			nowSmsInfo.name = getNameFromPhone(String.valueOf(nowSmsInfo.address),context);
			// 设置最新一条短信的时间
			nowSmsInfo.date = smsThreadCursor.getLong(smsThreadCursor.getColumnIndex("date"));
			
			// 查找有多少未读短信
			{
				Cursor noReadThreadSmsCountCursor = context.getContentResolver().query(SMS.CONTENT_URI,new String[] { "read", "thread_id" },
								"read = 0 and thread_id ="+ nowSmsInfo.threadId, null,null);
				nowSmsInfo.smsNoReadCount = noReadThreadSmsCountCursor.getCount();
				noReadThreadSmsCountCursor.close();
			}
			//查询有多少草稿
			{
				Cursor draftThreadSmsCountCursor = context.getContentResolver().query(SMS.CONTENT_URI,new String[] { "read", "thread_id" },
								"type=3 and thread_id ="+ nowSmsInfo.threadId, null,null);
				nowSmsInfo.smsDraftCount = draftThreadSmsCountCursor.getCount();
				draftThreadSmsCountCursor.close();
			}
			//查询有多少错误短信
			{
				Cursor failedThreadSmsCountCursor = context.getContentResolver().query(SMS.CONTENT_URI,new String[] { "read", "thread_id" },
						"type=5 and thread_id ="+ nowSmsInfo.threadId, null,null);
				nowSmsInfo.smsFailedCount = failedThreadSmsCountCursor.getCount();
				failedThreadSmsCountCursor.close();
			}
			
			// 把以上数据加入smsDataList
			smsDataMap.put(nowSmsInfo.threadId, nowSmsInfo);
		}
		smsThreadCursor.close();// 关闭游标
		// 所有短信会话数量
		Cursor simpleThreadDataCursor = context.getContentResolver().query(
				CONVERSATION_URI, new String[] { "_id", "message_count" },
				null, null, "date desc");
		int simpleThreadCount = simpleThreadDataCursor.getCount();
		for (int i = 0; i < simpleThreadCount; i++) {
			simpleThreadDataCursor.moveToNext();
			String simpleId = simpleThreadDataCursor.getString(simpleThreadDataCursor.getColumnIndex("_id"));
			SmsGroupInfo nowSmsInfo = smsDataMap.get(simpleId);
			if (nowSmsInfo != null) {
				nowSmsInfo.smsAllCount = simpleThreadDataCursor.getString(simpleThreadDataCursor.getColumnIndex("message_count"));
			}
		}// for-end
		simpleThreadDataCursor.close();
		List<SmsGroupInfo> returnSmsListData = new ArrayList<SmsGroupInfo>(smsDataMap.values());
		return returnSmsListData;
	}
	/**
	 * 根据threadid取得草稿
	 * @date 2013-3-8上午12:37:54
	 * @comment 
	 * @param context
	 * @param threadid
	 * @return
	 */
	public static MessageItem getDraftByThreadID(Context context,String threadid){
		MessageItem item = null;
		Cursor cursor = context.getContentResolver().query(SMS.CONTENT_URI,new String[] {SMS._ID,SMS.BODY,SMS.THREAD_ID,SMS.TYPE,SMS.ADDRESS},"type=3 and thread_id ="+ threadid, null,null);
		if(cursor.moveToNext()){
			item = new MessageItem();
			item.setId(cursor.getInt(cursor.getColumnIndex(SMS._ID)));
			item.setBody(cursor.getString(cursor.getColumnIndex(SMS.BODY)));
			item.setThread_id(cursor.getString(cursor.getColumnIndex(SMS.THREAD_ID)));
			item.setType(cursor.getInt(cursor.getColumnIndex(SMS.TYPE)));
			
		}
		cursor.close();
		return item;
	}
	/**
	 * 根据address取得所有信息
	 * @date Feb 27, 201311:11:58 PM
	 * @comment 
	 * @param context
	 * @param address
	 * @return
	 */
	public static  List<MessageItem> getSmsDataListByAddress(Context context,String address) {
		// 按照threadId进行过滤
		Cursor smsDataCursor = context.getContentResolver().query(SMS.CONTENT_URI,null, "address = '" + address+"'", null, "date");
		//testColNameAndDatas(smsDataCursor);// 测试是否拿到数据
		//子线程数量
		int threadCount = smsDataCursor.getCount();
		if(threadCount<1)return null;
		 List<MessageItem>  infos = new ArrayList<MessageItem>();
		// 遍历所有短信
		for (int i = 0; i < threadCount; i++) {
			// 获取短信
			MessageItem info = new MessageItem();
			// 向下移动一条记录
			smsDataCursor.moveToNext();
			info.setBody(smsDataCursor.getString(smsDataCursor.getColumnIndex("body"))); // 內容
			info.setPhone(smsDataCursor.getString(smsDataCursor.getColumnIndex("address"))); //号码
			info.setThread_id(smsDataCursor.getString(smsDataCursor.getColumnIndex("thread_id"))); //线程号
			info.setDate(smsDataCursor.getLong(smsDataCursor.getColumnIndex("date")));//时间
			info.setType(smsDataCursor.getInt(smsDataCursor.getColumnIndex("type")));//类型
			info.setId(smsDataCursor.getInt(smsDataCursor.getColumnIndex("_id")));
			//info.setProtocol(smsDataCursor.getInt(smsDataCursor.getColumnIndex("type")));
			info.setRead(smsDataCursor.getInt(smsDataCursor.getColumnIndex("read")));
			
			infos.add(info);
		}
		
		smsDataCursor.close();
		return infos;
	}

	/**
	 * 根据指定的ThreadID 取得所有的相关 // 发出和收到的短信
	 * 
	 * @param queryThreadId
	 * @return
	 */
	public static  List<MessageItem> getSmsDataListByThreadId(Context context,String queryThreadId) {
		// 按照threadId进行过滤
		Cursor smsDataCursor = context.getContentResolver().query(SMS.CONTENT_URI,null, "(type =1 or type=2 or type=5) and thread_id = " + queryThreadId, null, "date");
		//子线程数量
		int threadCount = smsDataCursor.getCount();
		if(threadCount<1)return null;
		 List<MessageItem>  infos = new ArrayList<MessageItem>();
		// 遍历所有短信
		for (int i = 0; i < threadCount; i++) {
			// 获取短信
			MessageItem info = new MessageItem();
			// 向下移动一条记录
			smsDataCursor.moveToNext();
			info.setBody(smsDataCursor.getString(smsDataCursor.getColumnIndex("body"))); // 內容
			info.setPhone(smsDataCursor.getString(smsDataCursor.getColumnIndex("address"))); //号码
			info.setThread_id(smsDataCursor.getString(smsDataCursor.getColumnIndex("thread_id"))); //线程号
			info.setDate(smsDataCursor.getLong(smsDataCursor.getColumnIndex("date")));//时间
			info.setType(smsDataCursor.getInt(smsDataCursor.getColumnIndex("type")));//类型
			info.setId(smsDataCursor.getInt(smsDataCursor.getColumnIndex("_id")));
			//info.setProtocol(smsDataCursor.getInt(smsDataCursor.getColumnIndex("type")));
			info.setRead(smsDataCursor.getInt(smsDataCursor.getColumnIndex("read")));
			infos.add(info);
		}
		
		smsDataCursor.close();
		return infos;
	}
	/**
	 * 取得联系人名字
	 * 
	 * @param smsThreadCursor
	 */
	public static void testColNameAndDatas(Cursor smsThreadCursor) {
		//短信数量
		int crusorCount = smsThreadCursor.getCount();
		//打印短信相关数据列
		String[] allColName = smsThreadCursor.getColumnNames();
		for (int i = 0; i < crusorCount; i++) {
			smsThreadCursor.moveToNext();
			for (int j = 0; j < allColName.length; j++) {
				Log.i("smsdata", "列名:" + allColName[j] + "数据:"+ smsThreadCursor.getString(j));
			}
			Log.i("smsdata", "`````````````````````");
		}
	}
	public static String getNameListFromThreadID(String threadid,Context context) {
		return "暂未实现";
	}

	/**
	 * 根据电话号码取得联系人名字
	 * @date 2013-3-1下午1:49:04
	 * @comment 
	 * @param number
	 * @param context
	 * @return 如果联系人存在就返回名字，如果不存在就返回电话号码
	 */
	public static String getNameFromPhone(String number,Context context) {
		if(number==null)return null;
		String address = number.replaceAll(" ", "");
        String name = null;
        String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER };
        String where = ContactsContract.CommonDataKinds.Phone.NUMBER + " = '"+ address + "' or "+ ContactsContract.CommonDataKinds.Phone.NUMBER + " like '+%"+ number + "'"; // WHERE clause.
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, // Which columns to return.
                where,//where
                null, // WHERE clause value substitution
                null); // Sort order.

        if (cursor == null) {
            return null;
        }
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            name = cursor.getString(nameFieldColumnIndex);
        }
        cursor.close();
        return name;
        
    }
}
