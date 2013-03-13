package com.ian.sms.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.widget.Toast;

import com.ian.sms.db.DatabaseHelper;
import com.ian.sms.mode.AutoReplyItem;
import com.ian.sms.mode.MessageItem;
import com.ian.sms.mode.SMS;
import com.ian.sms.mode.TimingSendMessageItem;
import com.ian.sms.tool.ActivityTool;
import com.ian.sms.tool.DateTool;
import com.ian.sms.tool.Globals;
import com.ian.sms.tool.SmsTool;
import com.ian.sms.tool.StringTool;
import com.ian.sms.view.FSendActivity;
import com.ian.sms.view.MainActivity;
import com.ian.sms.view.TalkActivity;
import com.ian.sms.view.TimingSendManagerActivity;

/**
 * 监听短信
 * 
 * @author ives
 * @date Feb 28, 20136:43:26 PM
 * @version 1.0
 * @comment
 */
public class SMSListenerService extends Service {
	public static String SERVICE_SMSListener_TAG = "com.ian.sms.service.smslistener.service";
	public static final String EXECUTING_RECEIVED_ACTION = "com.ian.sms.receive.executing.action";
	public static final String EXECUTE_TYPE_NAME = "EXECUTE_TYPE_NAME"; 
	
	public static final int EXECUTE_TYPE_AUTOREPLY = 1;
	public static final int EXECUTE_TYPE_TIMING = 2;
	public static final int EXECUTE_TYPE_GROUPSEND = 3;
	
	public static final String EXECUTE_TIMING_ACTION_NAME = "action_name";
	public static final int EXECUTE_TIMING_ACTION_REFRESHDB = 1;//刷新数据库
	
	public static  long READ_DB_INTERVAL =1000*60*60;//去数据库读取消息的间隔时间
	LinkedList<TimingSendMessageItem> sendItems;//指定时间内即将要发送的消息
	
	
	private SMSObserver smsObserver;
	SMSSendCheckBoradcastReceiver sMSSendCheckBoradcastReceiver;
	ExecutingBroadCast executingBroadCast;
	
