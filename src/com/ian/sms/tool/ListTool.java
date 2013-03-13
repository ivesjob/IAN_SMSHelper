package com.ian.sms.tool;

import java.util.ArrayList;
import java.util.HashSet;

import com.ian.sms.mode.ContactsItem;

public class ListTool {
	/**
	 * 去除ArrayList的重复值
	 * @date 2013-3-3下午4:27:26
	 * @comment 
	 * @param arlList
	 */
	public static void removeDuplicate(ArrayList<ContactsItem> arlList) { 
		HashSet<ContactsItem> h = new HashSet<ContactsItem>(arlList); 
		arlList.clear(); 
		arlList.addAll(h); 
	} 
}
