package com.ian.sms.tool;

public class Globals {
	public static  int MAX_ID = 0;//最大ＩＤ
	
	public static final String SMS_SEND_TYPE_TAG = "sms_send_type";
	
	public static final int SMS_SEND_TYPE_SINGLE = 1;//单个发送
	public static final int SMS_SEND_TYPE_AUTOREPLY = 2;//自动回复
	public static final int SMS_SEND_TYPE_GROUP = 3;//群发
	public static final int SMS_SEND_TYPE_TIMING = 4;//定时短信
}
