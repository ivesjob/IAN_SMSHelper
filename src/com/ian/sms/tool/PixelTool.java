package com.ian.sms.tool;

import android.content.Context;

/**
 * 像素操作辅助类
 * @author ives
 * @date Feb 19, 20134:13:10 PM
 * @version 1.0
 * @comment
 */
public class PixelTool {
	/**
	 * 将dip转换成像素
	 * @date 2013-3-4上午10:38:39
	 * @comment 
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
}
