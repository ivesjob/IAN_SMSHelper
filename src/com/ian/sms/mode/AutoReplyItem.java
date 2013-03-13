package com.ian.sms.mode;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 自动回复
 * @author ives
 * @date 2013-3-3下午11:15:14
 * @version 1.0
 * @comment
 */
public class AutoReplyItem implements Serializable{
	private int id;//id
	private String body;//内容
	private boolean isStart;//是否已经启动
	private boolean isIncludeStranger;//是否包含陌生人
	private boolean isAllContacts;//是否对所有联系人生效
	private boolean isOnlyReply;//同一人是否只回复一次
	private boolean isNoAnswerCall;//未接来电是否生效
	private boolean isHangUpCall;//是否对挂断来电生效
	private boolean isAcceptSMS;//收到短信
	
	private boolean isCustomTime;//是否自定义接收时间段
	
	private long day_startTime;//自定义接收时间的每天开始时间(单位为分钟)
	private long day_stopTime;//自定义接收时间的每天结束时间（单位为分钟）
	
	private String contacts;//用于容纳自定义的接收联系人


	/*数据库字段*/
	public static final String ID = "id";
	public static final String BODY = "body";
	public static final String ISSTART = "isstart";
	public static final String ISINCLUDESTRANGER = "isincludestranger";
	public static final String ISALLCONTACTS = "isallcontacts";
	public static final String ISONLYREPLY = "isonlyreply";
	public static final String ISNOANSWERCALL = "isnoanswerCall";
	public static final String ISHANGUPCALL = "ishangupCall";
	public static final String ISACCEPTSMS = "isacceptsms";
	public static final String ISCUSTOMTIME = "iscustomtime";
	public static final String DAY_STARTTIME = "day_starttime";
	public static final String DAY_STOPTIME = "day_stoptime";
	public static final String CONTACTS = "contacts";
	public static final String createTableStr ="create table reply(id INTEGER PRIMARY KEY,body TEXT,isstart INTEGER," +
			"isincludestranger INTEGER,isallcontacts INTEGER,isonlyreply INTEGER,isnoanswerCall INTEGER,ishangupCall INTEGER,isacceptsms INTEGER," +
			"iscustomtime INTEGER,day_starttime INTEGER,day_stoptime INTEGER,contacts TEXT)";
	
	public static final String[] ALLCLUMN = new String[]{ID,BODY,ISSTART,ISINCLUDESTRANGER,ISALLCONTACTS,
		ISONLYREPLY,ISNOANSWERCALL,ISHANGUPCALL,ISACCEPTSMS,ISCUSTOMTIME,DAY_STARTTIME,DAY_STOPTIME,CONTACTS};
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isStart() {
		return isStart;
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isIncludeStranger() {
		return isIncludeStranger;
	}

	public void setIncludeStranger(boolean isIncludeStranger) {
		this.isIncludeStranger = isIncludeStranger;
	}

	public boolean isAllContacts() {
		return isAllContacts;
	}

	public void setAllContacts(boolean isAllContacts) {
		this.isAllContacts = isAllContacts;
	}

	public boolean isOnlyReply() {
		return isOnlyReply;
	}

	public void setOnlyReply(boolean isOnlyReply) {
		this.isOnlyReply = isOnlyReply;
	}

	public boolean isNoAnswerCall() {
		return isNoAnswerCall;
	}

	public void setNoAnswerCall(boolean isNoAnswerCall) {
		this.isNoAnswerCall = isNoAnswerCall;
	}

	public boolean isHangUpCall() {
		return isHangUpCall;
	}

	public void setHangUpCall(boolean isHangUpCall) {
		this.isHangUpCall = isHangUpCall;
	}

	public boolean isAcceptSMS() {
		return isAcceptSMS;
	}

	public void setAcceptSMS(boolean isAcceptSMS) {
		this.isAcceptSMS = isAcceptSMS;
	}

	public boolean isCustomTime() {
		return isCustomTime;
	}

	public void setCustomTime(boolean isCustomTime) {
		this.isCustomTime = isCustomTime;
	}
	public long getDay_startTime() {
		return day_startTime;
	}

	public void setDay_startTime(long day_startTime) {
		this.day_startTime = day_startTime;
	}

	public long getDay_stopTime() {
		return day_stopTime;
	}

	public void setDay_stopTime(long day_stopTime) {
		this.day_stopTime = day_stopTime;
	}

	public String getContacts() {
		return contacts;
	}

	public void setContacts(String contacts) {
		this.contacts = contacts;
	}
	
	
}
