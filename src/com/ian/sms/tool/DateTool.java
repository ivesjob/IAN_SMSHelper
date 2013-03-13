package com.ian.sms.tool;

import java.util.Calendar;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

public class DateTool {
	public static long MILLISECOND_SECOND = 1000;//一秒含多少毫秒
	public static long MILLISECOND_MINUTE = MILLISECOND_SECOND*60;//一分钟含多少毫秒
	public static long MILLISECOND_HOUR = MILLISECOND_MINUTE*60;//一小时含多少毫秒
	public static long MILLISECOND_DAY = MILLISECOND_HOUR*24;//一天含多少毫秒
	public static long MILLISECOND_WEEK = MILLISECOND_DAY*7;//一星期含多少毫秒
	
	/**
	 * 时间转换函数
	 * @date Feb 25, 20131:34:46 PM
	 * @comment 
	 * @param context
	 * @param when
	 * @param fullFormat
	 * @return
	 */
	public static String formatTimeStampString(Context context, long when,boolean fullFormat) {
		Time then = new Time();
		then.set(when);
		Time now = new Time();
		now.setToNow();
		// Basic settings for formatDateTime() we want for all cases.
		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;
		// If the message is from a different year, show the date and year.
		if (then.year != now.year) {
			format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
		} else if (then.yearDay != now.yearDay) {
			// If it is from a different day than today, show only the date.
			format_flags |= DateUtils.FORMAT_SHOW_DATE;
		} else {
			// Otherwise, if the message is from today, show the time.
			format_flags |= DateUtils.FORMAT_SHOW_TIME;
		}
		// If the caller has asked for full details, make sure to show the date
		// and time no matter what we've determined above (but still make
		// showing
		// the year only happen if it is a different year from today).
		if (fullFormat) {
			format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
		}
		return DateUtils.formatDateTime(context, when, format_flags);
	}
	public static long getNextExecuteTime_Week1(int patternNumber,int hour,int minute){
		char[] weektag  = BinaryTool.int2Binary(patternNumber).toCharArray();
		int start_index = DateTool.getWeekDay();//开始的下标
		int interval = 0;//时间间隔
		//if(start_index>5)start_index = 1;//如果今天是星期六，就从下周星期天（0）开始循环，否则直接从明天（tomorrow_index）开始循环
		for (int i = start_index; i <= weektag.length+start_index; i++,interval++) {//总数为7天，从明天的下标开始
			if(weektag[i%7]==49){//如果等于1
				System.out.println("下次执行在星期："+i%7);
				break;
			}
		}
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR)+interval);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		
		return c.getTimeInMillis();
	}
	/**
	 * 
	 * @date 2013-3-8下午5:38:01
	 * @comment 
	 * @param patternNumber
	 * @return
	 */
	public static long getNextExecuteTime_Week(int patternNumber,int hour,int minute){
		char[] weektag  = BinaryTool.int2Binary(patternNumber).toCharArray();
		int tomorrow_index = DateTool.getWeekDay()+1;//明天的下标
		int interval = 1;//时间间隔
		//if(tomorrow_index>6)tomorrow_index = 0;//如果今天是星期六，就从下周星期天（0）开始循环，否则直接从明天（tomorrow_index）开始循环
		for (int i = tomorrow_index; i <= weektag.length+tomorrow_index; i++,interval++) {//总数为7天，从明天的下标开始
			if(weektag[i%7]==49){//如果等于1
				System.out.println("下次执行在星期："+i%7);
				break;
			}
		}
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR)+interval);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		
		return c.getTimeInMillis();
	}
	private static Time time;
	
	
	public static Time getTime(){
		if(time==null)time = new Time();
		return time;
	}
	public static String getDate(){
		Time time = getTime();  
        time.setToNow();  
        return time.year+"-"+(time.month+1)+"-"+time.monthDay;
	}
	public static String getTimeToString(){
		Time time = getTime();  
        time.setToNow();  
        return time.hour+":"+time.minute;
	}
	public static int getYear(){
		Time time = getTime();  
        time.setToNow();  
		return time.year;
	}
	public static int getMonthOfYear(){
		Time time = getTime();  
        time.setToNow(); 
		return time.month;
	}
	public static int getDayOfMonth(){
		Time time = getTime();  
        time.setToNow(); 
		return time.monthDay;
	}
	public static int getHour(){
		Time time = getTime();  
        time.setToNow(); 
		return time.hour;
	}
	public static int getMinute(){
		Time time = getTime();  
        time.setToNow(); 
		return time.minute;
	}
	public static int getSecond(){
		Time time = getTime();  
        time.setToNow(); 
		return time.second;
	}
	public static int getWeekDay(){
		Time time = getTime();  
        time.setToNow(); 
		return time.weekDay;
	}
	
	
	
	
}
