package com.ian.sms.mode;

import android.net.Uri;
/**
 * 对应的数据库字段
 * @author ives
 * @date 2013-3-1上午11:51:45
 * @version 1.0
 * @comment
 */
public class SMS {
	public static final String _ID = "_id";
	public static final String _COUNT = "_count";
	//对应的Uri
	public static final Uri CONTENT_URI = Uri.parse("content://sms");
	public static final Uri CONTENT_URI_INBOX = Uri.parse("content://sms/inbox");//         收件箱  
	public static final Uri CONTENT_URI_SENT = Uri.parse("content://sms/sent");//          已发送  
	public static final Uri CONTENT_URI_DRAFT = Uri.parse("content://sms/draft");//          草稿  
	public static final Uri CONTENT_URI_OUTBOX = Uri.parse("content://sms/outbox");//      发件中 
	public static final Uri CONTENT_URI_FAILED = Uri.parse("content://sms/failed");//         失败  
	public static final Uri CONTENT_URI_QUEUED = Uri.parse("content://sms/queued");//     待发送
	//过滤内容
	public static final String FILTER = "!imichat";
	/*数据库字段*/
	public static final String TYPE = "type";//类型1已经接收。2已经发出。3草稿。4正在发送。5发送失败。6待发送
	public static final String THREAD_ID = "thread_id";
	public static final String ADDRESS = "address";
	public static final String PERSON_ID = "person";
	public static final String DATE = "date";
	public static final String READ = "read";
	public static final String BODY = "body";
	public static final String PROTOCOL = "protocol";

	/*消息类型*/
	public static final int MESSAGE_TYPE_ALL = 0;
	public static final int MESSAGE_TYPE_INBOX = 1;
	public static final int MESSAGE_TYPE_SENT = 2;
	public static final int MESSAGE_TYPE_DRAFT = 3;
	public static final int MESSAGE_TYPE_OUTBOX = 4;
	public static final int MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages
	public static final int MESSAGE_TYPE_QUEUED = 6; // for messages to send later
	/*消息协议*/
	public static final int PROTOCOL_SMS = 0;// SMS_PROTO
	public static final int PROTOCOL_MMS = 1;// MMS_PROTO
	/*消息类型*/
	public static final int SMS_TYPE_IN = 1;
	public static final int SMS_TYPE_OUT = 2;
	/*是否阅读*/
	public static final int SMS_READ_YES = 1;
	public static final int SMS_READ_NO = 0;

}