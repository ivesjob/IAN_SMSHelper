package com.ian.sms.mode;

import java.io.Serializable;

import com.ian.sms.service.SMSListenerService;

/**
 * 定时短信
 * @author ives
 * @date 2013-3-3下午11:14:45
 * @version 1.0
 * @comment
 */
public class TimingSendMessageItem implements Serializable {
	private int id;
	private String body;
	private String date;//执行时间
	private String address;//多个号码用逗号隔开
	private int repeat;//重复方式（一次性，每天，每周，每月）
	private int status;//状态（待发送，已发送，草稿）
	private String nextDate;//下次执行时间（如果是重复任务，在每次执行之后进行计算）
	private int weekDaySign;//0-127之间，用于转换成二进制从而达到记录选定了一周中的多少天(0表示未选中，1表示选中)
	
	/*数据库字段*/
	public static final String TIMING_ID = "id";
	public static final String TIMING_DATE = "date";
	public static final String TIMING_BODY = "body";
	public static final String TIMING_ADDRESS = "address";
	public static final String TIMING_REPEAT = "repeat";
	public static final String TIMING_STATUS = "status";
	public static final String TIMING_NEXTDATE = "nextdate";
	public static final String TIMING_WEEKDAYSIGN = "weekdaysign";
	/*重复方式*/
	public static final int TIMING_REPEAT_ONE = 0;//一次性
	public static final int TIMING_REPEAT_REPEAT_DAY = 1;
	public static final int TIMING_REPEAT_REPEAT_WEEK = 2;
	public static final int TIMING_REPEAT_REPEAT_MONTH = 3;
	/*状态*/
	public static final int TIMING_STATUS_PENDING = 0;
	public static final int TIMING_STATUS_FINISH = 1;
	public static final int TIMING_STATUS_DRAFT = 2;
	
	public static final String[] ALLCLUMN = new String[]{TIMING_ID,TIMING_BODY,TIMING_DATE,TIMING_ADDRESS,TIMING_REPEAT,TIMING_STATUS,TIMING_NEXTDATE,TIMING_WEEKDAYSIGN};
	
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getRepeat() {
		return repeat;
	}
	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

	public String getNextDate() {
		return nextDate;
	}
	public void setNextDate(String nextDate) {
		this.nextDate = nextDate;
	}
	
	public int getWeekDaySign() {
		return weekDaySign;
	}
	public void setWeekDaySign(int weekDaySign) {
		this.weekDaySign = weekDaySign;
	}
	public String getSatusForString(){
		switch(status){
		case TIMING_STATUS_PENDING:
			long currentTime = System.currentTimeMillis();
			if(Long.parseLong(nextDate)<(currentTime+SMSListenerService.READ_DB_INTERVAL)&&Long.parseLong(nextDate)>currentTime){
				return "待发送";
			}else{
				return "未发送";
			}
		case TIMING_STATUS_FINISH:
			return "已发送";
		case TIMING_STATUS_DRAFT:
			return "草稿";
			default:
			return "未知";
		}
	}
	@Override
	public boolean equals(Object o) {
		return this.id==(((TimingSendMessageItem)o).getId());
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
}
