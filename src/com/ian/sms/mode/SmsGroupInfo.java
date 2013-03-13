package com.ian.sms.mode;
/**
 * 
 * @author ives
 * @date Feb 25, 201311:43:51 AM
 * @version 1.0
 * @comment
 */
public class SmsGroupInfo {
	/**
	 * 短信内容
	 */
	public String smsBody;
	/**
	 * 发送短信的电话号码
	 */
	public String address;
	/**
	 * 发送短信的日期和时间
	 */
	public long date;
	/**
	 * 发送短信人的姓名
	 */
	public String name;
	/**
	 * 短信类型1是接收到的，2是已发出的
	 */
	public String type;
	/**
	 * 未读短信数量
	 */
	public int smsNoReadCount;
	
	public int smsDraftCount;
	
	public int smsFailedCount;
	/**
	 * 所有短信
	 */
	public String smsAllCount;
	/**
	 * 短信线程ID 
	 */
	public String threadId;

	

}