	Timer timer;
	Handler handler;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		this.getContentResolver().unregisterContentObserver(smsObserver);
		unregisterReceiver(sMSSendCheckBoradcastReceiver);
		unregisterReceiver(executingBroadCast);
		unregisterReceiver(phoneListenerReceiver);
		unregisterReceiver(alarmReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	
	private void init(){
		addSMSObserver();
		
		
		alarmReceiver = new AlarmReceiver();
		IntentFilter  alarmFilter = new IntentFilter(AlarmReceiver.ALARM_RECEIVER_NAME);
		registerReceiver(alarmReceiver, alarmFilter);
		
		IntentFilter  filter = new IntentFilter();
		filter.addAction(SMSSendCheckBoradcastReceiver.SMS_SEND_RECEIVED_ACTION);
		filter.addAction(SMSSendCheckBoradcastReceiver.SMS_SEND_RESULT_RECEIVED_ACTION);
		sMSSendCheckBoradcastReceiver = new SMSSendCheckBoradcastReceiver();
		registerReceiver(sMSSendCheckBoradcastReceiver, filter);
		
		IntentFilter eFilter = new IntentFilter(EXECUTING_RECEIVED_ACTION);
		executingBroadCast = new ExecutingBroadCast();
		registerReceiver(executingBroadCast, eFilter);
		
		phoneListenerReceiver = new PhoneListenerReceiver();
		IntentFilter pFilter = new IntentFilter();
		pFilter.addAction("android.intent.action.PHONE_STATE");
		pFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		registerReceiver(phoneListenerReceiver, pFilter);
		
		Globals.MAX_ID = SmsTool.select_sms_MAXID(SMSListenerService.this);
		timer = new Timer();
		handler = new Handler();
		handler.obtainMessage();
		startDBReader();
	}
	/**
	 * 
	 * @author ives
	 * @date 2013-3-1下午12:13:16
	 * @version 1.0
	 * @comment
	 */
	class SMSHandler extends Handler{
		public static final String TAG = "SMSHandler";
		private Context mContext;
		public SMSHandler(Context context){
			super();
			this.mContext = context;
		}
		@Override
		public void handleMessage(Message message){
			MessageItem item = (MessageItem) message.obj;
			if(item==null)return;
			if(item.getType()==SMS.SMS_TYPE_IN){
				//检查自动回复
				if(checkAutoReplyForReceiveSMS(item.getPhone()))return ;
				if(ActivityTool.isTopActivy(SMSListenerService.this,"com.ian.sms.view.TalkActivity")){//如果对话框处于可见状态
					if(TalkActivity.thread_id.equals(item.getThread_id())){//如果对话框的address和当前消息address相同，则直接添加
						Intent intent = new Intent(TalkActivity.RECEIVE_TalkMessageItemAddReceiver_ACTION);
						intent.putExtra("type", "in");
						Bundle bundle = new Bundle();
						bundle.putSerializable("messageItem", item);
						intent.putExtras(bundle);
						sendBroadcast(intent);
						return;
					}
				}
				showReceiveSMSDialog(item);
				
			}
		}
	}
	private boolean checkAutoReplyForReceiveSMS(String number){
		//确定是否认识联系人
		boolean result = false;
		StringBuilder where = new StringBuilder();
		if(SmsTool.getNameFromPhone(number, SMSListenerService.this)==null){//陌生人
			where.append(AutoReplyItem.ISSTART+"=1 and "+AutoReplyItem.ISACCEPTSMS+"=1 and "+AutoReplyItem.ISINCLUDESTRANGER+"=1");
		}else{//联系人
			where.append(AutoReplyItem.ISSTART+"=1 and "+AutoReplyItem.ISACCEPTSMS+"=1 and("+AutoReplyItem.ISALLCONTACTS+"=1 or "+AutoReplyItem.CONTACTS+" like '%"+number+"%')");
		}
		ArrayList<AutoReplyItem>reslt = DatabaseHelper.getAllReplyMessage(this, where.toString(), null);
		if(reslt.size()>0)result = true;
		for (int i = 0; i < reslt.size(); i++) {
			SmsTool.sendMessage_AutoReply( number, reslt.get(i).getBody(), this);
		}
		return result;
	}
	/**
	 * 显示消息提示框
	 * @date 2013-3-7下午6:06:12
	 * @comment 
	 * @param item
	 */
	private void showReceiveSMSDialog(final MessageItem item) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_email);
		builder.setTitle("消息："+item.getPhone());
		builder.setMessage(item.getBody());
		builder.setPositiveButton("查看", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(SMSListenerService.this,MainActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				i.putExtra("tab", MainActivity.TAB_TAG_SMS);
				i.putExtra("threadid",item.getThread_id());
				startActivity(i);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("删除", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SmsTool.delete_sms_item(SMSListenerService.this, item.getId());
				Globals.MAX_ID = SmsTool.select_sms_MAXID(SMSListenerService.this);
				dialog.dismiss();
			}
		});
		builder.setNeutralButton("忽略", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            	dialog.dismiss();
            }
		});
		AlertDialog ad = builder.create();
		// ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
		// //系统中关机对话框就是这个属性
		ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		ad.setCanceledOnTouchOutside(false); // 点击外面区域不会让dialog消失
		ad.show();
	}
	/**
	 * 添加短信接收观察者
	 * @date 2013-3-1下午12:03:35
	 * @comment
	 */
	public void addSMSObserver(){
		ContentResolver resolver = getContentResolver();
		Handler handler = new SMSHandler(this);
		smsObserver = new SMSObserver(resolver, handler);
		resolver.registerContentObserver(SMS.CONTENT_URI, true, smsObserver);

	}
	/**
	 * 短信监听器(观察者)
	 * @author ives
	 * @date 2013-3-1上午11:55:22
	 * @version 1.0
	 * @comment
	 */
	class SMSObserver extends ContentObserver{
		public static final String TAG = "SMSObserver";
		//要查询的字段
		private  final String[] PROJECTION = new String[]{
				SMS._ID,// 0
				SMS.TYPE,// 1
				SMS.ADDRESS,// 2
				SMS.BODY,// 3
				SMS.DATE,// 4
				SMS.THREAD_ID,// 5
				SMS.READ,// 6
				SMS.PROTOCOL // 7
		};
		//查询条件
		private static final String SELECTION =SMS._ID + " > %s" +" and "+SMS.READ +" = "+SMS.SMS_READ_NO+" and (" + SMS.TYPE + " = " + SMS.MESSAGE_TYPE_INBOX +" or " + SMS.TYPE + " = " + SMS.MESSAGE_TYPE_SENT + ")";
		private static final int COLUMN_INDEX_ID = 0;
		private static final int COLUMN_INDEX_TYPE = 1;
		private static final int COLUMN_INDEX_PHONE = 2;
		private static final int COLUMN_INDEX_BODY = 3;
		private static final int COLUMN_INDEX_DATE = 4;
		private static final int COLUMN_INDEX_THREAD_ID = 5;
		private static final int COLUMN_INDEX_READ = 6;
		private static final int COLUMN_INDEX_PROTOCOL = 7;
		private static final int MAX_NUMS = 10;//最大行数
		
		private ContentResolver mResolver;
		private Handler mHandler;//外部处理器
		
		public SMSObserver(ContentResolver contentResolver, Handler handler){
			super(handler);
			this.mResolver = contentResolver;
			this.mHandler = handler;
		}

		@Override
		public void onChange(boolean selfChange){
			super.onChange(selfChange);
			String ss = String.format(SELECTION, Globals.MAX_ID);
			Cursor cursor = mResolver.query(SMS.CONTENT_URI, PROJECTION,ss, null,SMS.DATE+" desc limit 0,1" );
			int id, type, protocol,read;
			String phone, body,thread_id;
			long date;
			
			Message message;
			MessageItem item;
			int iter = 0;
			while (cursor.moveToNext()){//取最后一条用于显示，其余的默认处理
				id = cursor.getInt(COLUMN_INDEX_ID);
				type = cursor.getInt(COLUMN_INDEX_TYPE);
				phone = cursor.getString(COLUMN_INDEX_PHONE);
				body = cursor.getString(COLUMN_INDEX_BODY);
				protocol = cursor.getInt(COLUMN_INDEX_PROTOCOL);
				date = cursor.getLong(COLUMN_INDEX_DATE);
				read = cursor.getInt(COLUMN_INDEX_READ);
				thread_id = cursor.getString(COLUMN_INDEX_THREAD_ID);
				Globals.MAX_ID = id;
				
				if (protocol == SMS.PROTOCOL_SMS && body != null && type==SMS.SMS_TYPE_IN){
					item = new MessageItem();
					item.setId(id);
					item.setType(type);
					item.setPhone(phone);
					item.setBody(body);
					item.setProtocol(protocol);
					item.setDate(date);
					item.setRead(read);
					item.setThread_id(thread_id);
					message = new Message();
					message.obj = item;
					mHandler.sendMessage(message);
				}else{
					if (id > Globals.MAX_ID)
						Globals.MAX_ID = id;
				}
				if (iter > MAX_NUMS)
					break;
				iter++;
			}
		}
	}
	
	/**
	 * 发送消息的监听广播
	 * @author ives
	 * @date 2013-2-28下午10:43:48
	 * @version 1.0
	 * @comment
	 */
	public class SMSSendCheckBoradcastReceiver extends BroadcastReceiver{
		public static final String SMS_SEND_RECEIVED_ACTION = "com.ian.sms.receive.smssend.action";
		public static final String SMS_SEND_RESULT_RECEIVED_ACTION = "com.ian.sms.receive.smssend.result.action";
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if(action.equals(SMS_SEND_RECEIVED_ACTION)){//监听消息是否发出
				int send_type = intent.getIntExtra(Globals.SMS_SEND_TYPE_TAG, 0);//取得发送类型
				int result = 0;//发送结果
	            switch (getResultCode()) {
	            case Activity.RESULT_OK://成功发送
	            	result = Activity.RESULT_OK;
	                break;
	            case SmsManager.RESULT_ERROR_GENERIC_FAILURE://发送错误(通用故障原因)
	            	result = SmsManager.RESULT_ERROR_GENERIC_FAILURE;
	                break;
	            case SmsManager.RESULT_ERROR_NO_SERVICE://服务不可用
	            	result = SmsManager.RESULT_ERROR_NO_SERVICE;
	                break;
	            case SmsManager.RESULT_ERROR_NULL_PDU://没有提供pdu
	            	result = SmsManager.RESULT_ERROR_NULL_PDU;
	                break;
	            case SmsManager.RESULT_ERROR_RADIO_OFF://广播显示关闭
	            	result = SmsManager.RESULT_ERROR_RADIO_OFF;
	                break;
	            }
	            /*根据不同的情况调用不同的解决办法*/
	            if(send_type==Globals.SMS_SEND_TYPE_SINGLE){
            		disposeSinleMessage(context,intent,result);
            	}if(send_type==Globals.SMS_SEND_TYPE_AUTOREPLY){
            		disposeAutoReplyMessage(context,intent,result);
            	}else if(send_type==Globals.SMS_SEND_TYPE_GROUP){
            		disposeGroupMessage(context, intent,result);
            	}else if(send_type==Globals.SMS_SEND_TYPE_TIMING){
            		disposeTimingMessage(context, intent,result);
            	}
			}else if(action.equals(SMS_SEND_RESULT_RECEIVED_ACTION)){//监听消息是否送到
				switch (getResultCode()) {
	            case Activity.RESULT_OK://成功发送
	            	
				}
			}
		}
		/**
		 * 处理自动回复消息
		 * @date 2013-3-7下午6:48:20
		 * @comment 
		 * @param context
		 * @param intent
		 * @param result
		 */
		private void disposeAutoReplyMessage(Context context,Intent intent,int result){
			if(result==Activity.RESULT_OK){
				String address = intent.getStringExtra("address");
				String body = intent.getStringExtra("body");
				//将成功的消息插入数据库
	            //SmsTool.insert_send_Sms(context, address, body);
	            Toast.makeText(SMSListenerService.this, "已经自动回复消息", Toast.LENGTH_SHORT).show();
			}else{//发送失败的消息
				Toast.makeText(SMSListenerService.this, "自动回复失败", Toast.LENGTH_SHORT).show();
			}
			
		}
		/**
		 * 处理监听到单个消息发出
		 * @date 2013-3-3下午5:02:04
		 * @comment 
		 * @param intent
		 */
		private void disposeSinleMessage(Context context,Intent intent,int result){
			if(result==Activity.RESULT_OK){
				int viewIndex = intent.getIntExtra("viewIndex", 0);//在adapter中的位置
	           //将结果发送到UI
	            Intent outIntent = new Intent(TalkActivity.RECEIVE_TalkMessageItemAddReceiver_ACTION);
		        outIntent.putExtra("type", "out");
		        outIntent.putExtra("viewIndex", viewIndex);
		        sendBroadcast(outIntent);	
System.out.println("发送单发验证.广播");
			}else{//发送失败的消息
				
			}
			
		}
		/**
		 * 处理监听到群发消息
		 * @date 2013-3-3下午5:02:34
		 * @comment 
		 * @param intent
		 */
		private void disposeGroupMessage(Context context,Intent intent,int result){
			String address = intent.getStringExtra("address");
			String body = intent.getStringExtra("body");
            //将结果发送到界面更新广播
            Intent outIntent = new Intent(FSendActivity.RECEIVE_FSEND_ACTION);
            outIntent.putExtra("address", address);
            outIntent.putExtra("result", String.valueOf(result));
	        sendBroadcast(outIntent);
 System.out.println("发送群发验证广播");
		}
		/**
		 * 处理监听到的定时发送消息
		 * @date 2013-3-5下午4:41:22
		 * @comment 
		 * @param context
		 * @param intent
		 * @param result
		 */
		private void disposeTimingMessage(Context context,Intent intent,int result){
			String address = intent.getStringExtra("address");
			String body = intent.getStringExtra("body");
			//取得ID
			int id = intent.getIntExtra("id", 0);
			if(result==Activity.RESULT_OK){
				//改变消息状态
				String where = TimingSendMessageItem.TIMING_ID+"="+id;
				ArrayList<TimingSendMessageItem> results = DatabaseHelper.getAllTimingMessage(SMSListenerService.this, where, null);
				if(results.size()>0){
					TimingSendMessageItem item = results.get(0);
					ContentValues values = new ContentValues();
					switch (item.getRepeat()) {
					case TimingSendMessageItem.TIMING_REPEAT_ONE://一次性
						values.put(TimingSendMessageItem.TIMING_STATUS, TimingSendMessageItem.TIMING_STATUS_FINISH);
						DatabaseHelper.update(context, DatabaseHelper.TABLE_NAME_TIMING, values, where, null);
						break;
					case TimingSendMessageItem.TIMING_REPEAT_REPEAT_DAY://每天
						//计算下次执行时间
						long execute_time = Long.parseLong(item.getNextDate());
						execute_time+=DateTool.MILLISECOND_DAY;
						values.put(TimingSendMessageItem.TIMING_NEXTDATE, execute_time);
						DatabaseHelper.update(context, DatabaseHelper.TABLE_NAME_TIMING, values, where, null);
						break;
					case TimingSendMessageItem.TIMING_REPEAT_REPEAT_WEEK://每周
						caculateNextTime_Week(context,item,where);
						break;
					case TimingSendMessageItem.TIMING_REPEAT_REPEAT_MONTH://每月
						//计算下次执行时间
						long execute_time2 = Long.parseLong(item.getNextDate());
						Calendar c = Calendar.getInstance();
						if(c.get(Calendar.MONTH)>10){
							c.set(Calendar.YEAR, c.get(Calendar.YEAR)+1);
							c.set(Calendar.MONTH, 0);
						}else{
							c.set(Calendar.MONTH, c.get(Calendar.MONTH)+1);
						}
						Date date = new Date(execute_time2);
						c.set(Calendar.HOUR_OF_DAY, date.getHours());
						c.set(Calendar.MINUTE, date.getMinutes());
						values.put(TimingSendMessageItem.TIMING_NEXTDATE, c.getTimeInMillis());	
						DatabaseHelper.update(context, DatabaseHelper.TABLE_NAME_TIMING, values, where, null);
						break;
	
					default:
						break;
					}
					
				}
			}else{
				//弹出提醒，点击马上重新发送
				Toast.makeText(context, "你发送给"+address+"的消息发送失败，请重新发送", Toast.LENGTH_LONG).show();
			}
			
			//刷新界面
			sendBroadcast(new Intent(TimingSendManagerActivity.UpdateUIBroadCastReceiver.ACTION));
		}
		
    }
	
	
	/**
	 * 计算下次执行时间
	 * @date 2013-3-8下午3:48:32
	 * @comment 
	 * @param context
	 * @param item
	 * @param where
	 */
	private void caculateNextTime_Week(Context context,TimingSendMessageItem item,String where){
		Date date = new Date(Long.parseLong(item.getNextDate()));
		ContentValues values = new ContentValues();
		values.put(TimingSendMessageItem.TIMING_NEXTDATE, String.valueOf(DateTool.getNextExecuteTime_Week(item.getWeekDaySign(),date.getHours(),date.getMinutes())));	
		DatabaseHelper.update(context, DatabaseHelper.TABLE_NAME_TIMING, values, where, null);
	}
	AlarmReceiver alarmReceiver;

	/**
	 * 闹钟事件接收
	 * @author ives
	 * @date 2013-3-8下午10:17:58
	 * @version 1.0
	 * @comment
	 */
	public class AlarmReceiver extends BroadcastReceiver {
		public static final String ALARM_RECEIVER_NAME = "com.ian.sms.AlarmReceiver";
		public static final int TYPE_REFRESHDB = 1;
		public static final int TYPE_ADDTIMING = 2;
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getIntExtra("type", 0)) {
			case TYPE_REFRESHDB://刷新数据库
				refreshTimingSMS();
				break;
			case TYPE_ADDTIMING://添加timing任务
				
				break;

			default:
				break;
			}
			
		}
	}

	/**
	 * 启动数据库读取部分
	 * @date 2013-3-5下午4:45:33
	 * @comment
	 */
	private void startDBReader(){
		sendItems = new LinkedList<TimingSendMessageItem>();
		//闹钟服务
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(AlarmReceiver.ALARM_RECEIVER_NAME);
		intent.putExtra("type", 1);
		PendingIntent pendIntent = PendingIntent.getBroadcast(this,AlarmReceiver.TYPE_REFRESHDB, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+5000, READ_DB_INTERVAL, pendIntent);
	}
	/**
	 * 刷新读取数据库
	 * @date 2013-3-5下午4:51:03
	 * @comment
	 */
	private void refreshTimingSMS(){
		//读取数据库
		String where = TimingSendMessageItem.TIMING_STATUS+"="+TimingSendMessageItem.TIMING_STATUS_PENDING +" and "+TimingSendMessageItem.TIMING_NEXTDATE+">"+System.currentTimeMillis()+" and "+TimingSendMessageItem.TIMING_NEXTDATE+"<"+(System.currentTimeMillis()+READ_DB_INTERVAL);
		ArrayList<TimingSendMessageItem> result = DatabaseHelper.getAllTimingMessage(SMSListenerService.this, where, TimingSendMessageItem.TIMING_NEXTDATE+" desc");
		//求出与sendItems的差集,就是避免重复添加
		ArrayList<TimingSendMessageItem> temp = new ArrayList<TimingSendMessageItem>();
		for (int i = 0; i < result.size(); i++) {
			for (int j = 0; j < sendItems.size(); j++) {
				if(result.get(i).getId()==sendItems.get(j).getId()){//相同
					temp.add(result.get(i));
				}
			}
		}
		result.removeAll(temp);
		//将每个需要发送的item加入timer
		for (int i = 0; i < result.size(); i++) {
			addPendingTimingSMSItem(result.get(i));
		}
		//检查是否有需要发送的消息，如果有责获取wakelock
		if(sendItems.size()>0){
			getCPULock();
		}
	}
	WakeLock  wakeLock;
	private void getCPULock(){
		if(wakeLock==null){
			PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
	        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.ian.sms.smslistenerservice");
		}
        wakeLock.acquire(); //设置保持唤醒
	}
	private void releaseCPULock(){
		if(wakeLock==null)return;
		if(wakeLock.isHeld()){
			wakeLock.release();
			wakeLock = null;
		}
		
	}
	/**
	 * 添加单个定时发送消息
	 * @date 2013-3-5下午4:42:56
	 * @comment 
	 * @param item
	 */
	private void addPendingTimingSMSItem(final TimingSendMessageItem item){
		final TimerTask task = new TimerTask() {
			@Override
			public void run() {
				sendTimingSMS(item);
				if(sendItems.size()==0){
					releaseCPULock();
				}
			}
		};
		handler.post(new Runnable() {
			@Override
			public void run() {
				timer.schedule(task, new Date(Long.parseLong(item.getNextDate())));
			}
		});
//		AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//		Intent intent = new Intent(AlarmReceiver.ALARM_RECEIVER_NAME);
//		Bundle bundle = new Bundle();
//		bundle.putSerializable("item", item);
//		intent.putExtras(bundle);
//		PendingIntent pendIntent = PendingIntent.getBroadcast(this,0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, Long.parseLong(item.getNextDate())-System.currentTimeMillis()+SystemClock.elapsedRealtime(), pendIntent);
		sendItems.add(item);
	}
	/**
	 * 发送指定的定时消息
	 * @date 2013-3-8下午2:29:38
	 * @comment 
	 * @param item
	 */
	private void sendTimingSMS(TimingSendMessageItem item){
		//发送消息
		SmsTool.sendTimingMessage(SMSListenerService.this, StringTool.getSetToString(item.getAddress(), ","),item.getBody(),item.getId());
		//维护存储的集合
		sendItems.remove(item);
	}
	/**
	 * 外部可以通过此广播执行一些指定操作
	 * @author ives
	 * @date 2013-3-4上午11:23:18
	 * @version 1.0
	 * @comment
	 */
	class ExecutingBroadCast extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(EXECUTING_RECEIVED_ACTION)){
				int execute_type = intent.getIntExtra(EXECUTE_TYPE_NAME, 0);
				switch(execute_type){
					case EXECUTE_TYPE_AUTOREPLY://自动回复相关的执行
						disposeAutoReplyBDCT(context,intent);
						break;
					case EXECUTE_TYPE_TIMING://定时发送相关的执行
						disposeTimingBDCT(context, intent);
						break;
					case EXECUTE_TYPE_GROUPSEND://群发
						disposeGroupSendBDCT(context,intent);
						break;
					default:
						
					break;
				}
			}
		}
		/**
		 * 群发相关的广播消息解析
		 * @date 2013-3-7上午10:35:19
		 * @comment 
		 * @param context
		 * @param intent
		 */
		private void disposeGroupSendBDCT(Context context,Intent intent){
			String body = intent.getStringExtra("body");
//			ArrayList<String>receivers = intent.getStringArrayListExtra("receives");
//			SmsTool.sendGroupMessage(SMSListenerService.this, body, receivers);
		}
		/**
		 * 自动回复广播消息解析
		 * @date 2013-3-4上午11:38:49
		 * @comment 
		 * @param context
		 * @param intent
		 */
		private void disposeAutoReplyBDCT(Context context,Intent intent){
				
		}
		/**
		 * 定时广播消息解析
		 * @date 2013-3-4上午11:38:52
		 * @comment 
		 * @param context
		 * @param intent
		 */
		private void disposeTimingBDCT(Context context,Intent intent){
			int execute_action = intent.getIntExtra(EXECUTE_TIMING_ACTION_NAME, 0);
			switch (execute_action) {
			case EXECUTE_TIMING_ACTION_REFRESHDB:
				refreshTimingSMS();
				break;

			default:
				break;
			}
		}
	}
	PhoneListenerReceiver phoneListenerReceiver;
	public static long lastExecuteTime;
	public static String lastExecuteNumber="";
	public static boolean isAnswer = false;
	public static long ExecuteSameinterval = 1000*30;//同一消息处理间隔时间为30秒
	/**
	 * 来电监听
	 * @author ives
	 * @date 2013-3-9下午3:50:41
	 * @version 1.0
	 * @comment
	 */
	class PhoneListenerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
			}else{//来电
				TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);  
				tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);//设置一个监听器
				
			}
		}
		PhoneStateListener listener=new PhoneStateListener(){
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				super.onCallStateChanged(state, incomingNumber);
				switch(state){
				case TelephonyManager.CALL_STATE_IDLE://挂断
					disposeOnCallIDLEChanged(incomingNumber);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK://接听
					isAnswer = true;
					break;
				case TelephonyManager.CALL_STATE_RINGING://响铃
					break;
				}
			}
	 
		};
		/**
		 * 当电话挂断的时候检查是否需要自动回去
		 * @date 2013-3-9下午4:49:11
		 * @comment 
		 * @param number
		 */
		private void disposeOnCallIDLEChanged(String number){
			if(isAnswer){
				isAnswer = false;
				lastExecuteTime = System.currentTimeMillis();
				lastExecuteNumber = number;
				return;
			}
			if(number.equals(lastExecuteNumber)){//如果和上次处理的号码一样
				if(System.currentTimeMillis()-lastExecuteTime<ExecuteSameinterval){//如果处理间隔时间未到
					return;
				}
			}
			//执行自动回复
			lastExecuteTime = System.currentTimeMillis();
			lastExecuteNumber = number;
			checkAutoReplyForCall(number);
		}
	}
	/**
	 * 检查是否有需要自动回复
	 * @date 2013-3-9下午4:48:39
	 * @comment 
	 * @param number
	 */
	private void checkAutoReplyForCall(String number){
		//确定是否认识联系人
		StringBuilder where = new StringBuilder();
		if(SmsTool.getNameFromPhone(number, SMSListenerService.this)==null){//陌生人
			where.append(AutoReplyItem.ISSTART+"=1 and "+AutoReplyItem.ISNOANSWERCALL+"=1 and "+AutoReplyItem.ISINCLUDESTRANGER+"=1");
		}else{//联系人
			where.append(AutoReplyItem.ISSTART+"=1 and "+AutoReplyItem.ISNOANSWERCALL+"=1 and("+AutoReplyItem.ISALLCONTACTS+"=1 or "+AutoReplyItem.CONTACTS+" like '%"+number+"%')");
		}
		ArrayList<AutoReplyItem>reslt = DatabaseHelper.getAllReplyMessage(this, where.toString(), null);
		for (int i = 0; i < reslt.size(); i++) {
			SmsTool.sendMessage_AutoReply( number, reslt.get(i).getBody(), this);
		}
	}
}
