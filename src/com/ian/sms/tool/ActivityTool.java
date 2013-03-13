package com.ian.sms.tool;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.util.Log;

public class ActivityTool {
	/**
	 * 检测某Activity是否在当前Task的栈顶
	 * 
	 * @date Feb 28, 20134:45:50 PM
	 * @comment
	 * @param cmdName
	 * @return
	 */
	public static boolean isTopActivy(Context context, String cmtName) {
		 ActivityManager manager = (ActivityManager)
		 context.getSystemService(Context.ACTIVITY_SERVICE);
		 List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
		 String cmpNameTemp = null;
		 if(null != runningTaskInfos){
			 cmpNameTemp=runningTaskInfos.get(0).topActivity.getClassName();
		 }
		
		 if(null == cmpNameTemp)return false;
		 return cmpNameTemp.equals(cmtName);
	}
	/**
	 * 获取当前栈顶的activity class完整名字
	 * @date 2013-3-1下午2:40:56
	 * @comment 
	 * @param context
	 * @return
	 */
	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return (runningTaskInfos.get(0).topActivity).getClassName();
		else
			return "no activity";
	}
}
